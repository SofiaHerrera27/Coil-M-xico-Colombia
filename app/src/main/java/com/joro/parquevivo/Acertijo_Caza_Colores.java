package com.joro.parquevivo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Acertijo_Caza_Colores extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;

    // Componentes visuales y controladores
    private PreviewView previewView;
    private DrakoHelper drakoHelper;
    private ColorHuntController colorHuntController;

    // Elementos del cuadro flotante de Drako
    private Button btnDrakoAccion;

    // Control de progresión
    private List<AcertijoColorModel> listaAcertijos;
    private int indiceActual = 0;
    private AcertijoColorModel acertijoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargar el layout que contiene la cámara y el include de Drako
        setContentView(R.layout.activity_acertijo_caza_colores);

        // 1. Vincular componentes del layout
        previewView = findViewById(R.id.previewView);
        View drakoContainer = findViewById(R.id.layout_drako_container);

        // Extraer vistas internas usando los IDs exactos de tu XML
        ImageView imgDrako = drakoContainer.findViewById(R.id.imgDrako);
        TextView tvGlobo = drakoContainer.findViewById(R.id.tvGlobo);
        btnDrakoAccion = drakoContainer.findViewById(R.id.btnDrakoAccion);

        // 2. Inicializar controladores con el constructor de 4 parámetros
        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.CAZA_COLORES
        );

        colorHuntController = new ColorHuntController();

        // 3. Cargar acertijos y validar permiso de cámara
        cargarAcertijos();
        verificarPermisoCamara();
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
            mostrarAcertijoActual();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
                mostrarAcertijoActual();
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para jugar a la Caza de Colores", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void cargarAcertijos() {
        listaAcertijos = new ArrayList<>();

        listaAcertijos.add(new AcertijoColorModel(
                1,
                "¡Atención explorador! Para activar el mapa del parque, busca algo del color del fuego encendido o una manzana madura.",
                "ROJO",
                ColorHSVUtils.getLowerHSV("ROJO"),
                ColorHSVUtils.getUpperHSV("ROJO"),
                "¡Increíble! El ROJO desactiva la primera trampa. +50 ParquePuntos."
        ));

        listaAcertijos.add(new AcertijoColorModel(
                2,
                "¡Avanzamos! Ahora busca el color del césped del parque y de las hojas de los árboles gigantes.",
                "VERDE",
                ColorHSVUtils.getLowerHSV("VERDE"),
                ColorHSVUtils.getUpperHSV("VERDE"),
                "¡Fabuloso! Has encontrado el color VERDE. +50 ParquePuntos."
        ));

        listaAcertijos.add(new AcertijoColorModel(
                3,
                "¡Tercer desafío! Drako necesita el color del agua limpia de las fuentes y del cielo despejado.",
                "AZUL",
                ColorHSVUtils.getLowerHSV("AZUL"),
                ColorHSVUtils.getUpperHSV("AZUL"),
                "¡Excelente! El AZUL desactiva el segundo mecanismo del parque."
        ));

        listaAcertijos.add(new AcertijoColorModel(
                4,
                "¡Último acertijo! Busca la luz radiante del sol que calienta las mañanas y el color de los girasoles.",
                "AMARILLO",
                ColorHSVUtils.getLowerHSV("AMARILLO"),
                ColorHSVUtils.getUpperHSV("AMARILLO"),
                "¡MISIÓN CUMPLIDA! Has completado todos los acertijos de la Caza de Colores."
        ));
    }

    private void mostrarAcertijoActual() {
        if (indiceActual < listaAcertijos.size()) {
            acertijoActual = listaAcertijos.get(indiceActual);

            colorHuntController.setScanningActive(false);

            // Configuramos el mensaje y acción del botón en la UI
            drakoHelper.darConsejoOSaludo();
            actualizarInterfazDrako(
                    acertijoActual.getTextoAcertijo(),
                    "¡Buscar Color!",
                    v -> {
                        ocultarBotonAccion();
                        colorHuntController.setTargetColor(
                                acertijoActual.getHsvLower(),
                                acertijoActual.getHsvUpper(),
                                () -> runOnUiThread(this::completarAcertijo)
                        );
                        colorHuntController.setScanningActive(true);
                    }
            );
        } else {
            actualizarInterfazDrako(
                    "¡Has completado todos los acertijos de color!",
                    "Finalizar",
                    v -> finish()
            );
        }
    }

    private void completarAcertijo() {
        colorHuntController.setScanningActive(false);

        actualizarInterfazDrako(
                acertijoActual.getMensajeExito(),
                "Siguiente Reto",
                v -> {
                    indiceActual++;
                    mostrarAcertijoActual();
                }
        );
    }

    private void actualizarInterfazDrako(String textoMensaje, String textoBoton, View.OnClickListener onClickListener) {
        if (btnDrakoAccion != null) {
            btnDrakoAccion.setText(textoBoton);
            btnDrakoAccion.setOnClickListener(onClickListener);
            btnDrakoAccion.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarBotonAccion() {
        if (btnDrakoAccion != null) {
            btnDrakoAccion.setVisibility(View.GONE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy image) {
                        if (colorHuntController.isScanningActive()) {
                            Bitmap bitmap = previewView.getBitmap();
                            if (bitmap != null) {
                                colorHuntController.processFrameBitmap(bitmap);
                            }
                        }
                        image.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drakoHelper != null) {
            drakoHelper.iniciar();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drakoHelper != null) {
            drakoHelper.detener();
        }
    }
}