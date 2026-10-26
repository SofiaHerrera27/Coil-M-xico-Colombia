package com.joro.parquevivo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ParquePuntos extends AppCompatActivity {

    private TextView tvPuntos;
    private TextView tvNivel;
    private TextView tvProgreso;
    private ProgressBar progresoNivel;

    private DrakoHelper drakoHelper;

    private static final String NOMBRE_PREFERENCIAS = "parque_puntos";
    private static final String CLAVE_PUNTOS = "puntos";
    private static final int PUNTOS_POR_NIVEL = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parque_puntos);

        tvPuntos = findViewById(R.id.tvPuntos);
        tvNivel = findViewById(R.id.tvNivel);
        tvProgreso = findViewById(R.id.tvProgreso);
        progresoNivel = findViewById(R.id.progresoNivel);

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        // Inicializa Drako si el diseño flotante está disponible.
        View contenedorDrako = findViewById(R.id.layout_drako_container);

        if (contenedorDrako != null) {
            ImageView imgDrako = contenedorDrako.findViewById(R.id.imgDrako);
            TextView tvGlobo = contenedorDrako.findViewById(R.id.tvGlobo);

            if (imgDrako != null && tvGlobo != null) {
                drakoHelper = new DrakoHelper(
                        this,
                        imgDrako,
                        tvGlobo,
                        DrakoHelper.TipoPantalla.INICIO
                );
            }
        }

        actualizarInterfaz();
    }

    // Añade puntos a los que el jugador ya tiene guardados.
    public static void agregarPuntos(Context context, int cantidad) {
        if (cantidad <= 0) {
            return;
        }

        SharedPreferences preferencias = context.getSharedPreferences(
                NOMBRE_PREFERENCIAS,
                Context.MODE_PRIVATE
        );

        int puntosActuales = preferencias.getInt(CLAVE_PUNTOS, 0);
        int nuevosPuntos = puntosActuales + cantidad;

        preferencias.edit()
                .putInt(CLAVE_PUNTOS, nuevosPuntos)
                .apply();
    }

    // Consulta los puntos guardados.
    public static int obtenerPuntos(Context context) {
        SharedPreferences preferencias = context.getSharedPreferences(
                NOMBRE_PREFERENCIAS,
                Context.MODE_PRIVATE
        );

        return preferencias.getInt(CLAVE_PUNTOS, 0);
    }

    // Actualiza los puntos, el nivel y el progreso de la pantalla.
    private void actualizarInterfaz() {
        int puntos = obtenerPuntos(this);

        int nivel = (puntos / PUNTOS_POR_NIVEL) + 1;
        int puntosNivel = puntos % PUNTOS_POR_NIVEL;

        tvPuntos.setText(String.valueOf(puntos));
        tvNivel.setText("Nivel " + nivel);

        progresoNivel.setMax(PUNTOS_POR_NIVEL);
        progresoNivel.setProgress(puntosNivel);

        tvProgreso.setText(
                puntosNivel + " de " + PUNTOS_POR_NIVEL
                        + " puntos para el siguiente nivel"
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Actualiza los datos al volver a esta pantalla.
        actualizarInterfaz();

        if (drakoHelper != null) {
            drakoHelper.iniciar();
        }
    }

    @Override
    protected void onPause() {
        if (drakoHelper != null) {
            drakoHelper.detener();
        }

        super.onPause();
    }
}