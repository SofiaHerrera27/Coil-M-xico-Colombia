package com.joro.parquevivo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Premio extends AppCompatActivity {

    private TextView tvPuntosDisponibles;
    private TextView tvResultadoCanje;
    private int puntosDisponibles;

    private static final String NOMBRE_PREFERENCIAS = "parque_puntos";
    private static final String CLAVE_PUNTOS = "puntos";
    private static final String PREFS_EQUIPAMIENTO = "parque_prefs";
    private static final String KEY_ITEM_EQUIPADO = "item_equipado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premios);

        tvPuntosDisponibles = findViewById(R.id.tvPuntosDisponibles);
        tvResultadoCanje = findViewById(R.id.tvResultadoCanje);

        Button btnVolver = findViewById(R.id.btnVolverPremios);
        btnVolver.setOnClickListener(v -> finish());

        // Se eliminó la inicialización de Drako aquí porque el XML de premios no lo tiene.

        configurarBoton(R.id.btnCanjearSombreroExplorador, "Sombrero de explorador", 50, "sombrero_explorador");
        configurarBoton(R.id.btnCanjearGorraGuardabosques, "Gorra del guardabosques", 100, "gorra_guardabosques");
        configurarBoton(R.id.btnCanjearMochilaExploradora, "Mochila exploradora", 100, "mochila_exploradora");
        configurarBoton(R.id.btnCanjearBrujulaDorada, "Brújula dorada", 150, "brujula_dorada");
        configurarBoton(R.id.btnCanjearMariposaMagica, "Mariposa mágica", 150, "mariposa_magica");
        configurarBoton(R.id.btnCanjearSombreroMago, "Sombrero de mago", 150, "sombrero_mago");
        configurarBoton(R.id.btnCanjearCristalEnergia, "Cristal de energía", 200, "cristal_energia");
        configurarBoton(R.id.btnCanjearZorritoBosque, "Zorrito del bosque", 200, "zorrito_bosque");
        configurarBoton(R.id.btnCanjearBuhoSabio, "Búho sabio", 250, "buho_sabio");
        configurarBoton(R.id.btnCanjearMedallaEcologica, "Medalla ecológica", 250, "medalla_ecologica");
        configurarBoton(R.id.btnCanjearCoronaDorada, "Corona dorada", 300, "corona_dorada");
        configurarBoton(R.id.btnCanjearAlasMagicas, "Alas mágicas", 350, "alas_magicas");
        configurarBoton(R.id.btnCanjearDragonBebe, "Dragón bebé", 350, "dragon_bebe");
        configurarBoton(R.id.btnCanjearTunicaMagica, "Túnica mágica", 350, "tunica_magica");
        configurarBoton(R.id.btnCanjearGuardianBosque, "Guardián del bosque", 400, "guardian_bosque");
        configurarBoton(R.id.btnCanjearTrajeAventurero, "Traje de aventurero", 400, "traje_aventurero");
        configurarBoton(R.id.btnCanjearVaritaNatural, "Varita natural", 450, "varita_natural");
        configurarBoton(R.id.btnCanjearCofreMisterioso, "Cofre misterioso", 500, "cofre_misterioso");
        configurarBoton(R.id.btnCanjearArmaduraDorada, "Armadura dorada", 600, "armadura_dorada");
        configurarBoton(R.id.btnCanjearAlasCelestiales, "Alas celestiales", 700, "alas_celestiales");
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarPuntos();
        actualizarVisualesDePremios();
    }

    private int obtenerPuntos() {
        SharedPreferences preferencias = getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
        return preferencias.getInt(CLAVE_PUNTOS, 0);
    }

    private void actualizarPuntos() {
        puntosDisponibles = obtenerPuntos();
        tvPuntosDisponibles.setText("Tus puntos: " + puntosDisponibles);
    }

    private void actualizarVisualesDePremios() {
        SharedPreferences prefs = getSharedPreferences(PREFS_EQUIPAMIENTO, Context.MODE_PRIVATE);
        String itemEquipado = prefs.getString(KEY_ITEM_EQUIPADO, "");
        restaurarTextoBotones();

        if(itemEquipado.equals("sombrero_explorador")) { Button btn = findViewById(R.id.btnCanjearSombreroExplorador); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("gorra_guardabosques")) { Button btn = findViewById(R.id.btnCanjearGorraGuardabosques); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("mochila_exploradora")) { Button btn = findViewById(R.id.btnCanjearMochilaExploradora); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("brujula_dorada")) { Button btn = findViewById(R.id.btnCanjearBrujulaDorada); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("mariposa_magica")) { Button btn = findViewById(R.id.btnCanjearMariposaMagica); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("sombrero_mago")) { Button btn = findViewById(R.id.btnCanjearSombreroMago); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("cristal_energia")) { Button btn = findViewById(R.id.btnCanjearCristalEnergia); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("zorrito_bosque")) { Button btn = findViewById(R.id.btnCanjearZorritoBosque); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("buho_sabio")) { Button btn = findViewById(R.id.btnCanjearBuhoSabio); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("medalla_ecologica")) { Button btn = findViewById(R.id.btnCanjearMedallaEcologica); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("corona_dorada")) { Button btn = findViewById(R.id.btnCanjearCoronaDorada); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("alas_magicas")) { Button btn = findViewById(R.id.btnCanjearAlasMagicas); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("dragon_bebe")) { Button btn = findViewById(R.id.btnCanjearDragonBebe); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("tunica_magica")) { Button btn = findViewById(R.id.btnCanjearTunicaMagica); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("guardian_bosque")) { Button btn = findViewById(R.id.btnCanjearGuardianBosque); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("traje_aventurero")) { Button btn = findViewById(R.id.btnCanjearTrajeAventurero); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("varita_natural")) { Button btn = findViewById(R.id.btnCanjearVaritaNatural); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("cofre_misterioso")) { Button btn = findViewById(R.id.btnCanjearCofreMisterioso); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("armadura_dorada")) { Button btn = findViewById(R.id.btnCanjearArmaduraDorada); if(btn != null) btn.setText("¡Equipado!"); }
        else if(itemEquipado.equals("alas_celestiales")) { Button btn = findViewById(R.id.btnCanjearAlasCelestiales); if(btn != null) btn.setText("¡Equipado!"); }
    }

    private void restaurarTextoBotones() {
        int[] idsBotones = {
                R.id.btnCanjearSombreroExplorador, R.id.btnCanjearGorraGuardabosques, R.id.btnCanjearMochilaExploradora,
                R.id.btnCanjearBrujulaDorada, R.id.btnCanjearMariposaMagica, R.id.btnCanjearSombreroMago,
                R.id.btnCanjearCristalEnergia, R.id.btnCanjearZorritoBosque, R.id.btnCanjearBuhoSabio,
                R.id.btnCanjearMedallaEcologica, R.id.btnCanjearCoronaDorada, R.id.btnCanjearAlasMagicas,
                R.id.btnCanjearDragonBebe, R.id.btnCanjearTunicaMagica, R.id.btnCanjearGuardianBosque,
                R.id.btnCanjearTrajeAventurero, R.id.btnCanjearVaritaNatural, R.id.btnCanjearCofreMisterioso,
                R.id.btnCanjearArmaduraDorada, R.id.btnCanjearAlasCelestiales
        };
        for (int id : idsBotones) {
            Button btn = findViewById(id);
            if (btn != null) btn.setText("Simular canje");
        }
    }

    private void configurarBoton(int idBoton, String nombrePremio, int costo, String claveItem) {
        Button boton = findViewById(idBoton);
        if (boton != null) {
            boton.setOnClickListener(v -> realizarCanje(nombrePremio, costo, claveItem));
        }
    }

    private void realizarCanje(String nombrePremio, int costo, String claveItem) {
        actualizarPuntos();

        if (puntosDisponibles >= costo) {
            int nuevosPuntos = puntosDisponibles - costo;
            SharedPreferences prefsPuntos = getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
            prefsPuntos.edit().putInt(CLAVE_PUNTOS, nuevosPuntos).apply();

            SharedPreferences prefsEquip = getSharedPreferences(PREFS_EQUIPAMIENTO, Context.MODE_PRIVATE);
            prefsEquip.edit().putString(KEY_ITEM_EQUIPADO, claveItem).apply();

            tvResultadoCanje.setText("¡Canje exitoso!\nHas obtenido: " + nombrePremio + "\n\nDrako se ha puesto tu nuevo traje.");
            Toast.makeText(this, "¡Premio equipado!", Toast.LENGTH_SHORT).show();

            actualizarVisualesDePremios();
            actualizarPuntos();

        } else {
            int faltantes = costo - puntosDisponibles;
            tvResultadoCanje.setText("No tienes suficientes puntos.\nTe faltan: " + faltantes);
            Toast.makeText(this, "Necesitas más puntos", Toast.LENGTH_SHORT).show();
        }
    }
}