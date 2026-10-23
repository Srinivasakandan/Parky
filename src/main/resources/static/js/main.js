// Smart Parking System - Frontend Client Application Logic

let currentUser = null;
let dailyChartInstance = null;
let distributionChartInstance = null;
let currentExitRecord = null;

document.addEventListener('DOMContentLoaded', () => {
    checkSession();
});

// View Navigation Handler
function switchView(viewId) {
    document.querySelectorAll('.view-section').forEach(sec => sec.style.display = 'none');
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));

    const viewName = viewId.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
    const targetSection = document.getElementById(viewName + 'View');
    if (targetSection) {
        targetSection.style.display = 'block';
    }

    const navBtn = document.getElementById('nav' + viewName.charAt(0).toUpperCase() + viewName.slice(1));
    if (navBtn) {
        navBtn.classList.add('active');
    }

    // Refresh data when switching to specific views
    if (viewId === 'dashboard') {
        loadDashboardData();
    } else if (viewId === 'slots') {
        loadParkingSlots();
    } else if (viewId === 'rates') {
        loadParkingRates();
    } else if (viewId === 'employees') {
        loadEmployees();
    } else if (viewId === 'audit-logs') {
        loadAuditLogs();
    }
}

function switchTab(tabId, btnElement) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

    document.getElementById(tabId).classList.add('active');
    btnElement.classList.add('active');
}

// Session Check & Security Navigation
async function checkSession() {
    try {
        const resp = await fetch('/api/auth/current-user');
        const res = await resp.json();
        if (res.success && res.data) {
            currentUser = res.data;
            updateUIForRole();
            switchView('dashboard');
        } else {
            currentUser = null;
            updateUIForRole();
            switchView('customer');
        }
    } catch (e) {
        currentUser = null;
        updateUIForRole();
        switchView('customer');
    }
}

function updateUIForRole() {
    const userBadge = document.getElementById('userBadge');
    const authBtn = document.getElementById('authBtn');

    if (!currentUser) {
        userBadge.style.display = 'none';
        authBtn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Employee / Admin Login';
        
        document.getElementById('navCustomer').style.display = 'inline-block';
        document.getElementById('navDashboard').style.display = 'none';
        document.getElementById('navEntryExit').style.display = 'none';
        document.getElementById('navSlots').style.display = 'none';
        document.getElementById('navRates').style.display = 'none';
        document.getElementById('navEmployees').style.display = 'none';
        document.getElementById('navAuditLogs').style.display = 'none';
    } else {
        userBadge.style.display = 'inline-block';
        userBadge.textContent = currentUser.name + ' (' + currentUser.role + ')';
        authBtn.innerHTML = '<i class="fa-solid fa-right-from-bracket"></i> Logout';

        document.getElementById('navCustomer').style.display = 'inline-block';
        document.getElementById('navDashboard').style.display = 'inline-block';
        document.getElementById('navEntryExit').style.display = 'inline-block';
        document.getElementById('navSlots').style.display = 'inline-block';

        if (currentUser.role === 'ADMIN') {
            document.getElementById('navRates').style.display = 'inline-block';
            document.getElementById('navEmployees').style.display = 'inline-block';
            document.getElementById('navAuditLogs').style.display = 'inline-block';
        } else {
            document.getElementById('navRates').style.display = 'none';
            document.getElementById('navEmployees').style.display = 'none';
            document.getElementById('navAuditLogs').style.display = 'none';
        }
    }
}

function toggleAuth() {
    if (currentUser) {
        handleLogout();
    } else {
        switchView('login');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const resp = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const res = await resp.json();

        if (res.success) {
            currentUser = res.data;
            updateUIForRole();
            switchView('dashboard');
        } else {
            alert('Login failed: ' + res.message);
        }
    } catch (err) {
        alert('An error occurred during login.');
    }
}

function fillLogin(email, pwd) {
    document.getElementById('loginEmail').value = email;
    document.getElementById('loginPassword').value = pwd;
}

async function handleLogout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    currentUser = null;
    updateUIForRole();
    switchView('customer');
}

