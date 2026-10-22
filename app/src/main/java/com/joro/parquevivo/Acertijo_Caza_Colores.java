package com.joro.parquevivo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Acertijo_Caza_Colores extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private TextView tvAcertijo;
    private TextView tvEstadoEscaneo;
    private ProgressBar progressBarColor;
    private ImageView imgMariposa;

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

    // Estado del juego de persecución
    private boolean enPersecucion = false;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acertijo_caza_colores);

        // 1. Vincular componentes del layout
        previewView = findViewById(R.id.previewView);
        tvAcertijo = findViewById(R.id.tvAcertijo);
        tvEstadoEscaneo = findViewById(R.id.tvEstadoEscaneo);
        progressBarColor = findViewById(R.id.progressBarColor);
        imgMariposa = findViewById(R.id.imgMariposaPersecucion);

        View drakoContainer = findViewById(R.id.layout_drako_container);

        // Extraer vistas internas usando los IDs exactos del XML
        ImageView imgDrako = drakoContainer.findViewById(R.id.imgDrako);
        TextView tvGlobo = drakoContainer.findViewById(R.id.tvGlobo);
        btnDrakoAccion = drakoContainer.findViewById(R.id.btnDrakoAccion);

        // 2. Inicializar controladores
        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.CAZA_COLORES
        );

        colorHuntController = new ColorHuntController();

        // Click sobre la mariposa cuando aparezca
        imgMariposa.setOnClickListener(v -> alAtraparMariposa());

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

        // ROJO
        List<AcertijoColorModel> bancoRojo = new ArrayList<>();
        bancoRojo.add(new AcertijoColorModel(
                1,
                "No soy fuego pero parezco quemar. Me encuentras en las manzanas maduras, en las rosas del jardín o en las señales de advertencia. ¿Qué color soy?",
                "ROJO",
                ColorHSVUtils.getLowerHSV("ROJO"),
                ColorHSVUtils.getUpperHSV("ROJO"),
                "¡Gran agudeza visual! El ROJO desactiva la primera trampa del parque. +50 ParquePuntos."
        ));
        bancoRojo.add(new AcertijoColorModel(
                1,
                "Soy el color de la llama que calienta la noche y del fruto dulce de las frutillas del bosque. Búscame antes de que el fuego se apague.",
                "ROJO",
                ColorHSVUtils.getLowerHSV("ROJO"),
                ColorHSVUtils.getUpperHSV("ROJO"),
                "¡Increíble deducción! El ROJO revela el camino secreto. +50 ParquePuntos."
        ));
        listaAcertijos.add(bancoRojo.get(random.nextInt(bancoRojo.size())));

        // VERDE
        List<AcertijoColorModel> bancoVerde = new ArrayList<>();
        bancoVerde.add(new AcertijoColorModel(
                2,
                "Soy la piel del bosque y el aliento del parque. Cubro el suelo donde caminas y limpio el aire desde la copa de los árboles gigantes.",
                "VERDE",
                ColorHSVUtils.getLowerHSV("VERDE"),
                ColorHSVUtils.getUpperHSV("VERDE"),
                "¡Fabuloso! El VERDE demuestra tu conexión con la naturaleza. +50 ParquePuntos."
        ));
        bancoVerde.add(new AcertijoColorModel(
                2,
                "Si miras el musgo sobre las rocas o el caparazón de las viejas tortugas del estanque, me verás brillar. ¿Cuál es este color vital?",
                "VERDE",
                ColorHSVUtils.getLowerHSV("VERDE"),
                ColorHSVUtils.getUpperHSV("VERDE"),
                "¡Excelente! Has encontrado la esencia del color VERDE."
        ));
        listaAcertijos.add(bancoVerde.get(random.nextInt(bancoVerde.size())));

        // AZUL
        List<AcertijoColorModel> bancoAzul = new ArrayList<>();
        bancoAzul.add(new AcertijoColorModel(
                3,
                "Inalcanzable en el manto sobre nuestras cabezas y cristalino en las aguas profundas de las fuentes. ¿Qué tono necesita Drako?",
                "AZUL",
                ColorHSVUtils.getLowerHSV("AZUL"),
                ColorHSVUtils.getUpperHSV("AZUL"),
                "¡Perfecto! El AZUL ha restaurado la paz en la fuente del parque."
        ));
        bancoAzul.add(new AcertijoColorModel(
                3,
                "No me puedes sujetar con las manos cuando corro por el río, pero si miras al horizonte en un día despejado, me verás en todas partes.",
                "AZUL",
                ColorHSVUtils.getLowerHSV("AZUL"),
                ColorHSVUtils.getUpperHSV("AZUL"),
                "¡Fantástico! El AZUL activa el mecanismo acuático."
        ));
        listaAcertijos.add(bancoAzul.get(random.nextInt(bancoAzul.size())));

        // AMARILLO
        List<AcertijoColorModel> bancoAmarillo = new ArrayList<>();
        bancoAmarillo.add(new AcertijoColorModel(
                4,
                "Guardo la luz del sol matutino y el polvo brillante que buscan las abejas en los girasoles. ¿Qué resplandor debes enfocar?",
                "AMARILLO",
                ColorHSVUtils.getLowerHSV("AMARILLO"),
                ColorHSVUtils.getUpperHSV("AMARILLO"),
                "¡MISIÓN CUMPLIDA! El AMARILLO ha iluminado todo el parque."
        ));
        bancoAmarillo.add(new AcertijoColorModel(
                4,
                "Soy el color de las hojas secas en otoño y del rayo de luz que ilumina la mañana. Encuentra mi brillo para completar el reto.",
                "AMARILLO",
                ColorHSVUtils.getLowerHSV("AMARILLO"),
                ColorHSVUtils.getUpperHSV("AMARILLO"),
                "¡VICTORIA! Has desbloqueado el poder de la luz dorada."
        ));
        listaAcertijos.add(bancoAmarillo.get(random.nextInt(bancoAmarillo.size())));

        // NARANJA
        List<AcertijoColorModel> bancoNaranja = new ArrayList<>();
        bancoNaranja.add(new AcertijoColorModel(
                5,
                "Nací del abrazo entre el fuego y la luz del sol. Me encuentras en la piel de las mandarinas jugosas y en el manto de las hojas de otoño. ¿Qué color soy?",
                "NARANJA",
                ColorHSVUtils.getLowerHSV("NARANJA"),
                ColorHSVUtils.getUpperHSV("NARANJA"),
                "¡Excelente deducción! El NARANJA enciende la linterna mística de Drako. +50 ParquePuntos."
        ));
        bancoNaranja.add(new AcertijoColorModel(
                5,
                "No soy tan ardiente como el rojo ni tan radiante como el amarillo, pero adorno los atardeceres y la piel de los cítricos del huerto. Encuentra mi calidez.",
                "NARANJA",
                ColorHSVUtils.getLowerHSV("NARANJA"),
                ColorHSVUtils.getUpperHSV("NARANJA"),
                "¡Gran trabajo! El NARANJA ilumina el sendero oculto del parque."
        ));
        listaAcertijos.add(bancoNaranja.get(random.nextInt(bancoNaranja.size())));

        // MORADO
        List<AcertijoColorModel> bancoMorado = new ArrayList<>();
        bancoMorado.add(new AcertijoColorModel(
                6,
                "Fui el tono de la realeza en la antigüedad. Me escondo en el dulce jugo de las uvas y en el aroma fresco de la lavanda. ¿Qué misterioso color buscas?",
                "MORADO",
                ColorHSVUtils.getLowerHSV("MORADO"),
                ColorHSVUtils.getUpperHSV("MORADO"),
                "¡Misterio resuelto! El MORADO revela el secreto de los árboles ancestrales. +50 ParquePuntos."
        ));
        bancoMorado.add(new AcertijoColorModel(
                6,
                "Me oculto entre las sombras al atardecer y en los pétalos de las violetas silvestres. Si me enfocas, activarás el antiguo mapa del parque.",
                "MORADO",
                ColorHSVUtils.getLowerHSV("MORADO"),
                ColorHSVUtils.getUpperHSV("MORADO"),
                "¡Increíble! El MORADO ha desbloqueado la runa mágica."
        ));
        listaAcertijos.add(bancoMorado.get(random.nextInt(bancoMorado.size())));

        // ROSA
        List<AcertijoColorModel> bancoRosa = new ArrayList<>();
        bancoRosa.add(new AcertijoColorModel(
                7,
                "Soy delicado como los pétalos de flor de cerezo y brillante como el plumaje de los flamencos en el estanque. ¿Qué suave color debes enfocar?",
                "ROSA",
                ColorHSVUtils.getLowerHSV("ROSA"),
                ColorHSVUtils.getUpperHSV("ROSA"),
                "¡Sublime! El ROSA ha restaurado la armonía en el jardín del parque."
        ));
        bancoRosa.add(new AcertijoColorModel(
                7,
                "Nací de la mezcla entre la fuerza del rojo y la pureza del blanco. Me verás en los capullos de primavera y en los dulces algodones. ¿Cuál es mi tono?",
                "ROSA",
                ColorHSVUtils.getLowerHSV("ROSA"),
                ColorHSVUtils.getUpperHSV("ROSA"),
                "¡VICTORIA TOTAL! Has completado todos los acertijos de la Caza de Colores."
        ));
        listaAcertijos.add(bancoRosa.get(random.nextInt(bancoRosa.size())));

        // Mezcla el orden de presentación de los 7 colores de forma totalmente aleatoria
        Collections.shuffle(listaAcertijos);
    }

    private void mostrarAcertijoActual() {
        drakoHelper.mostrarTextoEspecial("¡Lee el acertijo de arriba, explorador!");

        // Reiniciar elementos visuales de la ronda anterior
        imgMariposa.setVisibility(View.GONE);
        imgMariposa.animate().cancel();
        enPersecucion = false;

        if (progressBarColor != null) {
            progressBarColor.setVisibility(View.GONE);
            progressBarColor.setProgress(0);
        }
        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setText("🔍 Presiona '¡Buscar Color!' para iniciar");
        }

        if (indiceActual < listaAcertijos.size()) {
            acertijoActual = listaAcertijos.get(indiceActual);
            colorHuntController.setScanningActive(false);

            actualizarInterfazDrako(
                    acertijoActual.getTextoAcertijo(),
                    "¡Buscar Color!",
                    v -> {
                        ocultarBotonAccion();
                        if (progressBarColor != null) {
                            progressBarColor.setVisibility(View.VISIBLE);
                            progressBarColor.setProgress(0);
                        }
                        if (tvEstadoEscaneo != null) {
                            tvEstadoEscaneo.setText("🎯 Apunta el cuadro central hacia el color objetivo...");
                        }

                        colorHuntController.setTargetColor(
                                acertijoActual.getHsvLower(),
                                acertijoActual.getHsvUpper(),
                                new ColorHuntController.ColorDetectionListener() {
                                    @Override
                                    public void onProgressUpdate(int progress) {
                                        runOnUiThread(() -> {
                                            if (progressBarColor != null) {
                                                progressBarColor.setProgress(progress);
                                            }
                                            if (tvEstadoEscaneo != null) {
                                                if (progress > 0) {
                                                    // Se activa cuando la cámara empieza a ver el color correcto y llena la barra
                                                    tvEstadoEscaneo.setText("¡Detectando color! Mantén fijo (" + progress + "%)");
                                                } else {
                                                    // Se muestra mientras aún no detecte nada
                                                    tvEstadoEscaneo.setText("caza de colores iniciada vamos por el...shii...");
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onColorVerified() {
                                        runOnUiThread(() -> alDetectarColorExitoso());
                                    }
                                }
                        );
                        colorHuntController.setScanningActive(true);
                    }
            );
        } else {
            actualizarInterfazDrako(
                    "¡Has completado todos los acertijos de color del parque!",
                    "Finalizar",
                    v -> finish()
            );
        }
    }

    private void alDetectarColorExitoso() {
        colorHuntController.setScanningActive(false);

        if (progressBarColor != null) {
            progressBarColor.setVisibility(View.GONE);
        }
        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setText("🎉 ¡Color Verificado! ¡Atrapa a Drako!");
        }

        // Iniciar minijuego de persecución
        iniciarJuegoPersecucion();
    }

    private void iniciarJuegoPersecucion() {
        enPersecucion = true;
        imgMariposa.setVisibility(View.VISIBLE);
        imgMariposa.setScaleX(1.0f);
        imgMariposa.setScaleY(1.0f);

        animarMariposa();
    }

    private void animarMariposa() {
        if (!enPersecucion) return;

        View parentView = (View) imgMariposa.getParent();
        if (parentView == null) return;

        int maxX = Math.max(100, parentView.getWidth() - imgMariposa.getWidth() - 100);
        int maxY = Math.max(200, parentView.getHeight() - imgMariposa.getHeight() - 300);

        float nextX = random.nextInt(Math.max(1, maxX));
        float nextY = random.nextInt(Math.max(1, maxY)) + 120; // Evitar la zona del texto superior

        imgMariposa.animate()
                .x(nextX)
                .y(nextY)
                .setDuration(900 + random.nextInt(500))
                .withEndAction(() -> {
                    if (enPersecucion) {
                        animarMariposa();
                    }
                })
                .start();
    }

    private void alAtraparMariposa() {
        if (!enPersecucion) return;

        enPersecucion = false;
        imgMariposa.animate().cancel();

        // Efecto visual al tocar
        imgMariposa.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .alpha(0.5f)
                .setDuration(300)
                .withEndAction(() -> {
                    imgMariposa.setVisibility(View.GONE);
                    imgMariposa.setAlpha(1.0f);
                    completarAcertijo();
                })
                .start();
    }

    private void completarAcertijo() {
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
        if (tvAcertijo != null) {
            tvAcertijo.setText(textoMensaje);
        }
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

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
                    if (colorHuntController.isScanningActive()) {
                        // Forzar la extracción del Bitmap de la vista en el Main Thread para evitar fallos de renderizado
                        ContextCompat.getMainExecutor(this).execute(() -> {
                            if (colorHuntController.isScanningActive()) {
                                Bitmap bitmap = previewView.getBitmap();
                                if (bitmap != null) {
                                    colorHuntController.processFrameBitmap(bitmap);
                                }
                            }
                        });
                    }
                    imageProxy.close();
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
            drakoHelper.iniciarSinSaludo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drakoHelper != null) {
            drakoHelper.detener();
        }
        if (imgMariposa != null) {
            imgMariposa.animate().cancel();
        }
    }
}