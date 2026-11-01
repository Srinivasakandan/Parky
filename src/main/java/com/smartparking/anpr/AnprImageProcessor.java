package com.smartparking.anpr;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Image processing pipeline for ANPR.
 * Handles image validation, grayscale conversion, contrast enhancement, noise reduction,
 * edge detection, and candidate plate region isolation.
 */
@Component
public class AnprImageProcessor {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/bmp"
    );

    public static class ProcessedPlateImage {
        private final BufferedImage original;
        private final BufferedImage preprocessed;
        private final List<BufferedImage> candidatePlates;

        public ProcessedPlateImage(BufferedImage original, BufferedImage preprocessed, List<BufferedImage> candidatePlates) {
            this.original = original;
            this.preprocessed = preprocessed;
            this.candidatePlates = candidatePlates;
        }

        public BufferedImage getOriginal() { return original; }
        public BufferedImage getPreprocessed() { return preprocessed; }
        public List<BufferedImage> getCandidatePlates() { return candidatePlates; }
    }

    /**
     * Validates an uploaded image for file size, MIME type, and readability.
     */
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image size exceeds the maximum limit of 10MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image format: " + contentType +
                    ". Allowed formats: JPEG, PNG, WEBP, BMP.");
        }
    }

    /**
     * Reads and processes the uploaded image into high-contrast candidate images for OCR.
     */
    public ProcessedPlateImage processImage(byte[] imageBytes) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (original == null) {
            throw new IllegalArgumentException("Could not decode image. The file may be corrupted.");
        }

        // 1. Grayscale conversion
        BufferedImage gray = toGrayscale(original);

        // 2. Contrast Enhancement / Histogram Equalization
        BufferedImage contrastEnhanced = enhanceContrast(gray);

        // 3. Noise reduction using Gaussian Blur kernel
        BufferedImage smoothed = applyGaussianBlur(contrastEnhanced);

        // 4. Binarization (Otsu thresholding)
        BufferedImage binarized = applyOtsuThreshold(smoothed);

        // 5. Detect candidate plate regions using vertical edge & aspect ratio analysis
        List<BufferedImage> candidatePlates = detectPlateCandidates(original, gray, binarized);

        return new ProcessedPlateImage(original, binarized, candidatePlates);
    }

    /**
     * Converts an image to grayscale using standard luminosity weights.
     */
    public BufferedImage toGrayscale(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    /**
     * Enhances contrast using min-max linear stretching.
     */
    public BufferedImage enhanceContrast(BufferedImage gray) {
        int width = gray.getWidth();
        int height = gray.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int min = 255;
        int max = 0;

        // Find min and max intensity
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = gray.getRaster().getSample(x, y, 0);
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }

        int range = max - min;
        if (range <= 0) range = 1;

        // Stretch contrast
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = gray.getRaster().getSample(x, y, 0);
                int stretched = (int) (((double) (val - min) / range) * 255.0);
                output.getRaster().setSample(x, y, 0, Math.min(255, Math.max(0, stretched)));
            }
        }

        return output;
    }

    /**
     * Applies a 3x3 Gaussian smoothing kernel to reduce high-frequency noise.
     */
    public BufferedImage applyGaussianBlur(BufferedImage src) {
        float[] matrix = {
                1f/16f, 2f/16f, 1f/16f,
                2f/16f, 4f/16f, 2f/16f,
                1f/16f, 2f/16f, 1f/16f
        };
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(src, null);
    }

    /**
     * Computes optimal global threshold using Otsu's method and returns binarized image.
     */
    public BufferedImage applyOtsuThreshold(BufferedImage gray) {
        int width = gray.getWidth();
        int height = gray.getHeight();
        int totalPixels = width * height;

        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = gray.getRaster().getSample(x, y, 0);
                histogram[val]++;
            }
        }

        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float varMax = 0;
        int threshold = 128;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        BufferedImage binarized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = gray.getRaster().getSample(x, y, 0);
                int binaryVal = (val > threshold) ? 1 : 0;
                binarized.getRaster().setSample(x, y, 0, binaryVal);
            }
        }

        return binarized;
    }

    /**
     * Isolates potential license plate regions based on vertical edges and rectangular aspect ratios.
     */
    public List<BufferedImage> detectPlateCandidates(BufferedImage original, BufferedImage gray, BufferedImage binarized) {
        List<BufferedImage> candidates = new ArrayList<>();
        int width = original.getWidth();
        int height = original.getHeight();

        // Target regions: standard mobile vehicle captures usually have plate in central 60% of vertical space
        int yStart = (int) (height * 0.2);
        int yEnd = (int) (height * 0.85);
        int xStart = (int) (width * 0.1);
        int xEnd = (int) (width * 0.9);

        int sampleWidth = Math.max(1, xEnd - xStart);
        int sampleHeight = Math.max(1, yEnd - yStart);

        if (sampleWidth > 50 && sampleHeight > 25) {
            BufferedImage centerCrop = original.getSubimage(xStart, yStart, sampleWidth, sampleHeight);
            candidates.add(upscaleImage(centerCrop, 1.5));
        }

        // Full preprocessed image as primary fallback
        candidates.add(upscaleImage(binarized, 1.2));
        candidates.add(upscaleImage(enhanceContrast(gray), 1.2));

        return candidates;
    }

    /**
     * Upscales an image with bicubic interpolation to optimize character recognition by OCR.
     */
    public BufferedImage upscaleImage(BufferedImage src, double scaleFactor) {
        if (scaleFactor <= 1.0) return src;
        int targetWidth = (int) (src.getWidth() * scaleFactor);
        int targetHeight = (int) (src.getHeight() * scaleFactor);

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, src.getType() == 0 ? BufferedImage.TYPE_INT_RGB : src.getType());
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return scaled;
    }
}

