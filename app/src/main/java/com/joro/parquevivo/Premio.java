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

        // Conectar los elementos del XML.
        tvPuntosDisponibles = findViewById(R.id.tvPuntosDisponibles);
        tvResultadoCanje = findViewById(R.id.tvResultadoCanje);

        // Botón para regresar.
        Button btnVolver = findViewById(R.id.btnVolverPremios);

        btnVolver.setOnClickListener(v -> finish());

        // Conectar los 20 botones de premios.
        configurarBoton(R.id.btnCanjearSombreroExplorador,
                "Sombrero de explorador", 50);

        configurarBoton(R.id.btnCanjearGorraGuardabosques,
                "Gorra del guardabosques", 100);

        configurarBoton(R.id.btnCanjearMochilaExploradora,
                "Mochila exploradora", 100);

        configurarBoton(R.id.btnCanjearBrujulaDorada,
                "Brújula dorada", 150);

        configurarBoton(R.id.btnCanjearMariposaMagica,
                "Mariposa mágica", 150);

        configurarBoton(R.id.btnCanjearSombreroMago,
                "Sombrero de mago", 150);

        configurarBoton(R.id.btnCanjearCristalEnergia,
                "Cristal de energía", 200);

        configurarBoton(R.id.btnCanjearZorritoBosque,
                "Zorrito del bosque", 200);

        configurarBoton(R.id.btnCanjearBuhoSabio,
                "Búho sabio", 250);

        configurarBoton(R.id.btnCanjearMedallaEcologica,
                "Medalla ecológica", 250);

        configurarBoton(R.id.btnCanjearCoronaDorada,
                "Corona dorada", 300);

        configurarBoton(R.id.btnCanjearAlasMagicas,
                "Alas mágicas", 350);

        configurarBoton(R.id.btnCanjearDragonBebe,
                "Dragón bebé", 350);

        configurarBoton(R.id.btnCanjearTunicaMagica,
                "Túnica mágica", 350);

        configurarBoton(R.id.btnCanjearGuardianBosque,
                "Guardián del bosque", 400);

        configurarBoton(R.id.btnCanjearTrajeAventurero,
                "Traje de guardián del bosque", 400);

        configurarBoton(R.id.btnCanjearVaritaNatural,
                "Varita natural", 450);

        configurarBoton(R.id.btnCanjearCofreMisterioso,
                "Cofre misterioso", 500);

        configurarBoton(R.id.btnCanjearArmaduraDorada,
                "Armadura dorada", 600);

        configurarBoton(R.id.btnCanjearAlasCelestiales,
                "Alas celestiales", 700);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarPuntos();
    }

    // Consultar y mostrar los ParquePuntos disponibles.
    private void actualizarPuntos() {

        puntosDisponibles = ParquePuntos.obtenerPuntos(this);

        tvPuntosDisponibles.setText(
                "Tus ParquePuntos: " + puntosDisponibles
        );
    }

    // Configurar cada botón de canje.
    private void configurarBoton(int idBoton, String nombrePremio, int costo) {

        Button boton = findViewById(idBoton);

        boton.setOnClickListener(v ->
                simularCanje(nombrePremio, costo)
        );
    }

    // Comprobar si el usuario tiene puntos suficientes.
    private void simularCanje(String nombrePremio, int costo) {

        actualizarPuntos();

        if (puntosDisponibles >= costo) {

            tvResultadoCanje.setText(
                    "¡Canje simulado exitosamente!\n\n"
                            + "Premio: " + nombrePremio + "\n"
                            + "Costo: " + costo + " ParquePuntos.\n\n"
                            + "¡Felicidades! Tienes suficientes puntos "
                            + "para obtener esta recompensa.\n\n"
                            + "Esta es una demostración. "
                            + "Tus puntos no han sido descontados."
            );

            Toast.makeText(
                    this,
                    "¡Canje de demostración exitoso!",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            int puntosFaltantes = costo - puntosDisponibles;

            tvResultadoCanje.setText(
                    "No tienes suficientes ParquePuntos.\n\n"
                            + "Premio: " + nombrePremio + "\n"
                            + "Costo: " + costo + " ParquePuntos.\n"
                            + "Disponibles: " + puntosDisponibles + " puntos.\n"
                            + "Te faltan: " + puntosFaltantes + " puntos."
            );

            Toast.makeText(
                    this,
                    "Necesitas más ParquePuntos.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
