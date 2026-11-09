package com.joro.parquevivo;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
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

    private ProgresoManager progreso;
    private View layoutTrofeo;
    private ImageView imgTrofeo;
    private TextView tvTrofeoNombre, tvTrofeoPuntos;
    private ObjectAnimator giroTrofeo, flotarTrofeo;
    private static final long DURACION_GIRO_MS = 5000;
    private long usuarioId = -1;

    // Control de ejecución y tiempos
    private final Handler handlerJuego = new Handler(Looper.getMainLooper());
    private boolean monedaDisponible = false;
    private View imgMoneda;
    private volatile boolean procesandoFrame = false;
    private static final int CAMERA_PERMISSION_CODE = 101;

    // TIEMPOS FIJOS ESTÁTICOS DE JUEGO (EN MILISEGUNDOS)
    private static final long TIEMPO_FASE1_MS = 15000; // 15 segundos fijos para Persecución (5 vidas)
    private static final long TIEMPO_FASE2_MS = 6000;  // 6 segundos fijos por asomada en Escondite (3 vidas)

    // Reloj Temporizador de Cuenta Regresiva
    private CountDownTimer timerJuego;
    private int tiempoRestanteSegundos = 0;

    // Lógica de Vidas por Fases
    private static final int TOQUES_FASE1_TOTAL = 5; // 5 corazones al inicio
    private int toquesFase1Actuales = 0;

    private static final int TOQUES_FASE2_TOTAL = 3; // 3 corazones en el escondite
    private int toquesFase2Actuales = 0;

    private boolean estaEscondido = false;
    private boolean esperandoToquePopUp = false;
    private boolean estaAturdido = false;
    private Runnable runnablePopUp;
    private Runnable runnableReanudarMovimiento;

    // Parte A: Campos de anclaje por sensores
    private AnclajeSensor anclajeSensor;
    private final float[] rActual = new float[9];
    private boolean tieneOrientacion = false;
    private boolean anclado = false;
    private final float[] dirMundo = new float[3];
    private static final float FOV_VERTICAL_GRADOS = 60f;

    // Componentes de interfaz
    private TextView tvAcertijo;
    private TextView tvEstadoEscaneo;
    private ProgressBar progressBarColor;
    private ImageView imgMariposa;
    private PreviewView previewView;

    // Controladores auxiliares
    private DrakoHelper drakoHelper;
    private ColorHuntController colorHuntController;
    private Button btnDrakoAccion;

    // Control de progresión de acertijos
    private List<AcertijoColorModel> listaAcertijos;
    private int indiceActual = 0;
    private AcertijoColorModel acertijoActual;

    // Estado del minijuego
    private boolean enPersecucion = false;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acertijo_caza_colores);

        usuarioId = getIntent().getLongExtra("usuario_id", -1);

        MusicaManager.init(this);
        progreso = new ProgresoManager(this);

        imgMoneda = findViewById(R.id.imgMoneda);
        if (imgMoneda != null) {
            imgMoneda.setOnClickListener(v -> recolectarMonedaSorpresa());
        }

        previewView = findViewById(R.id.previewView);
        tvAcertijo = findViewById(R.id.tvAcertijo);
        tvEstadoEscaneo = findViewById(R.id.tvEstadoEscaneo);
        progressBarColor = findViewById(R.id.progressBarColor);
        imgMariposa = findViewById(R.id.imgMariposaPersecucion);

        // Ampliar área táctil (hitbox) agregando margen transparente interno
        if (imgMariposa != null) {
            int paddingPx = (int) (18 * getResources().getDisplayMetrics().density);
            imgMariposa.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

            // Detección instantánea al presionar (ACTION_DOWN)
            imgMariposa.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    alAtraparMariposa();
                    return true;
                }
                return false;
            });
        }

        layoutTrofeo = findViewById(R.id.layoutTrofeo);
        imgTrofeo = findViewById(R.id.imgTrofeo);
        tvTrofeoNombre = findViewById(R.id.tvTrofeoNombre);
        tvTrofeoPuntos = findViewById(R.id.tvTrofeoPuntos);

        if (imgTrofeo != null) {
            imgTrofeo.setOnClickListener(v -> {
                Intent intent = new Intent(this, PremiosActivity.class);
                intent.putExtra("usuario_id", usuarioId);
                startActivity(intent);
            });
        }

        View drakoContainer = findViewById(R.id.layout_drako_container);
        ImageView imgDrako = drakoContainer.findViewById(R.id.imgDrako);
        TextView tvGlobo = drakoContainer.findViewById(R.id.tvGlobo);
        btnDrakoAccion = drakoContainer.findViewById(R.id.btnDrakoAccion);

        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.CAZA_COLORES
        );

        colorHuntController = new ColorHuntController();
        anclajeSensor = new AnclajeSensor(this);

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

    private void evaluarAparicionMoneda() {
        if (monedaDisponible) return;

        if (random.nextInt(100) < 30) {
            monedaDisponible = true;
            if (imgMoneda != null) {
                imgMoneda.setVisibility(View.VISIBLE);
            }
            MusicaManager.reproducirEfecto(R.raw.sfx_aparece);
            if (drakoHelper != null) {
                drakoHelper.mostrarTextoEspecial("¡Mira! Apareció una Moneda Sorpresa. ¡Tócala!");
            }
        }
    }

    public void recolectarMonedaSorpresa() {
        if (!monedaDisponible) return;

        monedaDisponible = false;

        if (imgMoneda != null) {
            imgMoneda.setVisibility(View.GONE);
        }

        MusicaManager.reproducirEfecto(R.raw.sfx_moneda);
        progreso.sumarPuntos(10);

        if (drakoHelper != null) {
            drakoHelper.mostrarTextoEspecial("¡Moneda atrapada! +10 ParquePuntos (Total: " + progreso.getPuntos() + ")");
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
                "¡Gran agudeza visual! El ROJO abre un camino secreto del parque. +50 ParquePuntos.",
                R.drawable.drako_rojo
        ));
        bancoRojo.add(new AcertijoColorModel(
                1,
                "Soy el color de la llama que calienta la noche y del fruto dulce de las frutillas del bosque. Búscame antes de que el fuego se apague.",
                "ROJO",
                ColorHSVUtils.getLowerHSV("ROJO"),
                ColorHSVUtils.getUpperHSV("ROJO"),
                "¡Increíble deducción! El ROJO revela el camino secreto. +50 ParquePuntos.",
                R.drawable.drako_rojo
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
                "¡Fabuloso! El VERDE demuestra tu conexión con la naturaleza. +50 ParquePuntos.",
                R.drawable.drako_verde
        ));
        bancoVerde.add(new AcertijoColorModel(
                2,
                "Si miras el musgo sobre las rocas o el caparazón de las viejas tortugas del estanque, me verás brillar. ¿Cuál es este color vital?",
                "VERDE",
                ColorHSVUtils.getLowerHSV("VERDE"),
                ColorHSVUtils.getUpperHSV("VERDE"),
                "¡Excelente! Has encontrado la esencia del color VERDE.",
                R.drawable.drako_verde
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
                "¡Perfecto! El AZUL ha restaurado la paz en la fuente del parque.",
                R.drawable.drako_azul
        ));
        bancoAzul.add(new AcertijoColorModel(
                3,
                "No me puedes sujetar con las manos cuando corro por el río, pero si miras al horizonte en un día despejado, me verás en todas partes.",
                "AZUL",
                ColorHSVUtils.getLowerHSV("AZUL"),
                ColorHSVUtils.getUpperHSV("AZUL"),
                "¡Fantástico! El AZUL activa el mecanismo acuático.",
                R.drawable.drako_azul
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
                "¡Brillante! El AMARILLO ilumina un rincón del parque.",
                R.drawable.drako_amarrillo
        ));
        bancoAmarillo.add(new AcertijoColorModel(
                4,
                "Soy el color de las hojas secas en otoño y del rayo de luz que ilumina la mañana. Encuentra mi brillo para completar el reto.",
                "AMARILLO",
                ColorHSVUtils.getLowerHSV("AMARILLO"),
                ColorHSVUtils.getUpperHSV("AMARILLO"),
                "¡Espléndido! Has desbloqueado el poder de la luz dorada.",
                R.drawable.drako_amarrillo
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
                "¡Excelente deducción! El NARANJA enciende la linterna mística de Drako. +50 ParquePuntos.",
                R.drawable.drako_naranja
        ));
        bancoNaranja.add(new AcertijoColorModel(
                5,
                "No soy tan ardiente como el rojo ni tan radiante como el amarillo, pero adorno los atardeceres y la piel de los cítricos del huerto. Encuentra mi calidez.",
                "NARANJA",
                ColorHSVUtils.getLowerHSV("NARANJA"),
                ColorHSVUtils.getUpperHSV("NARANJA"),
                "¡Gran trabajo! El NARANJA ilumina el sendero oculto del parque.",
                R.drawable.drako_naranja
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
                "¡Misterio resuelto! El MORADO revela el secreto de los árboles ancestrales. +50 ParquePuntos.",
                R.drawable.drako_morado
        ));
        bancoMorado.add(new AcertijoColorModel(
                6,
                "Me oculto entre las sombras al atardecer y en los pétalos de las violetas silvestres. Si me enfocas, activarás el antiguo mapa del parque.",
                "MORADO",
                ColorHSVUtils.getLowerHSV("MORADO"),
                ColorHSVUtils.getUpperHSV("MORADO"),
                "¡Increíble! El MORADO ha desbloqueado la runa mágica.",
                R.drawable.drako_morado
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
                "¡Sublime! El ROSA ha restaurado la armonía en el jardín del parque.",
                R.drawable.drako_rosa
        ));
        bancoRosa.add(new AcertijoColorModel(
                7,
                "Nací de la mezcla entre la fuerza del rojo y la pureza del blanco. Me verás en los capullos de primavera y en los dulces algodones. ¿Cuál es mi tono?",
                "ROSA",
                ColorHSVUtils.getLowerHSV("ROSA"),
                ColorHSVUtils.getUpperHSV("ROSA"),
                "¡Genial! Has encontrado el ROSA del jardín.",
                R.drawable.drako_rosa
        ));
        listaAcertijos.add(bancoRosa.get(random.nextInt(bancoRosa.size())));

        Collections.shuffle(listaAcertijos);
    }

    private void mostrarAcertijoActual() {
        cancelarTimer();
        handlerJuego.removeCallbacksAndMessages(null);
        ocultarTrofeo();

        estaEscondido = false;
        esperandoToquePopUp = false;
        estaAturdido = false;
        toquesFase1Actuales = 0;
        toquesFase2Actuales = 0;

        if (imgMariposa != null) {
            imgMariposa.setVisibility(View.GONE);
            imgMariposa.animate().cancel();
        }
        enPersecucion = false;
        anclado = false;

        if (progressBarColor != null) {
            progressBarColor.setVisibility(View.GONE);
            progressBarColor.setProgress(0);
        }
        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setTextColor(Color.WHITE);
            tvEstadoEscaneo.setText("Presiona '¡Buscar Color!' para iniciar");
        }

        if (indiceActual < listaAcertijos.size()) {
            evaluarAparicionMoneda();

            drakoHelper.mostrarTextoEspecial("¡Lee el acertijo de arriba, explorador!");
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
                            tvEstadoEscaneo.setTextColor(Color.WHITE);
                            tvEstadoEscaneo.setText("Apunta el cuadro central hacia el color objetivo...");
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

                                            if (progress > 0 && !enPersecucion && !esperandoToquePopUp && tvEstadoEscaneo != null) {
                                                tvEstadoEscaneo.setText("¡Detectando color! Mantén fijo (" + progress + "%)");
                                            } else if (!enPersecucion && !esperandoToquePopUp && tvEstadoEscaneo != null) {
                                                tvEstadoEscaneo.setText("Caza de colores iniciada, buscando...");
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
                    "VICTORIA TOTAL: ¡Has completado todos los acertijos de color del parque!",
                    "Finalizar",
                    v -> finish()
            );
        }
    }

    private void alDetectarColorExitoso() {
        if (enPersecucion || esperandoToquePopUp) return;
        colorHuntController.setScanningActive(false);
        if (progressBarColor != null) progressBarColor.setVisibility(View.GONE);

        if (estaEscondido) {
            if (tvEstadoEscaneo != null && acertijoActual != null) {
                tvEstadoEscaneo.setText("Shhh... ¡está escondido en el "
                        + acertijoActual.getColorNombre() + "! Prepara tu dedo...");
            }
            programarPopUpEscondido();
        } else {
            if (tvEstadoEscaneo != null && acertijoActual != null) {
                tvEstadoEscaneo.setText("Shhh... algo se mueve dentro del "
                        + acertijoActual.getColorNombre() + "...");
            }
            handlerJuego.postDelayed(this::iniciarJuegoPersecucion, 800 + random.nextInt(2200));
        }
    }

    // --- FASE 1: PRIMERA APARICIÓN (5 VIDAS + RELOJ FIJO DE 15s) ---
    private void iniciarJuegoPersecucion() {
        if (imgMariposa == null) return;
        enPersecucion = true;
        esperandoToquePopUp = false;
        estaAturdido = false;
        toquesFase1Actuales = 0;

        MusicaManager.reproducirEfecto(R.raw.sfx_aparece);

        if (acertijoActual != null && acertijoActual.getImagenPersonajeResId() != 0) {
            imgMariposa.setImageResource(acertijoActual.getImagenPersonajeResId());
        }

        View raiz = findViewById(R.id.rootLayout);
        if (raiz == null) raiz = previewView;

        float[] c = colorHuntController.getCentroColor();
        float cx = (c != null && raiz != null) ? c[0] * raiz.getWidth() : (raiz != null ? raiz.getWidth() / 2f : 500f);
        float cy = (c != null && raiz != null) ? c[1] * raiz.getHeight() : (raiz != null ? raiz.getHeight() / 2f : 500f);

        imgMariposa.setScaleX(0.2f);
        imgMariposa.setScaleY(0.2f);
        imgMariposa.setX(cx - mitadPersonaje());
        imgMariposa.setY(cy - mitadPersonaje());
        imgMariposa.setVisibility(View.VISIBLE);

        imgMariposa.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(350)
                .withEndAction(this::animarMariposa)
                .start();

        // Reloj fijo de 15 segundos para la Fase 1
        iniciarTimerFase1(TIEMPO_FASE1_MS);
    }

    private void iniciarTimerFase1(long duracionMs) {
        cancelarTimer();
        timerJuego = new CountDownTimer(duracionMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteSegundos = (int) Math.ceil(millisUntilFinished / 1000.0);
                actualizarTextoEstado();
            }

            @Override
            public void onFinish() {
                if (enPersecucion && !esperandoToquePopUp) {
                    drakoHuye();
                }
            }
        }.start();
    }

    private void drakoHuye() {
        if (!enPersecucion) return;
        cancelarTimer();
        enPersecucion = false;

        if (imgMariposa != null) {
            imgMariposa.animate().cancel();
            imgMariposa.setVisibility(View.GONE);
        }

        estaEscondido = true; // Activa la 2da escapatoria (modo escondite Pop-up)

        if (tvEstadoEscaneo != null && acertijoActual != null) {
            tvEstadoEscaneo.setTextColor(Color.WHITE);
            tvEstadoEscaneo.setText("¡Se escondió! Apunta de nuevo al color "
                    + acertijoActual.getColorNombre() + " para encontrar su escondite...");
        }

        if (drakoHelper != null && acertijoActual != null) {
            drakoHelper.mostrarTextoEspecial("¡Se escondió! Re-enfoca el color " + acertijoActual.getColorNombre() + " para que asome la cabeza.");
        }

        if (progressBarColor != null) {
            progressBarColor.setProgress(0);
            progressBarColor.setVisibility(View.VISIBLE);
        }

        colorHuntController.setScanningActive(true);
    }

    // --- FASE 2: MODO ESCONDITE / POP-UP (3 VIDAS + RELOJ FIJO DE 6s POR INTENTO) ---
    private void programarPopUpEscondido() {
        if (runnablePopUp != null) handlerJuego.removeCallbacks(runnablePopUp);

        int tiempoEspera = (2 + random.nextInt(3)) * 1000;
        runnablePopUp = this::mostrarCabecitaEmergente;
        handlerJuego.postDelayed(runnablePopUp, tiempoEspera);
    }

    private void mostrarCabecitaEmergente() {
        if (imgMariposa == null) return;
        toquesFase2Actuales = 0;
        esperandoToquePopUp = true;
        enPersecucion = true;
        estaAturdido = false;

        MusicaManager.reproducirEfecto(R.raw.sfx_aparece);

        if (acertijoActual != null && acertijoActual.getImagenPersonajeResId() != 0) {
            imgMariposa.setImageResource(acertijoActual.getImagenPersonajeResId());
        }

        View raiz = findViewById(R.id.rootLayout);
        if (raiz == null) raiz = previewView;

        float[] c = colorHuntController.getCentroColor();
        float cx = (c != null && raiz != null) ? c[0] * raiz.getWidth() : (raiz != null ? raiz.getWidth() / 2f : 500f);
        float cy = (c != null && raiz != null) ? c[1] * raiz.getHeight() : (raiz != null ? raiz.getHeight() / 2f : 500f);

        imgMariposa.setX(cx - mitadPersonaje());
        imgMariposa.setY(cy - mitadPersonaje());
        imgMariposa.setVisibility(View.VISIBLE);
        imgMariposa.setScaleX(0.2f);
        imgMariposa.setScaleY(0.2f);

        imgMariposa.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Reloj fijo de 6 segundos para dar los 3 toques en el escondite
        iniciarTimerFase2(TIEMPO_FASE2_MS);
    }

    private void iniciarTimerFase2(long duracionMs) {
        cancelarTimer();
        timerJuego = new CountDownTimer(duracionMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteSegundos = (int) Math.ceil(millisUntilFinished / 1000.0);
                actualizarTextoEstado();
            }

            @Override
            public void onFinish() {
                if (esperandoToquePopUp) {
                    esperandoToquePopUp = false;
                    enPersecucion = false;

                    imgMariposa.animate()
                            .scaleX(0f)
                            .scaleY(0f)
                            .setDuration(250)
                            .withEndAction(() -> imgMariposa.setVisibility(View.GONE))
                            .start();

                    if (tvEstadoEscaneo != null) {
                        tvEstadoEscaneo.setTextColor(Color.WHITE);
                        tvEstadoEscaneo.setText("¡Se volvió a esconder! Sigue buscando...");
                    }

                    programarPopUpEscondido();
                }
            }
        }.start();
    }

    // --- ANIMACIÓN CON TURBO Y MEJORA DE ATURDIMIENTO (STUN) ---
    private void animarMariposa() {
        if (!enPersecucion || imgMariposa == null || esperandoToquePopUp || estaAturdido) return;

        View parentView = (View) imgMariposa.getParent();
        if (parentView == null) return;

        int minY = 250;
        View layoutEstado = findViewById(R.id.layoutEstadoBusqueda);
        if (layoutEstado != null) {
            minY = layoutEstado.getBottom() + 16;
        }

        int maxX = Math.max(100, parentView.getWidth() - imgMariposa.getWidth() - 100);
        int maxY = Math.max(minY + 50, parentView.getHeight() - imgMariposa.getHeight() - 300);

        float nextX = random.nextInt(Math.max(1, maxX));
        float nextY = minY + random.nextInt(Math.max(1, Math.max(1, maxY - minY)));

        // Turbo de velocidad al llegar a las últimas 2 vidas (toques >= 3)
        long duracion = (toquesFase1Actuales >= 3) ? (350 + random.nextInt(250)) : (900 + random.nextInt(500));

        imgMariposa.animate()
                .x(nextX)
                .y(nextY)
                .setDuration(duracion)
                .withEndAction(() -> {
                    if (enPersecucion && !esperandoToquePopUp && !estaAturdido) {
                        animarMariposa();
                    }
                })
                .start();
    }

    // --- LÓGICA DE IMPACTO CON ATURDIMIENTO (STUN) DE 300ms ---
    private void alAtraparMariposa() {
        if ((!enPersecucion && !esperandoToquePopUp) || imgMariposa == null) return;

        MusicaManager.reproducirEfecto(R.raw.sfx_moneda);

        // MEJORA: Pausa de 300ms ("Stun") para facilitar los toques seguidos
        estaAturdido = true;
        imgMariposa.animate().cancel();

        imgMariposa.setScaleX(1.35f);
        imgMariposa.setScaleY(1.35f);

        if (runnableReanudarMovimiento != null) {
            handlerJuego.removeCallbacks(runnableReanudarMovimiento);
        }

        runnableReanudarMovimiento = () -> {
            if (imgMariposa != null && enPersecucion) {
                imgMariposa.setScaleX(1.0f);
                imgMariposa.setScaleY(1.0f);
                estaAturdido = false;
                if (!esperandoToquePopUp) {
                    animarMariposa();
                }
            }
        };
        handlerJuego.postDelayed(runnableReanudarMovimiento, 300);

        if (esperandoToquePopUp) {
            // FASE 2 (Escondite - 3 corazones)
            toquesFase2Actuales++;
            int faltantes = TOQUES_FASE2_TOTAL - toquesFase2Actuales;
            actualizarTextoEstado();
            if (faltantes > 0) return;
        } else {
            // FASE 1 (Persecución - 5 corazones)
            toquesFase1Actuales++;
            int faltantes = TOQUES_FASE1_TOTAL - toquesFase1Actuales;
            actualizarTextoEstado();
            if (faltantes > 0) return;
        }

        // --- SI COMPLETÓ TODAS LAS VIDAS DE LA FASE CORRESPONDIENTE ---
        cancelarTimer();
        enPersecucion = false;
        esperandoToquePopUp = false;

        if (runnablePopUp != null) handlerJuego.removeCallbacks(runnablePopUp);
        if (runnableReanudarMovimiento != null) handlerJuego.removeCallbacks(runnableReanudarMovimiento);

        imgMariposa.animate().cancel();
        imgMariposa.animate().scaleX(1.4f).scaleY(1.4f).setDuration(250)
                .withEndAction(() -> {
                    imgMariposa.setVisibility(View.GONE);
                    imgMariposa.setScaleX(1f);
                    imgMariposa.setScaleY(1f);

                    String color = acertijoActual.getColorNombre();
                    boolean esNuevo = !progreso.estaCapturado(color);
                    progreso.marcarCapturado(color);
                    if (esNuevo) {
                        progreso.sumarPuntos(50);
                    }

                    mostrarTrofeo(esNuevo);
                    completarAcertijo();
                }).start();
    }

    // --- ACTUALIZACIÓN VISUAL Y ALERTA DE TIEMPO CRÍTICO ---
    private void actualizarTextoEstado() {
        if (tvEstadoEscaneo == null) return;

        // MEJORA: Cambio a color ROJO en los últimos 3 segundos
        if (tiempoRestanteSegundos <= 3) {
            tvEstadoEscaneo.setTextColor(Color.RED);
        } else {
            tvEstadoEscaneo.setTextColor(Color.WHITE);
        }

        if (esperandoToquePopUp) {
            int faltantes = TOQUES_FASE2_TOTAL - toquesFase2Actuales;
            String corazones = getCorazonesTexto(faltantes);
            String alerta = (tiempoRestanteSegundos <= 3) ? "⚠️ ¡TIEMPO CRÍTICO! " : "";
            tvEstadoEscaneo.setText("⏱️ " + tiempoRestanteSegundos + "s | " + alerta + "⚡ ¡AHÍ ESTÁ! Faltan " + faltantes + " toques (" + corazones + ")");
        } else if (enPersecucion) {
            int faltantes = TOQUES_FASE1_TOTAL - toquesFase1Actuales;
            String corazones = getCorazonesTexto(faltantes);
            String alerta = (tiempoRestanteSegundos <= 3) ? "⚠️ ¡RAPIDO! " : "";
            if (toquesFase1Actuales >= 3) {
                tvEstadoEscaneo.setText("⏱️ " + tiempoRestanteSegundos + "s | " + alerta + "⚡ ¡TURBO! Faltan " + faltantes + " (" + corazones + ")");
            } else {
                tvEstadoEscaneo.setText("⏱️ " + tiempoRestanteSegundos + "s | " + alerta + "⚡ ¡TÓCALO! Faltan " + faltantes + " toques (" + corazones + ")");
            }
        }
    }

    private String getCorazonesTexto(int cantidad) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cantidad; i++) {
            sb.append("❤️");
        }
        return sb.toString();
    }

    private void cancelarTimer() {
        if (timerJuego != null) {
            timerJuego.cancel();
            timerJuego = null;
        }
    }

    private float mitadPersonaje() {
        return 45f * getResources().getDisplayMetrics().density;
    }

    private float focalPx() {
        View raiz = findViewById(R.id.rootLayout);
        float alto = (raiz != null && raiz.getHeight() > 0)
                ? raiz.getHeight()
                : getResources().getDisplayMetrics().heightPixels;
        return (alto / 2f) / (float) Math.tan(Math.toRadians(FOV_VERTICAL_GRADOS / 2f));
    }

    private void anclarPersonaje() {
        if (!tieneOrientacion || imgMariposa == null) return;

        View raiz = findViewById(R.id.rootLayout);
        if (raiz == null) raiz = previewView;
        if (raiz == null) return;

        float f = focalPx();
        float w = (raiz.getWidth() > 0) ? raiz.getWidth() : getResources().getDisplayMetrics().widthPixels;
        float h = (raiz.getHeight() > 0) ? raiz.getHeight() : getResources().getDisplayMetrics().heightPixels;

        float px = (imgMariposa.getX() + mitadPersonaje() - w / 2f) / f;
        float py = (h / 2f - (imgMariposa.getY() + mitadPersonaje())) / f;

        float[] r = rActual;
        dirMundo[0] = r[0] * px + r[1] * py - r[2];
        dirMundo[1] = r[3] * px + r[4] * py - r[5];
        dirMundo[2] = r[6] * px + r[7] * py - r[8];

        float n = (float) Math.sqrt(dirMundo[0] * dirMundo[0]
                + dirMundo[1] * dirMundo[1] + dirMundo[2] * dirMundo[2]);

        if (n > 0.0001f) {
            for (int i = 0; i < 3; i++) dirMundo[i] /= n;
            anclado = true;
        }
    }

    private void proyectarPersonaje() {
        if (imgMariposa == null) return;
        float[] r = rActual;
        float dx = r[0] * dirMundo[0] + r[3] * dirMundo[1] + r[6] * dirMundo[2];
        float dy = r[1] * dirMundo[0] + r[4] * dirMundo[1] + r[7] * dirMundo[2];
        float profundidad = -(r[2] * dirMundo[0] + r[5] * dirMundo[1] + r[8] * dirMundo[2]);

        if (profundidad < 0.1f) {
            imgMariposa.setVisibility(View.INVISIBLE);
            return;
        }

        View raiz = findViewById(R.id.rootLayout);
        if (raiz == null) raiz = previewView;
        if (raiz == null) return;

        float w = (raiz.getWidth() > 0) ? raiz.getWidth() : getResources().getDisplayMetrics().widthPixels;
        float h = (raiz.getHeight() > 0) ? raiz.getHeight() : getResources().getDisplayMetrics().heightPixels;

        float f = focalPx();
        float x = w / 2f + f * (dx / profundidad);
        float y = h / 2f - f * (dy / profundidad);

        imgMariposa.setVisibility(View.VISIBLE);
        imgMariposa.setX(x - mitadPersonaje());
        imgMariposa.setY(y - mitadPersonaje());
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

    private void mostrarTrofeo(boolean esNuevo) {
        MusicaManager.reproducirEfecto(R.raw.sfx_victoria);
        if (acertijoActual != null && imgTrofeo != null) {
            String color = acertijoActual.getColorNombre();
            imgTrofeo.setImageResource(acertijoActual.getImagenPersonajeResId());
            Personaje p = Personaje.desdeColor(color);
            if (tvTrofeoNombre != null) {
                tvTrofeoNombre.setText(p != null ? p.nombre : "Drako");
            }
            if (tvTrofeoPuntos != null) {
                tvTrofeoPuntos.setText(esNuevo ? "+50 ParquePuntos" : "Ya estaba en tus premios");
            }
        }
        if (layoutTrofeo != null) {
            layoutTrofeo.setVisibility(View.VISIBLE);
        }

        if (imgTrofeo != null) {
            imgTrofeo.setScaleX(0f);
            imgTrofeo.setScaleY(0f);
            imgTrofeo.animate().scaleX(1f).scaleY(1f).setDuration(500)
                    .setInterpolator(new OvershootInterpolator()).start();
        }

        iniciarAnimacionesTrofeo();
    }

    private void iniciarAnimacionesTrofeo() {
        if (imgTrofeo == null) return;
        float d = getResources().getDisplayMetrics().density;
        imgTrofeo.setCameraDistance(8000f * d);
        detenerAnimacionesTrofeo();

        giroTrofeo = ObjectAnimator.ofFloat(imgTrofeo, View.ROTATION_Y, 0f, 360f);
        giroTrofeo.setDuration(DURACION_GIRO_MS);
        giroTrofeo.setInterpolator(new LinearInterpolator());
        giroTrofeo.setRepeatCount(ValueAnimator.INFINITE);
        giroTrofeo.start();

        flotarTrofeo = ObjectAnimator.ofFloat(imgTrofeo, View.TRANSLATION_Y, 0f, -20f * d);
        flotarTrofeo.setDuration(1600);
        flotarTrofeo.setRepeatMode(ValueAnimator.REVERSE);
        flotarTrofeo.setRepeatCount(ValueAnimator.INFINITE);
        flotarTrofeo.start();
    }

    private void detenerAnimacionesTrofeo() {
        if (giroTrofeo != null) giroTrofeo.cancel();
        if (flotarTrofeo != null) flotarTrofeo.cancel();
    }

    private void ocultarTrofeo() {
        detenerAnimacionesTrofeo();
        if (imgTrofeo != null) {
            imgTrofeo.animate().cancel();
            imgTrofeo.setRotationY(0f);
            imgTrofeo.setTranslationY(0f);
        }
        if (layoutTrofeo != null) {
            layoutTrofeo.setVisibility(View.GONE);
        }
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
                if (previewView != null) {
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                }

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
                    if (colorHuntController.isScanningActive() && !procesandoFrame) {
                        procesandoFrame = true;
                        ContextCompat.getMainExecutor(this).execute(() -> {
                            if (colorHuntController.isScanningActive() && previewView != null) {
                                Bitmap bitmapCompleto = previewView.getBitmap();
                                if (bitmapCompleto != null) {
                                    int width = bitmapCompleto.getWidth();
                                    int height = bitmapCompleto.getHeight();
                                    int cropWidth = (int) (width * 0.35f);
                                    int cropHeight = (int) (height * 0.35f);
                                    int startX = (width - cropWidth) / 2;
                                    int startY = (height - cropHeight) / 2;

                                    Bitmap recuadroCentro = Bitmap.createBitmap(
                                            bitmapCompleto, startX, startY, cropWidth, cropHeight
                                    );

                                    colorHuntController.processFrameBitmap(recuadroCentro);
                                }
                            }
                            procesandoFrame = false;
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
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);
        if (drakoHelper != null) {
            drakoHelper.iniciar();
        }
        if (anclajeSensor != null) {
            anclajeSensor.iniciar(r -> {
                System.arraycopy(r, 0, rActual, 0, 9);
                tieneOrientacion = true;
                if (enPersecucion && !anclado && !esperandoToquePopUp) {
                    anclarPersonaje();
                } else if (enPersecucion && anclado && !esperandoToquePopUp) {
                    proyectarPersonaje();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelarTimer();
        MusicaManager.pausar();
        if (drakoHelper != null) {
            drakoHelper.detener();
        }
        if (anclajeSensor != null) {
            anclajeSensor.detener();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelarTimer();
        if (drakoHelper != null) {
            drakoHelper.liberar();
        }
        handlerJuego.removeCallbacksAndMessages(null);
    }
}