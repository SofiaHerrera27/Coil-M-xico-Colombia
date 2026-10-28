package com.joro.parquevivo;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword, etConfirmar;
    private DatabaseHelper db;
    private DrakoHelper drakoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Garantizar inicialización del gestor de música
        MusicaManager.init(this);

        db = new DatabaseHelper(this);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmar = findViewById(R.id.etConfirmar);

        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        TextView tvIrLogin = findViewById(R.id.tvIrLogin);

        // Elementos del layout flotante de Drako
        ImageView imgDrako = findViewById(R.id.imgDrako);
        TextView tvGlobo = findViewById(R.id.tvGlobo);

        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.REGISTRO
        );

        if (btnRegistrar != null) {
            btnRegistrar.setOnClickListener(v -> registrar());
        }

        if (tvIrLogin != null) {
            tvIrLogin.setOnClickListener(v -> finish());
        }
    }

    private void registrar() {
        String nombre = (etNombre != null) ? etNombre.getText().toString().trim() : "";
        String email = (etEmail != null) ? etEmail.getText().toString().trim() : "";
        String password = (etPassword != null) ? etPassword.getText().toString() : "";
        String confirmar = (etConfirmar != null) ? etConfirmar.getText().toString() : "";

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (etEmail != null) etEmail.setError("Correo no válido");
            return;
        }
        if (password.length() < 6) {
            if (etPassword != null) etPassword.setError("Mínimo 6 caracteres");
            return;
        }
        if (!password.equals(confirmar)) {
            if (etConfirmar != null) etConfirmar.setError("Las contraseñas no coinciden");
            return;
        }

        if (db.registrarUsuario(nombre, email, password)) {
            Toast.makeText(this, "Usuario creado exitosamente", Toast.LENGTH_LONG).show();
            finish(); // vuelve al login
        } else {
            Toast.makeText(this, "Ese correo ya está registrado", Toast.LENGTH_SHORT).show();
        }
    }

    // --- MANEJO DE CICLO DE VIDA Y RECURSOS DE AUDIO ---

    @Override
    protected void onResume() {
        super.onResume();
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);
        if (drakoHelper != null) {
            drakoHelper.iniciar(); // Inicia saludo y parpadeo de Drako
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicaManager.pausar();
        if (drakoHelper != null) {
            drakoHelper.detener(); // Cancela animaciones y audio activo
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drakoHelper != null) {
            drakoHelper.liberar(); // Cierra el motor de voz TextToSpeech de forma limpia
        }
    }
}