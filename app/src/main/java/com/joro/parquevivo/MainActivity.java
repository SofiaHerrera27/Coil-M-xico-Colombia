package com.joro.parquevivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DrakoHelper drakoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String nombre = getIntent().getStringExtra("nombre");
        if (nombre == null) nombre = "Explorador";

        TextView tvBienvenida = findViewById(R.id.tvBienvenida);
        if (tvBienvenida != null) {
            tvBienvenida.setText("¡Bienvenido, " + nombre + "!");
        }

        ImageView imgDrako = findViewById(R.id.imgDrako);
        TextView tvGlobo = findViewById(R.id.tvGlobo);

        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.INICIO
        );

        // 1. Navegación a las actividades activas
        findViewById(R.id.cardCaza).setOnClickListener(v ->
                startActivity(new Intent(this, Acertijo_Caza_Colores.class)));

        findViewById(R.id.cardPremios).setOnClickListener(v ->
                startActivity(new Intent(this, PremiosActivity.class)));

        // 2. Secciones en desarrollo
        findViewById(R.id.cardAlbum).setOnClickListener(v ->
                drakoHelper.mostrarTextoEspecial("¡Tu álbum llegará muy pronto!"));

        findViewById(R.id.cardPuntos).setOnClickListener(v ->
                drakoHelper.mostrarTextoEspecial("¡Pronto verás tus ParquePuntos aquí!"));

        findViewById(R.id.cardTrivia).setOnClickListener(v ->
                drakoHelper.mostrarTextoEspecial("¡La trivia está en construcción!"));

        Button btnSalir = findViewById(R.id.btnSalir);
        if (btnSalir != null) {
            btnSalir.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                MusicaManager.detener();
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carga la pista de ambiente general
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);

        if (drakoHelper != null) {
            drakoHelper.iniciar();   // Muestra mensaje y activa parpadeo
        }

        // Actualizar ParquePuntos acumulados en tiempo real
        ProgresoManager progreso = new ProgresoManager(this);
        TextView tvPuntosMenu = findViewById(R.id.tvPuntosCard);
        if (tvPuntosMenu != null) {
            tvPuntosMenu.setText("ParquePuntos: " + progreso.getPuntos());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicaManager.pausar();
        if (drakoHelper != null) {
            drakoHelper.detener();   // Cancela animaciones y audio de Drako al salir
        }
    }
}