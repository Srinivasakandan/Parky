package com.smartparking.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates Indian vehicle registration number formats using Java Regex.
 * Supports standard state/UT registrations and Bharat Series (BH) registrations.
 *
 * NOTE: Regex validation verifies format compliance only and does not perform
 * official government VAHAN verification.
 */
public class VehicleNumberValidator {

    // Valid 2-letter State & Union Territory codes in India
    private static final Set<String> VALID_STATE_CODES = new HashSet<>(Arrays.asList(
            "AN", "AP", "AR", "AS", "BR", "CG", "CH", "DD", "DL", "DN",
            "GA", "GJ", "HP", "HR", "JH", "JK", "KA", "KL", "LA", "LD",
            "MH", "ML", "MN", "MP", "MZ", "NL", "OD", "PB", "PY", "RJ",
            "SK", "TN", "TR", "TS", "UK", "UP", "WB"
    ));

    // Standard Format: e.g. TN01AB1234, DL01A1234, MH121234
    // State (2 letters) + RTO (1-2 digits) + Series (0-3 letters) + Number (1-4 digits)
    private static final Pattern STANDARD_PATTERN = Pattern.compile("^([A-Z]{2})(\\d{1,2})([A-Z]{0,3})(\\d{1,4})$");

    // Bharat Series (BH) Format: e.g. 21BH1234AA, 22BH9999A
    // Year of registration (2 digits) + BH + 4-digit number + 1-2 letters
    private static final Pattern BHARAT_SERIES_PATTERN = Pattern.compile("^(\\d{2})BH(\\d{4})([A-Z]{1,2})$");

    public static class ValidationResult {
        private final boolean valid;
        private final String normalizedNumber;
        private final String formatType;
        private final String message;

        public ValidationResult(boolean valid, String normalizedNumber, String formatType, String message) {
            this.valid = valid;
            this.normalizedNumber = normalizedNumber;
            this.formatType = formatType;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getNormalizedNumber() { return normalizedNumber; }
        public String getFormatType() { return formatType; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "ValidationResult{valid=" + valid + ", normalized='" + normalizedNumber + '\'' +
                    ", format='" + formatType + '\'' + ", message='" + message + "'}";
        }
    }

    /**
     * Cleans raw input by removing whitespace, hyphens, special characters and converting to uppercase.
     */
    public static String clean(String input) {
        if (input == null) return "";
        return input.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    /**
     * Comprehensive validation of an Indian vehicle number.
     */
    public static ValidationResult validate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ValidationResult(false, "", "UNKNOWN", "Vehicle registration number cannot be empty.");
        }

        String cleaned = clean(input);

        if (cleaned.length() < 6 || cleaned.length() > 11) {
            return new ValidationResult(false, cleaned, "UNKNOWN",
                    "Invalid registration length (" + cleaned.length() + " chars). Standard Indian numbers are between 6 and 11 characters.");
        }

        // Check Bharat Series (BH) first: YY BH #### XX
        Matcher bhMatcher = BHARAT_SERIES_PATTERN.matcher(cleaned);
        if (bhMatcher.matches()) {
            int year = Integer.parseInt(bhMatcher.group(1));
            if (year < 21 || year > 99) {
                return new ValidationResult(false, cleaned, "BH_SERIES", "Invalid registration year in Bharat Series number.");
            }
            return new ValidationResult(true, cleaned, "BHARAT_SERIES", "Valid Bharat Series (BH) registration format.");
        }

        // Check Standard State/UT format
        Matcher standardMatcher = STANDARD_PATTERN.matcher(cleaned);
        if (standardMatcher.matches()) {
            String stateCode = standardMatcher.group(1);
            if (!VALID_STATE_CODES.contains(stateCode)) {
                return new ValidationResult(false, cleaned, "STANDARD",
                        "Invalid Indian State/UT code: '" + stateCode + "'. Must be a recognized State or UT code (e.g. TN, DL, MH, KA).");
            }

            int rtoNum = Integer.parseInt(standardMatcher.group(2));
            if (rtoNum < 1 || rtoNum > 99) {
                return new ValidationResult(false, cleaned, "STANDARD", "Invalid RTO district code.");
            }

            return new ValidationResult(true, cleaned, "STANDARD_STATE",
                    "Valid standard Indian registration format (" + stateCode + ").");
        }

        return new ValidationResult(false, cleaned, "INVALID",
                "Number '" + cleaned + "' does not match standard Indian (e.g. TN07AB1234) or Bharat Series (e.g. 22BH1234AA) format.");
    }

    /**
     * Convenient boolean check.
     */
    public static boolean isValid(String input) {
        return validate(input).isValid();
    }
}

