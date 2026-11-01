package com.smartparking.anpr;

public interface NumberPlateRecognitionService {
    ANPRResult recognize(String input);

    default ANPRResult recognizeImage(byte[] imageBytes) {
        return recognize("");
    }
}
