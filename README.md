# Smart Parking Management System

A complete, production-ready, college-level **Smart Parking Management System** built with **Java 21, Spring Boot 3, Spring Data JPA, Spring Security, Hibernate, MySQL, HTML5, CSS3, JavaScript, and Chart.js**.

---

## 📌 Features & Business Rules

1. **Manual Vehicle Entry**:
   - Employee enters **Vehicle Number** (e.g. `TN01AB1234`) and selects **Vehicle Type** (`Bike`, `Car`, `SUV`, `Truck`).
   - Format validation ensures valid plate numbers (4-15 alphanumeric characters).
2. **Automatic Time Recording**: Server automatically generates entry/exit timestamps (`LocalDateTime`). No manual time entry required.
3. **Vehicle Entry & Slot Assignment**: Auto-assigns available parking slots based on vehicle type allowed (`BIKE`, `CAR`, `SUV`, `TRUCK`). Automatically marks slot status as `OCCUPIED`.
4. **Duplicate Parking Prevention**: Prevents the same vehicle from having multiple active parking records (`VehicleAlreadyParkedException`).
5. **Vehicle Exit & Fee Calculation**: Calculates parking duration automatically using `Duration`. Fetches configurable rates from DB (`parking_rates` table) and computes fee based on minimum 1 hour / ceiling hourly billing.
6. **Slot Release & Revenue Tracking**: Frees parking slots (`OCCUPIED` → `AVAILABLE`), updates daily/weekly/monthly revenue stats, and generates payment transaction records.
7. **Digital Printable Receipt**: Generates a printable receipt (`window.print()`) with transaction ID, duration, rate, fee, and payment method.
8. **Customer Vehicle Search**: Public portal allowing vehicle owners to enter their vehicle number (e.g., `TN01AB1234`) to check live status, slot location, entry time, duration, and fee.
9. **Role-Based Access Control**:
   - **ADMIN**: Access to KPIs, Chart.js analytics, slot management, rate updates, employee management, and audit logs.
   - **EMPLOYEE**: Vehicle entry, vehicle exit, bill generation, slot grid view, customer search.
   - **CUSTOMER**: Public vehicle lookup & receipt viewing (no password required).
10. **Audit Trail**: Logs all critical operations (`VEHICLE_ENTRY`, `VEHICLE_EXIT`, `PAYMENT`, `SLOT_UPDATE`, `RATE_UPDATE`, `LOGIN`).

---

## 📐 Technology Stack

- **Backend**: Java 21, Spring Boot 3.2.5, Spring Data JPA, Hibernate, Spring Security, Maven.
- **Database**: MySQL 8.0 (Primary) / H2 Embedded (Fallback/Dev).
- **Frontend**: HTML5, CSS3 (Glassmorphism theme, dark mode, badges), Vanilla JavaScript, Chart.js, Font Awesome.

---

## 🔮 Future ANPR Architecture Extension

The parking business logic is completely decoupled from plate capture using the `NumberPlateRecognitionService` interface.

Current workflow:
```text
Employee enters Vehicle Number + Vehicle Type
        ↓
Vehicle Validation
        ↓
Parking Slot Allocation
        ↓
Parking Record
        ↓
Billing / Payment
```

In the future, camera/OCR can replace the manual vehicle number input seamlessly without modifying core business logic:
```text
Camera Hardware / ANPR
        ↓
Plate Detection (OpenCV / YOLO)
        ↓
OCR (Tesseract / EasyOCR)
        ↓
NumberPlateRecognitionService
        ↓
Existing Parking Entry Logic
```

---

## 🚀 Setup & Running Instructions

### Prerequisites:
- Java JDK 21+
- Apache Maven 3.8+
- MySQL 8.0+

### Option A: Running with MySQL (Default)
1. Create MySQL database:
   ```sql
   CREATE DATABASE smart_parking_db;
   ```
2. Configure credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=root
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Option B: Running with Standalone H2 Database (No MySQL Setup Required)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Open browser at: [http://localhost:8080](http://localhost:8080)

---

## 🔑 Pre-seeded Sample Logins

- **Admin Account**: `admin@parking.com` / `admin123`
- **Employee Account**: `employee@parking.com` / `emp123`

---

## 🧪 REST API Endpoints

- `POST /api/auth/login` - User login
- `POST /api/vehicles/entry` - Register vehicle entry & assign slot
- `POST /api/vehicles/exit` - Process vehicle exit, calculate fee & release slot
- `GET /api/vehicles/parked` - List currently parked vehicles
- `GET /api/vehicles/exited` - List exited vehicles history
- `GET /api/vehicles/{vehicleNumber}` - Search vehicle details
- `GET /api/customer/search/{vehicleNumber}` - Customer vehicle lookup
- `GET /api/parking-slots` - List all parking slots
- `GET /api/dashboard` - Fetch KPI statistics & analytics data
- `GET /api/rates` - Fetch parking rates per vehicle type
- `PUT /api/rates/update` - Update parking rate (Admin only)
- `GET /api/employees` - List staff members (Admin only)
- `GET /api/audit-logs` - System audit log history (Admin only)