// Customer Search Portal
async function handleCustomerSearch(e) {
    e.preventDefault();
    const plate = document.getElementById('customerSearchInput').value.trim();
    if (!plate) return;

    try {
        const resp = await fetch('/api/customer/search/' + encodeURIComponent(plate));
        const res = await resp.json();

        if (res.success && res.data) {
            const data = res.data;
            document.getElementById('customerResultCard').style.display = 'block';
            document.getElementById('cVehicleNumber').textContent = data.vehicleNumber;
            document.getElementById('cVehicleType').textContent = data.vehicleType;
            document.getElementById('cSlot').textContent = data.parkingSlot;
            document.getElementById('cEntryTime').textContent = formatDate(data.entryTime);
            document.getElementById('cExitTime').textContent = data.exitTime ? formatDate(data.exitTime) : 'N/A (Currently Parked)';
            document.getElementById('cDuration').textContent = data.durationFormatted;
            document.getElementById('cFee').textContent = '₹' + data.parkingFee;
            document.getElementById('cStatusBadge').textContent = data.status;
            document.getElementById('cPaymentStatus').textContent = data.paymentStatus || 'PENDING';
            document.getElementById('cTxnId').textContent = data.transactionId || 'N/A';

            if (data.status === 'PARKED') {
                document.getElementById('cStatusBadge').className = 'badge badge-success';
                document.getElementById('cPrintBtn').style.display = 'none';
            } else {
                document.getElementById('cStatusBadge').className = 'badge badge-secondary';
                document.getElementById('cPrintBtn').style.display = 'block';
                window.lastCustomerReceipt = data;
            }
        } else {
            alert('Vehicle record not found.');
            document.getElementById('customerResultCard').style.display = 'none';
        }
    } catch (err) {
        alert('Could not search vehicle.');
    }
}

// Vehicle Entry Registration
async function handleVehicleEntry(e) {
    e.preventDefault();
    const vehicleNumber = document.getElementById('entryVehicleNumber').value.trim().toUpperCase();
    const vehicleType = document.getElementById('entryVehicleType').value;
    const preferredSlotNumber = document.getElementById('entryPreferredSlot').value.trim();

    if (!vehicleNumber) {
        alert('Vehicle number is required.');
        return;
    }

    const formatRegex = /^[A-Z0-9]{4,15}$/;
    if (!formatRegex.test(vehicleNumber)) {
        alert('Invalid vehicle number format. Must be 4 to 15 alphanumeric characters.');
        return;
    }

    try {
        const resp = await fetch('/api/vehicles/entry', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ vehicleNumber, vehicleType, preferredSlotNumber })
        });
        const res = await resp.json();

        if (res.success) {
            alert('Vehicle Parked Successfully!\nAssigned Slot: ' + res.data.slotNumber);
            document.getElementById('entryVehicleNumber').value = '';
            document.getElementById('entryPreferredSlot').value = '';
            switchView('dashboard');
        } else {
            alert('Parking Error: ' + (res.message || 'Unknown error from server.'));
        }
    } catch (err) {
        console.error('Error processing vehicle entry:', err);
        alert('Error processing vehicle entry: ' + (err.message || 'Network or server error.'));
    }
}

// Vehicle Exit Lookup & Calculation
async function lookupExitVehicle() {
    const plate = document.getElementById('exitVehicleSearchInput').value.trim().toUpperCase();
    if (!plate) {
        alert('Please enter a vehicle number.');
        return;
    }

    try {
        const resp = await fetch('/api/vehicles/' + encodeURIComponent(plate));
        const res = await resp.json();

        if (res.success && res.data) {
            const data = res.data;
            if (data.status !== 'PARKED') {
                alert('Vehicle ' + plate + ' is not currently parked.');
                return;
            }

            currentExitRecord = data;
            document.getElementById('exitBillDetailsCard').style.display = 'block';
            document.getElementById('exVehicleNum').textContent = data.vehicleNumber;
            document.getElementById('exVehicleType').textContent = data.vehicleType;
            document.getElementById('exSlot').textContent = data.parkingSlot;
            document.getElementById('exEntryTime').textContent = formatDate(data.entryTime);
            document.getElementById('exExitTime').textContent = 'NOW (Auto Generated)';
            document.getElementById('exDuration').textContent = data.durationFormatted;
            document.getElementById('exRate').textContent = '₹/hr as per DB';
            document.getElementById('exTotalFee').textContent = '₹' + data.parkingFee;
        } else {
            alert('Active parking record not found for vehicle: ' + plate);
        }
    } catch (err) {
        console.error('Error looking up vehicle:', err);
        alert('Error looking up vehicle: ' + (err.message || 'Network or server error.'));
    }
}

