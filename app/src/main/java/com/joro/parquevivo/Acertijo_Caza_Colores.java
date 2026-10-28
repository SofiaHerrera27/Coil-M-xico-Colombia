package com.joro.parquevivo;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    // Control de ejecucion y tiempos
    private final Handler handlerJuego = new Handler(Looper.getMainLooper());
    private boolean monedaDisponible = false;
    private View imgMoneda;
    private Runnable tareaHuida;
    private volatile boolean procesandoFrame = false;
    private static final long TIEMPO_HUIDA_MS = 12000;
    private static final int CAMERA_PERMISSION_CODE = 101;

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

    // Control de progresion de acertijos
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

        // Inicializar gestor de sonido y contexto global
        MusicaManager.init(this);
        progreso = new ProgresoManager(this);

        imgMoneda = findViewById(R.id.imgMoneda);
        if (imgMoneda != null) {
            imgMoneda.setOnClickListener(v -> recolectarMonedaSorpresa());
        }

        // 1. Vincular vistas del XML
        previewView = findViewById(R.id.previewView);
        tvAcertijo = findViewById(R.id.tvAcertijo);
        tvEstadoEscaneo = findViewById(R.id.tvEstadoEscaneo);
        progressBarColor = findViewById(R.id.progressBarColor);
        imgMariposa = findViewById(R.id.imgMariposaPersecucion);

        // Vistas para la animación del trofeo
        layoutTrofeo = findViewById(R.id.layoutTrofeo);
        imgTrofeo = findViewById(R.id.imgTrofeo);
        tvTrofeoNombre = findViewById(R.id.tvTrofeoNombre);
        tvTrofeoPuntos = findViewById(R.id.tvTrofeoPuntos);

        // Navegación directa a la pantalla de Premios al tocar el trofeo
        imgTrofeo.setOnClickListener(v ->
                startActivity(new Intent(this, PremiosActivity.class)));

        View drakoContainer = findViewById(R.id.layout_drako_container);
        ImageView imgDrako = drakoContainer.findViewById(R.id.imgDrako);
        TextView tvGlobo = drakoContainer.findViewById(R.id.tvGlobo);
        btnDrakoAccion = drakoContainer.findViewById(R.id.btnDrakoAccion);

        // 2. Inicializar controladores y sensores
        drakoHelper = new DrakoHelper(
                this,
                imgDrako,
                tvGlobo,
                DrakoHelper.TipoPantalla.CAZA_COLORES
        );

        colorHuntController = new ColorHuntController();
        anclajeSensor = new AnclajeSensor(this);

        // Listener de toque para atrapar al personaje
        imgMariposa.setOnClickListener(v -> alAtraparMariposa());

        // 3. Cargar acertijos e iniciar
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

        if (random.nextInt(100) < 30) { // 30% de probabilidad
            monedaDisponible = true;
            if (imgMoneda != null) {
                imgMoneda.setVisibility(View.VISIBLE);
            }
            MusicaManager.reproducirEfecto(R.raw.sfx_aparece);
            if (tvEstadoEscaneo != null) {
                tvEstadoEscaneo.append("\n¡Apareció una Moneda Sorpresa!");
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

        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.append("\n¡Moneda atrapada! +10 ParquePuntos (total: " + progreso.getPuntos() + ")");
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
        handlerJuego.removeCallbacksAndMessages(null);
        ocultarTrofeo();

        imgMariposa.setVisibility(View.GONE);
        imgMariposa.animate().cancel();
        enPersecucion = false;
        anclado = false;

        if (progressBarColor != null) {
            progressBarColor.setVisibility(View.GONE);
            progressBarColor.setProgress(0);
        }
        if (tvEstadoEscaneo != null) {
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

                                            if (progress > 0 && !enPersecucion) {
                                                tvEstadoEscaneo.setText("¡Detectando color! Mantén fijo (" + progress + "%)");
                                            } else if (!enPersecucion) {
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
        if (enPersecucion) return;
        colorHuntController.setScanningActive(false);
        if (progressBarColor != null) progressBarColor.setVisibility(View.GONE);

        if (tvEstadoEscaneo != null && acertijoActual != null) {
            tvEstadoEscaneo.setText("Shhh... algo se mueve dentro del "
                    + acertijoActual.getColorNombre() + "...");
        }

        handlerJuego.postDelayed(this::iniciarJuegoPersecucion, 800 + random.nextInt(2200));
    }

    private void iniciarJuegoPersecucion() {
        enPersecucion = true;
        MusicaManager.reproducirEfecto(R.raw.sfx_aparece);

        if (acertijoActual != null && acertijoActual.getImagenPersonajeResId() != 0) {
            imgMariposa.setImageResource(acertijoActual.getImagenPersonajeResId());
        }

        if (tvEstadoEscaneo != null) {
            tvEstadoEscaneo.setText("¡Salió! ¡Tócalo antes de que huya!");
        }

        View raiz = findViewById(R.id.rootLayout);
        if (raiz == null) raiz = previewView;

        float[] c = colorHuntController.getCentroColor();
        float cx = (c != null) ? c[0] * raiz.getWidth() : raiz.getWidth() / 2f;
        float cy = (c != null) ? c[1] * raiz.getHeight() : raiz.getHeight() / 2f;

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

        tareaHuida = this::drakoHuye;
        handlerJuego.postDelayed(tareaHuida, TIEMPO_HUIDA_MS);
    }

    private void drakoHuye() {
        if (!enPersecucion) return;
        enPersecucion = false;
        imgMariposa.animate().cancel();
        imgMariposa.setVisibility(View.GONE);

        if (tvEstadoEscaneo != null && acertijoActual != null) {
            tvEstadoEscaneo.setText("¡Se escapó! Busca otro objeto "
                    + acertijoActual.getColorNombre() + " para encontrarlo");
        }

        if (progressBarColor != null) {
            progressBarColor.setProgress(0);
            progressBarColor.setVisibility(View.VISIBLE);
        }

        colorHuntController.setScanningActive(true);
    }

    private void animarMariposa() {
        if (!enPersecucion) return;

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
        if (!tieneOrientacion) return;

        View raiz = findViewById(R.id.rootLayout);
        if (raiz == null) raiz = previewView;

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

        float w = (raiz.getWidth() > 0) ? raiz.getWidth() : getResources().getDisplayMetrics().widthPixels;
        float h = (raiz.getHeight() > 0) ? raiz.getHeight() : getResources().getDisplayMetrics().heightPixels;

        float f = focalPx();
        float x = w / 2f + f * (dx / profundidad);
        float y = h / 2f - f * (dy / profundidad);

        imgMariposa.setVisibility(View.VISIBLE);
        imgMariposa.setX(x - mitadPersonaje());
        imgMariposa.setY(y - mitadPersonaje());
    }

    private void alAtraparMariposa() {
        if (!enPersecucion) return;
        enPersecucion = false;

        handlerJuego.removeCallbacks(tareaHuida);
        imgMariposa.animate().cancel();

        imgMariposa.animate().scaleX(1.3f).scaleY(1.3f).setDuration(250)
                .withEndAction(() -> {
                    imgMariposa.setVisibility(View.GONE);
                    imgMariposa.setScaleX(1f);
                    imgMariposa.setScaleY(1f);

                    String color = acertijoActual.getColorNombre();
                    boolean esNuevo = !progreso.estaCapturado(color);
                    progreso.marcarCapturado(color);

                    mostrarTrofeo(esNuevo);
                    completarAcertijo();
                }).start();
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
        String color = acertijoActual.getColorNombre();
        imgTrofeo.setImageResource(acertijoActual.getImagenPersonajeResId());
        Personaje p = Personaje.desdeColor(color);
        tvTrofeoNombre.setText(p != null ? p.nombre : "Drako");
        tvTrofeoPuntos.setText(esNuevo ? "+50 ParquePuntos" : "Ya estaba en tus premios");
        layoutTrofeo.setVisibility(View.VISIBLE);

        imgTrofeo.setScaleX(0f);
        imgTrofeo.setScaleY(0f);
        imgTrofeo.animate().scaleX(1f).scaleY(1f).setDuration(500)
                .setInterpolator(new OvershootInterpolator()).start();

        iniciarAnimacionesTrofeo();
    }

    private void iniciarAnimacionesTrofeo() {
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
        imgTrofeo.animate().cancel();
        imgTrofeo.setRotationY(0f);
        imgTrofeo.setTranslationY(0f);
        layoutTrofeo.setVisibility(View.GONE);
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
                    if (colorHuntController.isScanningActive() && !procesandoFrame) {
                        procesandoFrame = true;
                        ContextCompat.getMainExecutor(this).execute(() -> {
                            if (colorHuntController.isScanningActive()) {
                                Bitmap bitmapCompleto = previewView.getBitmap();
                                if (bitmapCompleto != null) {
                                    // Extraer únicamente el 35% central de la pantalla
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
        MusicaManager.reanudar(this, MusicaManager.Pista.ACERTIJO);
        if (drakoHelper != null) {
            drakoHelper.iniciarSinSaludo();
        }

        if (anclajeSensor != null && anclajeSensor.disponible()) {
            anclajeSensor.iniciar(r -> {
                System.arraycopy(r, 0, rActual, 0, 9);
                tieneOrientacion = true;
                if (anclado) {
                    runOnUiThread(this::proyectarPersonaje);
                }
            });
        }

        if (layoutTrofeo != null && layoutTrofeo.getVisibility() == View.VISIBLE) {
            iniciarAnimacionesTrofeo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicaManager.pausar();
        if (drakoHelper != null) {
            drakoHelper.detener();
        }
        detenerAnimacionesTrofeo();
        handlerJuego.removeCallbacksAndMessages(null);

        if (anclajeSensor != null) {
            anclajeSensor.detener();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drakoHelper != null) {
            drakoHelper.liberar();
        }
    }
}