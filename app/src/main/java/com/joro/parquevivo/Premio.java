package com.joro.parquevivo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Premio extends AppCompatActivity {

    private TextView tvPuntosDisponibles;
    private TextView tvResultadoCanje;
    private ProgresoManager progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canje_puntos);

        // Inicializar gestor de audio
        MusicaManager.init(this);

        // Instancia unificada del administrador de progreso
        progreso = new ProgresoManager(this);

        tvPuntosDisponibles = findViewById(R.id.tvPuntosDisponibles);
        tvResultadoCanje = findViewById(R.id.tvResultadoCanje);

        Button btnVolver = findViewById(R.id.btnVolverPremios);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // Configuración de los 20 botones de canje
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
        // Reproduce la música de fondo al entrar o regresar a la vista de canje
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);
        actualizarPuntos();
        actualizarVisualesDePremios();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa la música cuando la actividad pasa a segundo plano
        MusicaManager.pausar();
    }

    private void actualizarPuntos() {
        int puntosDisponibles = progreso.getPuntos();
        if (tvPuntosDisponibles != null) {
            tvPuntosDisponibles.setText("Tus puntos: " + puntosDisponibles);
        }
    }

    private void actualizarVisualesDePremios() {
        String itemEquipado = progreso.getTrajeEquipado().toLowerCase();
        restaurarTextoBotones();

        int resId = 0;
        switch (itemEquipado) {
            case "sombrero_explorador": resId = R.id.btnCanjearSombreroExplorador; break;
            case "gorra_guardabosques": resId = R.id.btnCanjearGorraGuardabosques; break;
            case "mochila_exploradora": resId = R.id.btnCanjearMochilaExploradora; break;
            case "brujula_dorada": resId = R.id.btnCanjearBrujulaDorada; break;
            case "mariposa_magica": resId = R.id.btnCanjearMariposaMagica; break;
            case "sombrero_mago": resId = R.id.btnCanjearSombreroMago; break;
            case "cristal_energia": resId = R.id.btnCanjearCristalEnergia; break;
            case "zorrito_bosque": resId = R.id.btnCanjearZorritoBosque; break;
            case "buho_sabio": resId = R.id.btnCanjearBuhoSabio; break;
            case "medalla_ecologica": resId = R.id.btnCanjearMedallaEcologica; break;
            case "corona_dorada": resId = R.id.btnCanjearCoronaDorada; break;
            case "alas_magicas": resId = R.id.btnCanjearAlasMagicas; break;
            case "dragon_bebe": resId = R.id.btnCanjearDragonBebe; break;
            case "tunica_magica": resId = R.id.btnCanjearTunicaMagica; break;
            case "guardian_bosque": resId = R.id.btnCanjearGuardianBosque; break;
            case "traje_aventurero": resId = R.id.btnCanjearTrajeAventurero; break;
            case "varita_natural": resId = R.id.btnCanjearVaritaNatural; break;
            case "cofre_misterioso": resId = R.id.btnCanjearCofreMisterioso; break;
            case "armadura_dorada": resId = R.id.btnCanjearArmaduraDorada; break;
            case "alas_celestiales": resId = R.id.btnCanjearAlasCelestiales; break;
        }

        if (resId != 0) {
            Button btn = findViewById(resId);
            if (btn != null) btn.setText("¡Equipado!");
        }
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
            if (btn != null) btn.setText("Canjear");
        }
    }

    private void configurarBoton(int idBoton, String nombrePremio, int costo, String claveItem) {
        Button boton = findViewById(idBoton);
        if (boton != null) {
            boton.setOnClickListener(v -> realizarCanje(nombrePremio, costo, claveItem));
        }
    }

    private void realizarCanje(String nombrePremio, int costo, String claveItem) {
        // Control de seguridad: Si ya lo tiene equipado, no se cobra de nuevo
        if (progreso.getTrajeEquipado().equalsIgnoreCase(claveItem)) {
            Toast.makeText(this, "¡Este traje ya lo tienes equipado!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Intenta descontar los puntos usando ProgresoManager
        if (progreso.descontarPuntos(costo)) {
            // Guarda el traje equipado en ProgresoManager
            progreso.setTrajeEquipado(claveItem);

            tvResultadoCanje.setText("¡Canje exitoso!\nHas obtenido: " + nombrePremio + "\n\nDrako se ha puesto tu nuevo traje.");
            Toast.makeText(this, "¡Premio equipado!", Toast.LENGTH_SHORT).show();

            actualizarVisualesDePremios();
            actualizarPuntos();
        } else {
            int faltantes = costo - progreso.getPuntos();
            tvResultadoCanje.setText("No tienes suficientes puntos.\nTe faltan: " + faltantes);
            Toast.makeText(this, "Necesitas más puntos", Toast.LENGTH_SHORT).show();
        }
    }
}