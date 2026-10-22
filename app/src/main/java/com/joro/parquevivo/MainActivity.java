package com.joro.parquevivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DrakoHelper drakoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String nombre = getIntent().getStringExtra("nombre");
        TextView tvBienvenida = findViewById(R.id.tvBienvenida);
        tvBienvenida.setText("¡Bienvenido, " + nombre + "!");
        ImageView imgDrako = findViewById(R.id.imgDrako);
        TextView tvGlobo = findViewById(R.id.tvGlobo);

        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.INICIO
        );
        //Union o mejro dicho conexion de las secciones con las  acciones correspondientes.
        findViewById(R.id.cardCaza).setOnClickListener(v ->
                startActivity(new Intent(this, Acertijo_Caza_Colores.class)));

        findViewById(R.id.cardAlbum).setOnClickListener(v ->
                drakoHelper.mostrarTextoEspecial("¡Tu álbum llegará muy pronto!"));

        findViewById(R.id.cardPuntos).setOnClickListener(v ->
                drakoHelper.mostrarTextoEspecial("¡Pronto verás tus ParquePuntos aquí!"));

        findViewById(R.id.cardTrivia).setOnClickListener(v ->
                drakoHelper.mostrarTextoEspecial("¡La trivia está en construcción!"));

        findViewById(R.id.cardPremios).setOnClickListener(v ->
                drakoHelper.mostrarTextoEspecial("¡Los premios llegarán pronto!"));

        Button btnSalir = findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        drakoHelper.iniciar();   // muestra el mensaje y empieza a parpadear
    }

    @Override
    protected void onPause() {
        super.onPause();
        drakoHelper.detener();   // para el parpadeo al salir de la pantalla
    }

}