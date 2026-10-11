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

        db = new DatabaseHelper(this);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmar = findViewById(R.id.etConfirmar);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        TextView tvIrLogin = findViewById(R.id.tvIrLogin);

// Obtener los elementos del layout flotante de Drako
        ImageView imgDrako = findViewById(R.id.imgDrako);
        TextView tvGlobo = findViewById(R.id.tvGlobo);

        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.REGISTRO
        );


        btnRegistrar.setOnClickListener(v -> registrar());
        tvIrLogin.setOnClickListener(v -> finish());
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmar = etConfirmar.getText().toString();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }
        if (!password.equals(confirmar)) {
            etConfirmar.setError("Las contraseñas no coinciden");
            return;
        }

        if (db.registrarUsuario(nombre, email, password)) {
            Toast.makeText(this, "Usuario creado exitosamente", Toast.LENGTH_LONG).show();
            finish(); // vuelve al login
        } else {
            Toast.makeText(this, "Ese correo ya está registrado", Toast.LENGTH_SHORT).show();
        }
    }
    // --- MANEJO DE CICLO DE VIDA (Obligatorio para evitar errores) ---

    @Override
    protected void onResume() {
        super.onResume();
        drakoHelper.iniciar(); // Empieza a parpadear
    }

    @Override
    protected void onPause() {
        super.onPause();
        drakoHelper.detener(); // Se pausa si el usuario sale o cambia de app
    }
}