// Confirm Vehicle Exit & Process Payment
async function confirmVehicleExit() {
    if (!currentExitRecord) return;

    const paymentMethod = document.getElementById('exitPaymentMethod').value;

    try {
        const resp = await fetch('/api/vehicles/exit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                vehicleNumber: currentExitRecord.vehicleNumber,
                paymentMethod: paymentMethod
            })
        });
        const res = await resp.json();

        if (res.success && res.data) {
            alert('Vehicle exit & payment completed!');
            showReceiptModal(res.data);
            document.getElementById('exitBillDetailsCard').style.display = 'none';
            document.getElementById('exitVehicleSearchInput').value = '';
            currentExitRecord = null;
        } else {
            alert('Exit failed: ' + res.message);
        }
    } catch (err) {
        console.error('Error completing vehicle exit:', err);
        alert('Error completing exit: ' + (err.message || 'Network or server error.'));
    }
}

// Printable Digital Receipt Modal
function showReceiptModal(bill) {
    document.getElementById('rVehicleNumber').textContent = bill.vehicleNumber;
    document.getElementById('rVehicleType').textContent = bill.vehicleType;
    document.getElementById('rSlot').textContent = bill.slotNumber;
    document.getElementById('rEntryTime').textContent = formatDate(bill.entryTime);
    document.getElementById('rExitTime').textContent = formatDate(bill.exitTime);
    document.getElementById('rDuration').textContent = bill.durationFormatted;
    document.getElementById('rRate').textContent = '₹' + bill.ratePerHour + '/hr';
    document.getElementById('rFee').textContent = '₹' + bill.parkingFee;
    document.getElementById('rPaymentStatus').textContent = bill.paymentStatus;
    document.getElementById('rPaymentMethod').textContent = bill.paymentMethod;
    document.getElementById('rTxnId').textContent = bill.transactionId;

    document.getElementById('receiptModal').classList.add('active');
}

