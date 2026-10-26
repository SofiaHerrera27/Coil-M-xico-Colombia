package com.joro.parquevivo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Premio extends AppCompatActivity {

    private TextView tvPuntosDisponibles;
    private TextView tvResultadoCanje;

    private int puntosDisponibles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premios);

        tvPuntosDisponibles = findViewById(R.id.tvPuntosDisponibles);
        tvResultadoCanje = findViewById(R.id.tvResultadoCanje);

        Button btnSemilla = findViewById(R.id.btnCanjearSemilla);
        Button btnInsignia = findViewById(R.id.btnCanjearInsignia);
        Button btnGuardian = findViewById(R.id.btnCanjearGuardian);
        Button btnVolver = findViewById(R.id.btnVolverPremios);

        btnSemilla.setOnClickListener(v ->
                simularCanje("Semilla sorpresa", 50));

        btnInsignia.setOnClickListener(v ->
                simularCanje("Insignia de explorador", 100));

        btnGuardian.setOnClickListener(v ->
                simularCanje("Guardián de la naturaleza", 200));

        btnVolver.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarPuntos();
    }

    private void actualizarPuntos() {
        puntosDisponibles = ParquePuntos.obtenerPuntos(this);

        tvPuntosDisponibles.setText(
                "Tus ParquePuntos: " + puntosDisponibles
        );
    }

    private void simularCanje(String nombrePremio, int costo) {

        if (puntosDisponibles >= costo) {

            tvResultadoCanje.setText(
                    "¡Canje simulado exitosamente!\n\n"
                            + "Premio: " + nombrePremio + "\n"
                            + "Costo: " + costo + " ParquePuntos.\n\n"
                            + "Esta es una demostración. "
                            + "Tus puntos no han sido descontados."
            );

            Toast.makeText(
                    this,
                    "¡Canje de demostración exitoso!",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            tvResultadoCanje.setText(
                    "No tienes suficientes ParquePuntos.\n\n"
                            + "Premio: " + nombrePremio + "\n"
                            + "Costo: " + costo + " puntos.\n"
                            + "Disponibles: " + puntosDisponibles + " puntos."
            );

            Toast.makeText(
                    this,
                    "Necesitas más ParquePuntos.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
