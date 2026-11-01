package com.joro.parquevivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DrakoHelper drakoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvBienvenida = findViewById(R.id.tvBienvenida);
        String nombre = getIntent().getStringExtra("nombre");

        if (tvBienvenida != null) {
            if (nombre == null || nombre.trim().isEmpty()) {
                tvBienvenida.setText("¡Bienvenido a ParqueVivo!");
            } else {
                tvBienvenida.setText("¡Bienvenido, " + nombre + "!");
            }
        }

        ImageView imgDrako = findViewById(R.id.imgDrako);
        TextView tvGlobo = findViewById(R.id.tvGlobo);

        if (imgDrako != null && tvGlobo != null) {
            drakoHelper = new DrakoHelper(this, imgDrako, tvGlobo, DrakoHelper.TipoPantalla.INICIO);
        }

        View cardCaza = findViewById(R.id.cardCaza);
        if (cardCaza != null) {
            cardCaza.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Acertijo_Caza_Colores.class)));
        }

        View cardAlbum = findViewById(R.id.cardAlbum);
        if (cardAlbum != null) {
            cardAlbum.setOnClickListener(v -> mostrarMensajeDrako("¡Tu álbum llegará muy pronto!"));
        }

        View cardTrivia = findViewById(R.id.cardTrivia);
        if (cardTrivia != null) {
            cardTrivia.setOnClickListener(v -> mostrarMensajeDrako("¡La trivia está en construcción!"));
        }

        View cardPremios = findViewById(R.id.cardPremios);
        if (cardPremios != null) {
            cardPremios.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Premio.class)));
        }

        Button btnSalir = findViewById(R.id.btnSalir);
        if (btnSalir != null) {
            btnSalir.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void mostrarMensajeDrako(String mensaje) {
        if (drakoHelper != null) {
            drakoHelper.mostrarTextoEspecial(mensaje);
        } else {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drakoHelper != null) {
            // ¡ESTA ES LA MAGIA! Le decimos que relea el SharedPreferences y cambie el traje
            drakoHelper.actualizarTraje();
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