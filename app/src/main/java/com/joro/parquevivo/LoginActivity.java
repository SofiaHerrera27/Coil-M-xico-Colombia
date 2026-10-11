package com.joro.parquevivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private DatabaseHelper db;

    // Instancia del ayudante de Drako
    private DrakoHelper drakoHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Obtener los elementos del layout flotante de Drako
        ImageView imgDrako = findViewById(R.id.imgDrako);
        TextView tvGlobo = findViewById(R.id.tvGlobo);

        // Conectar a Drako con sus indicaciones en la pantalla LOGIN
        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.LOGIN
        );

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);

        btnLogin.setOnClickListener(v -> iniciarSesion());

        tvIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }


    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = db.validarLogin(email, password);
        if (nombre != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("nombre", nombre);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
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