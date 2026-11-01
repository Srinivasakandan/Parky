package com.smartparking.anpr;

import com.smartparking.util.VehicleNumberValidator;
import jakarta.annotation.PostConstruct;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Primary
public class TesseractANPRService implements NumberPlateRecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(TesseractANPRService.class);

    @Autowired
    private AnprImageProcessor imageProcessor;

    private ITesseract tesseract;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            tesseract = new Tesseract();

            // Locate or extract tessdata directory
            String tessDataPath = resolveTessDataPath();
            logger.info("Initializing Tesseract OCR with datapath: {}", tessDataPath);

            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");

            // Whitelist uppercase English alphabet and digits
            tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
            // Set Page Segmentation Mode to single uniform block of text (PSM 6)
            tesseract.setPageSegMode(6);

            initialized = true;
            logger.info("Tesseract ANPR engine initialized successfully.");
        } catch (Exception e) {
            logger.warn("Could not initialize native Tesseract OCR engine ({}). Fallback OCR mode active.", e.getMessage());
            initialized = false;
        }
    }

    private String resolveTessDataPath() {
        // 1. Check local directory in source tree
        Path localPath = Paths.get("src", "main", "resources", "tessdata");
        if (Files.exists(localPath) && Files.exists(localPath.resolve("eng.traineddata"))) {
            return localPath.toAbsolutePath().toString();
        }

        // 2. Check current working directory tessdata
        Path cwdPath = Paths.get("tessdata");
        if (Files.exists(cwdPath) && Files.exists(cwdPath.resolve("eng.traineddata"))) {
            return cwdPath.toAbsolutePath().toString();
        }

        // 3. Extract eng.traineddata from classpath to temp directory
        try {
            ClassPathResource resource = new ClassPathResource("tessdata/eng.traineddata");
            if (resource.exists()) {
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "parky_tessdata");
                if (!tempDir.exists()) tempDir.mkdirs();
                File targetFile = new File(tempDir, "eng.traineddata");
                if (!targetFile.exists() || targetFile.length() == 0) {
                    try (InputStream in = resource.getInputStream();
                         FileOutputStream out = new FileOutputStream(targetFile)) {
                        in.transferTo(out);
                    }
                }
                return tempDir.getAbsolutePath();
            }
        } catch (Exception ex) {
            logger.warn("Failed to extract tessdata from classpath: {}", ex.getMessage());
        }

        return localPath.toAbsolutePath().toString();
    }

    @Override
    public ANPRResult recognize(String input) {
        String cleaned = VehicleNumberValidator.clean(input);
        VehicleNumberValidator.ValidationResult val = VehicleNumberValidator.validate(cleaned);
        String confidence = val.isValid() ? "95%" : (cleaned.length() >= 4 ? "60%" : "20%");
        String status = val.isValid() ? "SUCCESS" : (cleaned.isEmpty() ? "FAILED" : "PARTIAL");

        return new ANPRResult(cleaned, confidence, status, "MANUAL_INPUT",
                val.isValid(), val.getFormatType(), val.getMessage());
    }

    @Override
    public ANPRResult recognizeImage(byte[] imageBytes) {
        if (!initialized || tesseract == null) {
            return new ANPRResult("", "0%", "FAILED", "OCR_UNAVAILABLE",
                    false, "NONE", "OCR Engine is not initialized on this host. Please use manual entry.");
        }

        try {
            AnprImageProcessor.ProcessedPlateImage processed = imageProcessor.processImage(imageBytes);
            List<BufferedImage> candidateImages = processed.getCandidatePlates();

            String bestRawNumber = "";
            int bestConfidence = 0;
            VehicleNumberValidator.ValidationResult bestValidation = null;

            for (BufferedImage img : candidateImages) {
                try {
                    String ocrText = tesseract.doOCR(img);
                    if (ocrText == null || ocrText.trim().isEmpty()) continue;

                    String cleaned = VehicleNumberValidator.clean(ocrText);
                    if (cleaned.length() < 4) continue;

                    VehicleNumberValidator.ValidationResult val = VehicleNumberValidator.validate(cleaned);

                    // If a valid Indian registration format is detected, accept with high confidence
                    if (val.isValid()) {
                        return new ANPRResult(cleaned, "96%", "SUCCESS", "CAMERA_OCR",
                                true, val.getFormatType(), val.getMessage());
                    }

                    // Otherwise keep track of longest/closest candidate
                    if (cleaned.length() > bestRawNumber.length()) {
                        bestRawNumber = cleaned;
                        bestValidation = val;
                        bestConfidence = Math.min(85, 30 + cleaned.length() * 6);
                    }
                } catch (TesseractException te) {
                    logger.debug("Candidate OCR attempt: {}", te.getMessage());
                }
            }

            if (!bestRawNumber.isEmpty()) {
                String confStr = bestConfidence + "%";
                boolean isValid = bestValidation != null && bestValidation.isValid();
                String msg = bestValidation != null ? bestValidation.getMessage() : "Recognized with partial accuracy. Please review.";
                String fmt = bestValidation != null ? bestValidation.getFormatType() : "UNCERTAIN";

                return new ANPRResult(bestRawNumber, confStr, "PARTIAL", "CAMERA_OCR",
                        isValid, fmt, msg);
            }

            return new ANPRResult("", "0%", "FAILED", "CAMERA_OCR",
                    false, "NONE", "Could not clearly detect characters on the plate. Please adjust camera angle or enter manually.");

        } catch (Exception e) {
            logger.error("Error processing image for ANPR: {}", e.getMessage(), e);
            return new ANPRResult("", "0%", "FAILED", "CAMERA_OCR",
                    false, "NONE", "Recognition failed: " + e.getMessage());
        }
    }
}

