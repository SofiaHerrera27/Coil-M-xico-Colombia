package com.joro.parquevivo;

import android.graphics.Bitmap;

public class ColorHuntController {

    // Interfaz Callback: Permite notificar a la Activity cuando se valida un color sin acoplar las clases
    public interface ColorDetectionListener {
        void onColorDetected();
    }

    // Variables de estado del controlador
    private float[] currentLowerHsv;           // Rango inferior activo enviado por el acertijo
    private float[] currentUpperHsv;           // Rango superior activo enviado por el acertijo
    private boolean isScanningActive = false; // Flag para activar/pausar la CPU de procesamiento
    private ColorDetectionListener listener;  // Referencia al receptor de eventos de éxito

    // Asigna el color que se debe buscar en el acertijo actual
    public void setTargetColor(float[] lower, float[] upper, ColorDetectionListener listener) {
        this.currentLowerHsv = lower;
        this.currentUpperHsv = upper;
        this.listener = listener;
    }

    // Controla el interruptor de escaneo
    public void setScanningActive(boolean active) {
        this.isScanningActive = active;
    }

    // Consulta el estado actual de la búsqueda
    public boolean isScanningActive() {
        return isScanningActive;
    }

    // Procesa el fotograma entregado por CameraX
    public void processFrameBitmap(Bitmap bitmap) {
        //verificacion:
        // Cláusula de guarda: Si el juego está en pausas o no hay color asignado, cancela el análisis inmediatamente
        if (!isScanningActive || currentLowerHsv == null || currentUpperHsv == null) {
            return;
        }

        // Llama al detector de color nativo
        boolean detected = NativeColorDetector.isColorInCenter(bitmap, currentLowerHsv, currentUpperHsv);

        // Si se encuentra el color objetivo, dispara el evento hacia la Activity
        if (detected && listener != null) {
            listener.onColorDetected();
        }
    }
}