package com.joro.parquevivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String nombre = getIntent().getStringExtra("nombre");
        TextView tvBienvenida = findViewById(R.id.tvBienvenida);
        tvBienvenida.setText("¡Bienvenido, " + nombre + "!");

        Button btnSalir = findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}