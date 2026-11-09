package com.joro.parquevivo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DrakoHelper drakoHelper;
    private long usuarioId = 1; // ID de usuario por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String nombre = getIntent().getStringExtra("nombre");
        if (nombre == null) nombre = "Explorador";

        // Recuperar usuario_id proveniente del Login
        long idRecibido = getIntent().getLongExtra("usuario_id", -1);
        if (idRecibido != -1) {
            usuarioId = idRecibido;
        }

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

        ImageButton btnMusicaToggle = findViewById(R.id.btnMusicaToggle);
        if (btnMusicaToggle != null) {
            btnMusicaToggle.setImageResource(MusicaManager.isMusicaHabilitada() ? R.drawable.ic_music_on : R.drawable.ic_music_off);
            btnMusicaToggle.setOnClickListener(v -> {
                boolean estaActiva = MusicaManager.toggleMusica(this);
                btnMusicaToggle.setImageResource(estaActiva ? R.drawable.ic_music_on : R.drawable.ic_music_off);
            });
        }

        // 1. SECCIÓN CAZA DE COLORES
        findViewById(R.id.cardCaza).setOnClickListener(v -> {
            Intent intent = new Intent(this, Acertijo_Caza_Colores.class);
            intent.putExtra("usuario_id", usuarioId);
            startActivity(intent);
        });

        // 2. SECCIÓN PREMIOS (TU SECCIÓN: Colección de Dragones 3D)
        findViewById(R.id.cardPremios).setOnClickListener(v -> {
            Intent intent = new Intent(this, PremiosActivity.class);
            intent.putExtra("usuario_id", usuarioId);
            startActivity(intent);
        });

        // 3. SECCIÓN ÁLBUM DE AVENTURAS
        findViewById(R.id.cardAlbum).setOnClickListener(v -> {
            Intent intent = new Intent(this, AlbumActivity.class);
            intent.putExtra("usuario_id", usuarioId);
            startActivity(intent);
        });

        // 4. SECCIÓN CANJE DE PUNTOS (SECCIÓN DE TU COMPAÑERA: Premio.class)
        findViewById(R.id.cardPuntos).setOnClickListener(v -> {
            Intent intent = new Intent(this, Premio.class);
            intent.putExtra("usuario_id", usuarioId);
            startActivity(intent);
        });

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
        MusicaManager.init(this);
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);

        if (drakoHelper != null) {
            drakoHelper.iniciar();
        }

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
            drakoHelper.detener();
        }
    }
}