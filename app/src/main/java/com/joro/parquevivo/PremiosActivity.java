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

        ProgresoManager progreso = new ProgresoManager(this);
        GridLayout grid = findViewById(R.id.gridTrofeos);
        int lado = (int) (130 * getResources().getDisplayMetrics().density);
        int obtenidos = 0;

        for (Personaje p : Personaje.values()) {
            boolean capturado = progreso.estaCapturado(p.name());
            if (capturado) obtenidos++;

            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(lado, lado));
            img.setImageResource(p.imagenResId);
            if (!capturado) {
                img.setColorFilter(Color.BLACK);   // silueta del trofeo pendiente
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

        ((TextView) findViewById(R.id.tvPremiosPuntos))
                .setText("ParquePuntos: " + progreso.getPuntos());
        ((TextView) findViewById(R.id.tvPremiosAvance))
                .setText(obtenidos + " de " + Personaje.values().length + " trofeos");

        ((Button) findViewById(R.id.btnVolverPremios)).setOnClickListener(v -> finish());
    }
    @Override
    protected void onResume() {
        super.onResume();
        MusicaManager.cambiarPista(MusicaManager.Pista.AMBIENTE);
    }
}