function printCustomerReceipt() {
    if (window.lastCustomerReceipt) {
        const d = window.lastCustomerReceipt;
        showReceiptModal({
            vehicleNumber: d.vehicleNumber,
            vehicleType: d.vehicleType,
            slotNumber: d.parkingSlot,
            entryTime: d.entryTime,
            exitTime: d.exitTime,
            durationFormatted: d.durationFormatted,
            ratePerHour: '-',
            parkingFee: d.parkingFee,
            paymentStatus: d.paymentStatus || 'PAID',
            paymentMethod: d.paymentMethod || 'UPI',
            transactionId: d.transactionId || 'TXN123'
        });
    }
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

// Dashboard Stats & Charts Loader
async function loadDashboardData() {
    try {
        const resp = await fetch('/api/dashboard');
        const res = await resp.json();

        if (res.success && res.data) {
            const s = res.data;
            document.getElementById('statParked').textContent = s.currentlyParked;
            document.getElementById('statAvailable').textContent = s.availableSlots;
            document.getElementById('statOccupied').textContent = s.occupiedSlots;
            document.getElementById('statTodayRevenue').textContent = '₹' + s.todayRevenue;
            document.getElementById('statPeakHour').textContent = String(s.peakParkingHour).padStart(2, '0') + ':00';
        }

        loadParkedTable();
        loadCharts();
    } catch (err) {
        console.error('Failed to load dashboard data', err);
    }
}

async function loadParkedTable() {
    try {
        const resp = await fetch('/api/vehicles/parked');
        const res = await resp.json();
        const tbody = document.getElementById('currentlyParkedTableBody');

        if (res.success && res.data.length > 0) {
            tbody.innerHTML = res.data.map(rec => `
                <tr>
                    <td><strong>${rec.vehicle.vehicleNumber}</strong></td>
                    <td>${rec.vehicle.vehicleType}</td>
                    <td><span class="badge badge-success">${rec.slot.slotNumber}</span></td>
                    <td>${formatDate(rec.entryTime)}</td>
                    <td><span class="badge badge-info">${rec.status}</span></td>
                    <td>
                        <button class="btn btn-danger" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="quickExit('${rec.vehicle.vehicleNumber}')">
                            <i class="fa-solid fa-right-from-bracket"></i> Exit
                        </button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No vehicles currently parked.</td></tr>';
        }
    } catch (err) {
        console.error('Failed to load parked vehicles', err);
    }
}

function quickExit(plateNumber) {
    switchView('entry-exit');
    switchTab('exitTab', document.querySelectorAll('.tab-btn')[1]);
    document.getElementById('exitVehicleSearchInput').value = plateNumber;
    lookupExitVehicle();
}

async function loadCharts() {
    try {
        const [dailyResp, distResp] = await Promise.all([
            fetch('/api/revenue/daily'),
            fetch('/api/revenue/distribution')
        ]);

        const dailyData = (await dailyResp.json()).data;
        const distData = (await distResp.json()).data;

        // Render Daily Revenue Chart
        const dailyCtx = document.getElementById('dailyRevenueChart').getContext('2d');
        if (dailyChartInstance) dailyChartInstance.destroy();
        dailyChartInstance = new Chart(dailyCtx, {
            type: 'bar',
            data: {
                labels: dailyData.labels,
                datasets: [{
                    label: 'Revenue (₹)',
                    data: dailyData.data,
                    backgroundColor: 'rgba(59, 130, 246, 0.6)',
                    borderColor: '#3b82f6',
                    borderWidth: 1.5,
                    borderRadius: 6
                }]
            },
            options: { responsive: true, plugins: { legend: { display: false } } }
        });

        // Render Vehicle Distribution Chart
        const distCtx = document.getElementById('vehicleDistributionChart').getContext('2d');
        if (distributionChartInstance) distributionChartInstance.destroy();
        distributionChartInstance = new Chart(distCtx, {
            type: 'doughnut',
            data: {
                labels: distData.labels,
                datasets: [{
                    data: distData.data,
                    backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
                }]
            },
            options: { responsive: true }
        });
    } catch (err) {
        console.error('Failed to load charts', err);
    }
}

// Visual Parking Slots Loader
async function loadParkingSlots() {
    try {
        const resp = await fetch('/api/parking-slots');
        const res = await resp.json();
        const container = document.getElementById('slotsContainer');

        if (res.success && res.data) {
            container.innerHTML = res.data.map(slot => `
                <div class="slot-card ${slot.status}">
                    <div class="slot-number">${slot.slotNumber}</div>
                    <div class="slot-type">${slot.vehicleTypeAllowed}</div>
                </div>
            `).join('');
        }
    } catch (err) {
        console.error('Failed to load parking slots', err);
    }
}

// Parking Rates Loader
async function loadParkingRates() {
    try {
        const resp = await fetch('/api/rates');
        const res = await resp.json();
        const tbody = document.getElementById('ratesTableBody');

        if (res.success && res.data) {
            tbody.innerHTML = res.data.map(r => `
                <tr>
                    <td><strong>${r.vehicleType}</strong></td>
                    <td>₹${r.ratePerHour} / hr</td>
                    <td><span class="badge badge-success">${r.status}</span></td>
                    <td>
                        <button class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="editRate('${r.vehicleType}', ${r.ratePerHour})">Edit Rate</button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (err) {
        console.error('Failed to load rates', err);
    }
}

async function editRate(type, currentRate) {
    const newRate = prompt('Enter new hourly rate for ' + type + ' (₹):', currentRate);
    if (!newRate || isNaN(newRate) || parseFloat(newRate) <= 0) return;

    try {
        const resp = await fetch('/api/rates/update', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ vehicleType: type, ratePerHour: parseFloat(newRate) })
        });
        const res = await resp.json();
        if (res.success) {
            alert('Rate updated successfully!');
            loadParkingRates();
        }
    } catch (err) {
        alert('Failed to update rate.');
    }
}

// Employee Management Loader
async function loadEmployees() {
    try {
        const resp = await fetch('/api/employees');
        const res = await resp.json();
        const tbody = document.getElementById('employeesTableBody');

        if (res.success && res.data) {
            tbody.innerHTML = res.data.map(u => `
                <tr>
                    <td>${u.id}</td>
                    <td><strong>${u.name}</strong></td>
                    <td>${u.email}</td>
                    <td><span class="badge badge-info">${u.role}</span></td>
                    <td><span class="badge badge-success">${u.status}</span></td>
                    <td>
                        ${u.role !== 'ADMIN' ? `<button class="btn btn-danger" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="deleteEmployee(${u.id})">Delete</button>` : '<em>Protected</em>'}
                    </td>
                </tr>
            `).join('');
        }
    } catch (err) {
        console.error('Failed to load employees', err);
    }
}

async function openAddEmployeeModal() {
    const name = prompt('Enter Employee Full Name:');
    if (!name) return;
    const email = prompt('Enter Employee Email:');
    if (!email) return;
    const password = prompt('Enter Employee Initial Password:', 'emp123');
    if (!password) return;

    try {
        const resp = await fetch('/api/employees', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role: 'EMPLOYEE', status: 'ACTIVE' })
        });
        const res = await resp.json();
        if (res.success) {
            alert('Employee created successfully!');
            loadEmployees();
        } else {
            alert('Error: ' + res.message);
        }
    } catch (err) {
        alert('Failed to create employee');
    }
}

