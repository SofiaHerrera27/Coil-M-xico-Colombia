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
    private static final int PUNTOS_POR_RETO = 50;

    private TextView tvAcertijo;
    private TextView tvEstadoEscaneo;
    private ProgressBar progressBarColor;
    private ImageView imgMariposa;
    private PreviewView previewView;

    private DrakoHelper drakoHelper;
    private ColorHuntController colorHuntController;
    private Button btnDrakoAccion;

    private List<AcertijoColorModel> listaAcertijos;
    private int indiceActual = 0;
    private AcertijoColorModel acertijoActual;

    private boolean enPersecucion = false;
    private boolean retoCompletado = false;
    private boolean juegoFinalizado = false;

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acertijo_caza_colores);

        previewView = findViewById(R.id.previewView);
        tvAcertijo = findViewById(R.id.tvAcertijo);
        tvEstadoEscaneo = findViewById(R.id.tvEstadoEscaneo);
        progressBarColor = findViewById(R.id.progressBarColor);
        imgMariposa = findViewById(R.id.imgMariposaPersecucion);

        View drakoContainer = findViewById(R.id.layout_drako_container);

        if (drakoContainer == null) {
            Toast.makeText(
                    this,
                    "No se encontró el diseño de Drako.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        ImageView imgDrako =
                drakoContainer.findViewById(R.id.imgDrako);

        TextView tvGlobo =
                drakoContainer.findViewById(R.id.tvGlobo);

        btnDrakoAccion =
                drakoContainer.findViewById(R.id.btnDrakoAccion);

        if (imgDrako == null || tvGlobo == null || btnDrakoAccion == null) {
            Toast.makeText(
                    this,
                    "Faltan componentes de Drako en el diseño.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.CAZA_COLORES
        );

        colorHuntController = new ColorHuntController();

        imgMariposa.setOnClickListener(v -> alAtraparMariposa());

        cargarAcertijos();
        verificarPermisoCamara();
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

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
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startCamera();
                mostrarAcertijoActual();

            } else {
                Toast.makeText(
                        this,
                        "Se requiere permiso de cámara para jugar a la Caza de Colores.",
                        Toast.LENGTH_LONG
                ).show();

                finish();
            }
        }
    }

    private void cargarAcertijos() {
        listaAcertijos = new ArrayList<>();

        List<AcertijoColorModel> bancoRojo = new ArrayList<>();

        bancoRojo.add(new AcertijoColorModel(
                1,
                "No soy fuego pero parezco quemar. Me encuentras en las manzanas maduras, en las rosas del jardín o en las señales de advertencia. ¿Qué color soy?",
                "ROJO",
                ColorHSVUtils.getLowerHSV("ROJO"),
                ColorHSVUtils.getUpperHSV("ROJO"),
                "¡Gran agudeza visual! El ROJO desactiva la primera trampa del parque."
        ));

        bancoRojo.add(new AcertijoColorModel(
                1,
                "Soy el color de la llama que calienta la noche y del fruto dulce de las frutillas del bosque. Búscame antes de que el fuego se apague.",
                "ROJO",
                ColorHSVUtils.getLowerHSV("ROJO"),
                ColorHSVUtils.getUpperHSV("ROJO"),
                "¡Increíble deducción! El ROJO revela el camino secreto."
        ));

        listaAcertijos.add(bancoRojo.get(random.nextInt(bancoRojo.size())));

        List<AcertijoColorModel> bancoVerde = new ArrayList<>();

        bancoVerde.add(new AcertijoColorModel(
                2,
                "Soy la piel del bosque y el aliento del parque. Cubro el suelo donde caminas y limpio el aire desde la copa de los árboles gigantes.",
                "VERDE",
                ColorHSVUtils.getLowerHSV("VERDE"),
                ColorHSVUtils.getUpperHSV("VERDE"),
                "¡Fabuloso! El VERDE demuestra tu conexión con la naturaleza."
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

        List<AcertijoColorModel> bancoNaranja = new ArrayList<>();

        bancoNaranja.add(new AcertijoColorModel(
                5,
                "Nací del abrazo entre el fuego y la luz del sol. Me encuentras en la piel de las mandarinas jugosas y en el manto de las hojas de otoño. ¿Qué color soy?",
                "NARANJA",
                ColorHSVUtils.getLowerHSV("NARANJA"),
                ColorHSVUtils.getUpperHSV("NARANJA"),
                "¡Excelente deducción! El NARANJA enciende la linterna mística de Drako."
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

        List<AcertijoColorModel> bancoMorado = new ArrayList<>();

        bancoMorado.add(new AcertijoColorModel(
                6,
                "Fui el tono de la realeza en la antigüedad. Me escondo en el dulce jugo de las uvas y en el aroma fresco de la lavanda. ¿Qué misterioso color buscas?",
                "MORADO",
                ColorHSVUtils.getLowerHSV("MORADO"),
                ColorHSVUtils.getUpperHSV("MORADO"),
                "¡Misterio resuelto! El MORADO revela el secreto de los árboles ancestrales."
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

        Collections.shuffle(listaAcertijos);
    }

    private void mostrarAcertijoActual() {
        imgMariposa.animate().cancel();
        imgMariposa.setVisibility(View.GONE);
        enPersecucion = false;
        retoCompletado = false;

        if (progressBarColor != null) {
            progressBarColor.setVisibility(View.GONE);
            progressBarColor.setProgress(0);
        }

        if (indiceActual >= listaAcertijos.size()) {
            juegoFinalizado = true;

            if (colorHuntController != null) {
                colorHuntController.setScanningActive(false);
            }

            actualizarInterfazDrako(
                    "¡Has completado los siete retos de Caza de Colores! " +
                            "Revisa tus ParquePuntos para ver tu progreso.",
                    "Finalizar",
                    v -> finish()
            );

            if (tvEstadoEscaneo != null) {
                tvEstadoEscaneo.setText("¡Todos los retos completados!");
            }

            return;
        }

        juegoFinalizado = false;
        acertijoActual = listaAcertijos.get(indiceActual);

        if (colorHuntController != null) {
            colorHuntController.setScanningActive(false);
        }

        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setText(
                    "Presiona ¡Buscar Color! para iniciar"
            );
        }

        actualizarInterfazDrako(
                acertijoActual.getTextoAcertijo(),
                "¡Buscar Color!",
                v -> iniciarBusquedaColor()
        );

        if (drakoHelper != null) {
            drakoHelper.mostrarTextoEspecial(
                    "¡Lee el acertijo y encuentra el color, explorador!"
            );
        }
    }

    private void iniciarBusquedaColor() {
        if (acertijoActual == null || juegoFinalizado || retoCompletado) {
            return;
        }

        ocultarBotonAccion();

        if (progressBarColor != null) {
            progressBarColor.setVisibility(View.VISIBLE);
            progressBarColor.setProgress(0);
        }

        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setText(
                    "Apunta la cámara hacia el color objetivo..."
            );
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
                                    tvEstadoEscaneo.setText(
                                            "Detectando color: " + progress + "%"
                                    );
                                } else {
                                    tvEstadoEscaneo.setText(
                                            "Busca el color indicado en el acertijo."
                                    );
                                }
                            }
                        });
                    }

                    @Override
                    public void onColorVerified() {
                        runOnUiThread(() -> {
                            if (!isFinishing() && !juegoFinalizado) {
                                alDetectarColorExitoso();
                            }
                        });
                    }
                }
        );

        colorHuntController.setScanningActive(true);
    }

    private void alDetectarColorExitoso() {
        if (juegoFinalizado || retoCompletado || enPersecucion) {
            return;
        }

        colorHuntController.setScanningActive(false);

        if (progressBarColor != null) {
            progressBarColor.setVisibility(View.GONE);
        }

        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setText(
                    "¡Color verificado! ¡Atrapa la mariposa!"
            );
        }

        actualizarInterfazDrako(
                "¡Encontraste el color! Ahora atrapa la mariposa para ganar tus puntos.",
                "",
                v -> { }
        );

        ocultarBotonAccion();
        iniciarJuegoPersecucion();
    }

    private void iniciarJuegoPersecucion() {
        enPersecucion = true;

        imgMariposa.setVisibility(View.VISIBLE);
        imgMariposa.setScaleX(1.0f);
        imgMariposa.setScaleY(1.0f);
        imgMariposa.setAlpha(1.0f);

        imgMariposa.post(this::animarMariposa);
    }

    private void animarMariposa() {
        if (!enPersecucion || retoCompletado || isFinishing()) {
            return;
        }

        View parentView = (View) imgMariposa.getParent();

        if (parentView == null
                || parentView.getWidth() <= 0
                || parentView.getHeight() <= 0) {
            imgMariposa.postDelayed(this::animarMariposa, 200);
            return;
        }

        int maxX = Math.max(
                1,
                parentView.getWidth() - imgMariposa.getWidth()
        );

        int maxY = Math.max(
                1,
                parentView.getHeight() - imgMariposa.getHeight()
        );

        float nextX = random.nextInt(maxX);
        float nextY = random.nextInt(maxY);

        imgMariposa.animate()
                .x(nextX)
                .y(nextY)
                .setDuration(900 + random.nextInt(500))
                .withEndAction(() -> {
                    if (enPersecucion && !retoCompletado) {
                        animarMariposa();
                    }
                })
                .start();
    }

    private void alAtraparMariposa() {
        if (!enPersecucion || retoCompletado || juegoFinalizado) {
            return;
        }

        enPersecucion = false;
        imgMariposa.animate().cancel();

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

    // Entrega los puntos solamente una vez por reto completado.
    private void completarAcertijo() {
        if (retoCompletado || juegoFinalizado || acertijoActual == null) {
            return;
        }

        retoCompletado = true;
        enPersecucion = false;

        if (colorHuntController != null) {
            colorHuntController.setScanningActive(false);
        }

        ParquePuntos.agregarPuntos(this, PUNTOS_POR_RETO);

        String mensaje = acertijoActual.getMensajeExito()
                + "\n\n¡Ganaste " + PUNTOS_POR_RETO + " ParquePuntos!";

        actualizarInterfazDrako(
                mensaje,
                "Siguiente reto",
                v -> {
                    indiceActual++;
                    mostrarAcertijoActual();
                }
        );

        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setText(
                    "Reto completado. Ganaste " + PUNTOS_POR_RETO + " puntos."
            );
        }
    }

    private void actualizarInterfazDrako(
            String textoMensaje,
            String textoBoton,
            View.OnClickListener onClickListener
    ) {
        if (tvAcertijo != null) {
            tvAcertijo.setText(textoMensaje);
        }

        if (btnDrakoAccion != null) {
            if (textoBoton == null || textoBoton.trim().isEmpty()) {
                btnDrakoAccion.setVisibility(View.GONE);
            } else {
                btnDrakoAccion.setText(textoBoton);
                btnDrakoAccion.setOnClickListener(onClickListener);
                btnDrakoAccion.setVisibility(View.VISIBLE);
            }
        }
    }

    private void ocultarBotonAccion() {
        if (btnDrakoAccion != null) {
            btnDrakoAccion.setVisibility(View.GONE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider()
                );

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build();

                imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        imageProxy -> {
                            try {
                                if (colorHuntController.isScanningActive()) {
                                    ContextCompat.getMainExecutor(this).execute(() -> {
                                        if (colorHuntController != null
                                                && colorHuntController.isScanningActive()
                                                && !isFinishing()) {

                                            Bitmap bitmap = previewView.getBitmap();

                                            if (bitmap != null) {
                                                colorHuntController.processFrameBitmap(bitmap);
                                            }
                                        }
                                    });
                                }
                            } finally {
                                imageProxy.close();
                            }
                        }
                );

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                e.printStackTrace();

                runOnUiThread(() -> Toast.makeText(
                        this,
                        "No se pudo iniciar la cámara.",
                        Toast.LENGTH_LONG
                ).show());
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
        if (drakoHelper != null) {
            drakoHelper.detener();
        }

        if (imgMariposa != null) {
            imgMariposa.animate().cancel();
        }

        enPersecucion = false;

        if (colorHuntController != null) {
            colorHuntController.setScanningActive(false);
        }

        super.onPause();
    }
}
