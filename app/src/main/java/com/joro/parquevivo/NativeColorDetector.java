package com.joro.parquevivo;

import android.graphics.Bitmap;
import android.graphics.Color;
import java.util.Locale;

/**
 * Clase encargada de examinar los píxeles de los fotogramas de la cámara
 * y determinar si coinciden con los rangos HSV solicitados por Drako.
 */
public class NativeColorDetector {

    // Variable para inspección y depuración en tiempo real (volatile para acceso entre hilos)
    public static volatile String ultimoHsv = "";

    /**
     * Evalúa si el área central del Bitmap contiene una concentración suficiente del color objetivo.
     */
    public static boolean isColorInCenter(Bitmap bitmap, float[] lowerHsv, float[] upperHsv) {
        // Validación de seguridad contra fotogramas nulos o desconfigurados
        if (bitmap == null || lowerHsv == null || upperHsv == null) return false;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int centerX = width / 2;
        int centerY = height / 2;

        // Tamaño del área de muestreo alrededor del centro (40x40 píxeles)
        int sampleSize = 20;

        int matchingPixels = 0;
        int totalPixelsTested = 0;

        float[] pixelHsv = new float[3];

        // Muestreo del píxel central exacto para depuración
        Color.colorToHSV(bitmap.getPixel(centerX, centerY), pixelHsv);
        ultimoHsv = String.format(Locale.US, "H=%.0f S=%.2f V=%.2f", pixelHsv[0], pixelHsv[1], pixelHsv[2]);

        // Recorrido de la Región de Interés (ROI) con salto de 4 píxeles para optimizar la CPU
        for (int x = centerX - sampleSize; x < centerX + sampleSize; x += 4) {
            for (int y = centerY - sampleSize; y < centerY + sampleSize; y += 4) {

                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int pixel = bitmap.getPixel(x, y);
                    Color.colorToHSV(pixel, pixelHsv);

                    if (isHsvInRange(pixelHsv, lowerHsv, upperHsv)) {
                        matchingPixels++;
                    }
                    totalPixelsTested++;
                }
            }
        }

        // Umbral de éxito: más del 40% de coincidencia en los píxeles evaluados
        return totalPixelsTested > 0 && ((float) matchingPixels / totalPixelsTested) > 0.4f;
    }

    /**
     * Calcula las coordenadas normalizadas (0.0 a 1.0) del centro de masa del color detectado en el cuadro.
     */
    public static float[] findColorCenter(Bitmap bitmap, float[] lowerHsv, float[] upperHsv) {
        if (bitmap == null || lowerHsv == null || upperHsv == null) return null;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float[] hsv = new float[3];
        long sumX = 0;
        long sumY = 0;
        int n = 0;

        for (int x = 0; x < w; x += 8) {
            for (int y = 0; y < h; y += 8) {
                Color.colorToHSV(bitmap.getPixel(x, y), hsv);
                if (isHsvInRange(hsv, lowerHsv, upperHsv)) {
                    sumX += x;
                    sumY += y;
                    n++;
                }
            }
        }

        if (n < 10) return null;
        return new float[]{ (float) sumX / n / w, (float) sumY / n / h };
    }

    /**
     * Verifica si las componentes HSV de un píxel caen dentro del rango inferior y superior.
     */
    private static boolean isHsvInRange(float[] hsv, float[] lower, float[] upper) {
        boolean hueMatch;

        // Manejo especial para el tono Rojo (Hue), el cual cruza el origen (350° a 10°)
        if (lower[0] > upper[0]) {
            hueMatch = (hsv[0] >= lower[0] || hsv[0] <= upper[0]);
        } else {
            hueMatch = (hsv[0] >= lower[0] && hsv[0] <= upper[0]);
        }

        boolean satMatch = (hsv[1] >= lower[1] && hsv[1] <= upper[1]);
        boolean valMatch = (hsv[2] >= lower[2] && hsv[2] <= upper[2]);

        return hueMatch && satMatch && valMatch;
    }
}