async function deleteEmployee(id) {
    if (!confirm('Are you sure you want to delete this employee?')) return;
    try {
        const resp = await fetch('/api/employees/' + id, { method: 'DELETE' });
        const res = await resp.json();
        if (res.success) {
            alert('Employee deleted.');
            loadEmployees();
        }
    } catch (err) {
        alert('Error deleting employee.');
    }
}

// Audit Logs Loader
async function loadAuditLogs() {
    try {
        const resp = await fetch('/api/audit-logs');
        const res = await resp.json();
        const tbody = document.getElementById('auditLogsTableBody');

        if (res.success && res.data) {
            tbody.innerHTML = res.data.map(log => `
                <tr>
                    <td>${formatDate(log.timestamp)}</td>
                    <td>${log.userEmail || 'System'}</td>
                    <td><span class="badge badge-info">${log.action}</span></td>
                    <td>${log.description}</td>
                </tr>
            `).join('');
        }
    } catch (err) {
        console.error('Failed to load audit logs', err);
    }
}

// Utility Date Formatter
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: true });
}

// Slot Management Helpers (Slots view — requires ADMIN)
function openAddSlotModal() {
    const slotNumber = prompt('Enter new slot number (e.g. D01):');
    if (!slotNumber || !slotNumber.trim()) return;

    const vehicleType = prompt('Enter vehicle type allowed (CAR / BIKE / SUV / TRUCK / ALL):');
    if (!vehicleType || !vehicleType.trim()) return;

    fetch('/api/parking-slots', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ slotNumber: slotNumber.trim().toUpperCase(), vehicleTypeAllowed: vehicleType.trim().toUpperCase(), status: 'AVAILABLE' })
    })
    .then(r => r.json())
    .then(res => {
        if (res.success) {
            alert('Slot ' + slotNumber.toUpperCase() + ' created successfully!');
            loadParkingSlots();
        } else {
            alert('Failed to create slot: ' + (res.message || 'Unknown error'));
        }
    })
    .catch(err => {
        console.error('Error creating slot:', err);
        alert('Error creating slot: ' + (err.message || 'Network error'));
    });
}
