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

    // Nuevo ImageView para el fondo de pantalla integrado
    private ImageView imgLoginFondo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialización global del sistema de sonido al abrir la aplicación
        MusicaManager.init(this);

        db = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Vincular el nuevo ImageView para el fondo integrado
        imgLoginFondo = findViewById(R.id.imgFondoLogin);
        if (imgLoginFondo != null) {
            // Establecer la imagen de fondo generada
            imgLoginFondo.setImageResource(R.drawable.fondo_parquevivo);
        }

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

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> iniciarSesion());
        }

        if (tvIrRegistro != null) {
            tvIrRegistro.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));
        }
    }

    private void iniciarSesion() {
        String email = (etEmail != null) ? etEmail.getText().toString().trim() : "";
        String password = (etPassword != null) ? etPassword.getText().toString() : "";

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

    // --- MANEJO DE CICLO DE VIDA Y RECURSOS DE AUDIO ---

    @Override
    protected void onResume() {
        super.onResume();
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);
        if (drakoHelper != null) {
            drakoHelper.iniciar(); // Inicia voz y parpadeo de Drako
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicaManager.pausar();
        if (drakoHelper != null) {
            drakoHelper.detener(); // Pausa reproducción y animaciones activas
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drakoHelper != null) {
            drakoHelper.liberar(); // Libera TextToSpeech al cerrar la vista
        }
    }
}