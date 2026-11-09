package com.joro.parquevivo;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ColorHuntActivity extends AppCompatActivity implements ColorHuntController.ColorDetectionListener {

    private ColorHuntController controller;
    private TextView tvInstrucciones;
    private ImageView imgCabezaDragon;
    private View reticulaEscaner;

    private Handler handlerJuego = new Handler(Looper.getMainLooper());
    private Runnable runnableAparicion;
    private Runnable runnableTiempoAgotado;

    private boolean esperandoToque = false;
    private String nombreColor = "Rojo";

    // Valores de rango HSV de ejemplo (Ajustar según el color objetivo)
    private final float[] lowerHsvRojo = new float[]{0f, 120f, 70f};
    private final float[] upperHsvRojo = new float[]{10f, 255f, 255f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_hunt);

        tvInstrucciones = findViewById(R.id.tvInstrucciones);
        imgCabezaDragon = findViewById(R.id.imgCabezaDragon);
        reticulaEscaner = findViewById(R.id.reticulaEscaner);

        ImageButton btnCerrar = findViewById(R.id.btnCerrarColorHunt);
        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> finish());

        // Inicializar controlador
        controller = new ColorHuntController();
        controller.setTargetColor(lowerHsvRojo, upperHsvRojo, this);
        controller.setScanningActive(true);

        // Listener para atrapar a la cabecita cuando asoma
        imgCabezaDragon.setOnClickListener(v -> {
            if (esperandoToque) {
                completarCapturaExitosa();
            }
        });
    }

    /**
     * Simulación de recepción de cuadro de la cámara CameraX / SurfaceView.
     * En tu analizador de imágenes de CameraX debes llamar a esta función pasándole el Bitmap del frame.
     */
    public void recibirFrameDeCamara(Bitmap bitmapFrame) {
        if (controller != null && controller.isScanningActive()) {
            controller.processFrameBitmap(bitmapFrame);
        }
    }

    // --- MÉTODOS DEL LISTENER (ColorDetectionListener) ---

    @Override
    public void onProgressUpdate(int progress) {
        runOnUiThread(() -> {
            // Actualizar la opacidad o escala de la retícula según avance el escaneo
            float alpha = 0.3f + ((float) progress / 100f) * 0.7f;
            if (reticulaEscaner != null) reticulaEscaner.setAlpha(alpha);
            tvInstrucciones.setText("Escaneando " + nombreColor + "... " + progress + "%");
        });
    }

    @Override
    public void onColorVerified() {
        runOnUiThread(this::iniciarSecuenciaEscape);
    }

    // --- LÓGICA DE JUEGO (Whack-a-Mole) ---

    private void iniciarSecuenciaEscape() {
        MusicaManager.reproducirEfecto(R.raw.sfx_moneda);
        tvInstrucciones.setText("¡Oh no! Drako " + nombreColor + " sintió el escáner y se escapó \n¡Sigue enfocando el color " + nombreColor + "!");

        // Generar un tiempo de espera aleatorio entre 4 y 9 segundos
        int tiempoEsperaRandom = (new Random().nextInt(6) + 4) * 1000;

        runnableAparicion = this::mostrarCabecitaEmergente;
        handlerJuego.postDelayed(runnableAparicion, tiempoEsperaRandom);
    }

    private void mostrarCabecitaEmergente() {
        esperandoToque = true;
        tvInstrucciones.setText("⚡ ¡AHÍ ESTÁ! ¡TÓCALO RÁPIDO! ⚡");

        imgCabezaDragon.setVisibility(View.VISIBLE);
        imgCabezaDragon.setScaleX(0.2f);
        imgCabezaDragon.setScaleY(0.2f);

        // Animación de aparición (Pop-up con Overshoot)
        ObjectAnimator animX = ObjectAnimator.ofFloat(imgCabezaDragon, View.SCALE_X, 0.2f, 1.0f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(imgCabezaDragon, View.SCALE_Y, 0.2f, 1.0f);
        animX.setDuration(400);
        animY.setDuration(400);
        animX.setInterpolator(new OvershootInterpolator());
        animY.setInterpolator(new OvershootInterpolator());
        animX.start();
        animY.start();

        // 2.5 segundos para que el jugador reaccione y lo toque
        runnableTiempoAgotado = () -> {
            if (esperandoToque) {
                esperandoToque = false;
                imgCabezaDragon.setVisibility(View.GONE);
                tvInstrucciones.setText("¡Se volvió a esconder! \nSigue buscando...");

                // Reiniciar espera aleatoria
                int nuevoTiempoRandom = (new Random().nextInt(5) + 3) * 1000;
                handlerJuego.postDelayed(runnableAparicion, nuevoTiempoRandom);
            }
        };

        handlerJuego.postDelayed(runnableTiempoAgotado, 2500);
    }

    private void completarCapturaExitosa() {
        esperandoToque = false;
        handlerJuego.removeCallbacks(runnableTiempoAgotado);
        handlerJuego.removeCallbacks(runnableAparicion);

        MusicaManager.reproducirEfecto(R.raw.sfx_moneda);
        imgCabezaDragon.setVisibility(View.GONE);
        tvInstrucciones.setText("¡ESPECTACULAR! ¡Capturaste a Drako " + nombreColor + "!");

        // Desbloquear en la base de datos
        DatabaseHelper db = new DatabaseHelper(this);
        db.desbloquearPremio(2); // Ejemplo ID Guardián Rojo

        Toast.makeText(this, " ¡Personaje capturado!", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handlerJuego != null) {
            handlerJuego.removeCallbacksAndMessages(null);
        }
    }
}