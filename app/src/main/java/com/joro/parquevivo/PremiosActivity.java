package com.joro.parquevivo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PremiosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premios);

        // Garantizar el contexto de audio al cargar la pantalla
        MusicaManager.init(this);

        ProgresoManager progreso = new ProgresoManager(this);
        GridLayout grid = findViewById(R.id.gridTrofeos);

        // CORREGIDO: Adición del operador de multiplicación (*)
        int lado = (int) (130 * getResources().getDisplayMetrics().density);
        int obtenidos = 0;

        if (grid != null) {
            for (Personaje p : Personaje.values()) {
                boolean capturado = progreso.estaCapturado(p.name());
                if (capturado) obtenidos++;

                ImageView img = new ImageView(this);
                img.setLayoutParams(new LinearLayout.LayoutParams(lado, lado));
                img.setImageResource(p.imagenResId);
                if (!capturado) {
                    img.setColorFilter(Color.BLACK);   // Silueta del trofeo pendiente
                    img.setAlpha(0.25f);
                }

                TextView nombre = new TextView(this);
                nombre.setText(capturado ? p.nombre : "???");
                nombre.setGravity(Gravity.CENTER);

                LinearLayout celda = new LinearLayout(this);
                celda.setOrientation(LinearLayout.VERTICAL);
                celda.setGravity(Gravity.CENTER);
                celda.addView(img);
                celda.addView(nombre);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f));
                lp.width = 0;
                lp.setMargins(8, 8, 8, 8);
                grid.addView(celda, lp);
            }
        }

        TextView tvPuntos = findViewById(R.id.tvPremiosPuntos);
        if (tvPuntos != null) {
            tvPuntos.setText("ParquePuntos: " + progreso.getPuntos());
        }

        TextView tvAvance = findViewById(R.id.tvPremiosAvance);
        if (tvAvance != null) {
            tvAvance.setText(obtenidos + " de " + Personaje.values().length + " trofeos");
        }

        Button btnVolver = findViewById(R.id.btnVolverPremios);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicaManager.pausar();
    }
}