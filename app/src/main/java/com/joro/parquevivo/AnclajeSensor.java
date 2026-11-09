package com.joro.parquevivo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.appcompat.app.AlertDialog;
import com.google.ar.core.ArCoreApk;

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

    /**
     * Valida tanto la disponibilidad del sensor físico como el soporte de ARCore de Google.
     */
    public static boolean esCompatibleConRA(Context context) {
        AnclajeSensor sensorCheck = new AnclajeSensor(context);
        if (!sensorCheck.disponible()) {
            return false;
        }
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(context);
        return availability.isSupported();
    }

    /**
     * Despliega la alerta de incompatibilidad.
     */
    public static void mostrarAvisoIncompatibilidad(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Función de RA No Disponible")
                .setMessage("Tu dispositivo no cuenta con giroscopio o los servicios de ARCore requeridos para mostrar modelos 3D en Realidad Aumentada.")
                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
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