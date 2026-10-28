package com.joro.parquevivo;

import android.graphics.Bitmap;

public class ColorHuntController {

    // Interfaz Callback: Permite notificar a la Activity el progreso del escaneo y la confirmación
    public interface ColorDetectionListener {
        void onProgressUpdate(int progress);
        void onColorVerified();
    }

    private float[] centroColor;
    private float[] currentLowerHsv;
    private float[] currentUpperHsv;
    private boolean isScanningActive = false;
    private ColorDetectionListener listener;

    private int currentProgress = 0;
    private static final int MAX_PROGRESS = 100;
    private static final int INCREMENT = 20; // Avance por cuadro detectado
    private static final int DECREMENT = 10; // Reducción si se pierde la toma

    public float[] getCentroColor() {
        return centroColor;
    }

    public void setTargetColor(float[] lower, float[] upper, ColorDetectionListener listener) {
        this.currentLowerHsv = lower;
        this.currentUpperHsv = upper;
        this.listener = listener;
        resetProgress();
    }

    public void resetProgress() {
        this.currentProgress = 0;
    }

    public void setScanningActive(boolean active) {
        this.isScanningActive = active;
        if (!active) {
            resetProgress();
        }
    }

    public boolean isScanningActive() {
        return isScanningActive;
    }

    public void processFrameBitmap(Bitmap bitmap) {
        if (!isScanningActive || currentLowerHsv == null || currentUpperHsv == null || bitmap == null) {
            return;
        }

        // Evalúa con la clase nativa si el color está en el centro
        boolean detected = NativeColorDetector.isColorInCenter(bitmap, currentLowerHsv, currentUpperHsv);

        if (detected) {
            currentProgress = Math.min(MAX_PROGRESS, currentProgress + INCREMENT);
        } else {
            // CORREGIDO: Se añadió el operador de resta (-)
            currentProgress = Math.max(0, currentProgress - DECREMENT);
        }

        if (listener != null) {
            listener.onProgressUpdate(currentProgress);

            if (currentProgress >= MAX_PROGRESS) {
                // Capturar las coordenadas del centro del color justo antes de notificar a la UI
                centroColor = NativeColorDetector.findColorCenter(bitmap, currentLowerHsv, currentUpperHsv);
                isScanningActive = false; // Detener escaneo al completar
                listener.onColorVerified();
            }
        }
    }
}