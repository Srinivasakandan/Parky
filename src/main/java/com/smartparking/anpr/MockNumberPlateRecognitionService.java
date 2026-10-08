package com.smartparking.anpr;

import org.springframework.stereotype.Service;

@Service
public class MockNumberPlateRecognitionService implements NumberPlateRecognitionService {

    @Override
    public ANPRResult recognize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ANPRResult("", "0%", "FAILED", "MOCK");
        }

        String cleaned = input.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (cleaned.length() < 4) {
            return new ANPRResult(cleaned, "45%", "FAILED", "MOCK");
        }

        // Mock recognition result with 98% confidence
        return new ANPRResult(cleaned, "98%", "SUCCESS", "MOCK");
    }
}
