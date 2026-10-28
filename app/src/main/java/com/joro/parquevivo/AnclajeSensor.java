package com.joro.parquevivo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AnclajeSensor {

    public interface ListenerOrientacion {
        void onOrientacionCambiada(float[] matrizRotacion);
    }

    private final SensorManager sensorManager;
    private final Sensor sensorRotacion;
    private SensorEventListener sensorEventListener;

    public AnclajeSensor(Context context) {
        // Uso de getApplicationContext() para evitar fugas de memoria con el contexto de la Activity
        Context appCtx = (context != null) ? context.getApplicationContext() : context;
        if (appCtx != null) {
            sensorManager = (SensorManager) appCtx.getSystemService(Context.SENSOR_SERVICE);
        } else {
            sensorManager = null;
        }

        if (sensorManager != null) {
            sensorRotacion = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        } else {
            sensorRotacion = null;
        }
    }

    // Verifica si el dispositivo cuenta con sensor giroscópico / vector de rotación
    public boolean disponible() {
        return sensorRotacion != null;
    }

    // Inicia la lectura de los sensores
    public void iniciar(ListenerOrientacion listener) {
        if (!disponible() || listener == null) return;

        // Asegura desregistrar cualquier listener previo antes de crear uno nuevo
        detener();

        sensorEventListener = new SensorEventListener() {
            private final float[] matrizRotacion = new float[9];

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(matrizRotacion, event.values);
                    listener.onOrientacionCambiada(matrizRotacion);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(
                sensorEventListener,
                sensorRotacion,
                SensorManager.SENSOR_DELAY_GAME
        );
    }

    // Detiene el listener para ahorrar batería cuando la pantalla pasa a onPause
    public void detener() {
        if (sensorManager != null && sensorEventListener != null) {
            sensorManager.unregisterListener(sensorEventListener);
            sensorEventListener = null;
        }
    }
}