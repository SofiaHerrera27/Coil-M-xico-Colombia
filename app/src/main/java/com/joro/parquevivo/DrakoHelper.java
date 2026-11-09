package com.joro.parquevivo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class DrakoHelper implements TextToSpeech.OnInitListener {

    private MediaPlayer vozLocal;
    private TextToSpeech tts;
    private boolean ttsListo = false;

    // --- Control de Anti-Spam y Tiempos ---
    private long ultimoClickTime = 0;
    private int contadorClicksRapidos = 0;
    private static final long TIEMPO_ENTRE_HABLA_MS = 2500; // Cooldown de 2.5s

    public enum TipoPantalla {
        LOGIN,
        REGISTRO,
        INICIO,
        CAZA_COLORES,
        ALBUM,
        TRIVIA,
        PREMIOS,
        CANJE
    }

    private final Context context;
    private final ImageView imgDrako;
    private final TextView tvGlobo;
    private final TipoPantalla tipoPantalla;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    // Sprites de parpadeo (Cambiados a no-finales para actualizar con cada traje)
    private Drawable abierto;
    private Drawable medio;
    private Drawable cerrado;

    private static final Map<TipoPantalla, Integer> indicesMensajes = new HashMap<>();

    public DrakoHelper(Context context,
                       ImageView imgDrako,
                       TextView tvGlobo,
                       TipoPantalla tipoPantalla) {

        this.context = context;
        this.imgDrako = imgDrako;
        this.tvGlobo = tvGlobo;
        this.tipoPantalla = tipoPantalla;

        // Carga la vestimenta inicial de Drako guardada en el progreso
        cargarSpritesSegunTraje();

        if (!indicesMensajes.containsKey(tipoPantalla)) {
            indicesMensajes.put(tipoPantalla, 0);
        }

        if (this.imgDrako != null) {
            this.imgDrako.setOnClickListener(v -> alHacerClickEnDrako());
        }

        // Inicializar motor de voz TTS
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    /**
     * Revisa en ProgresoManager cuál traje está equipado y asigna las imágenes correspondientes.
     */
    public void cargarSpritesSegunTraje() {
        ProgresoManager progreso = new ProgresoManager(context);
        String traje = progreso.getTrajeEquipado().toLowerCase().trim();

        int resAbierto;
        int resMedio;
        int resCerrado;

        switch (traje) {
            case "sombrero_explorador":
                resAbierto = R.drawable.dragon_sombrero;
                resMedio = R.drawable.dragon_sombrero_medio;
                resCerrado = R.drawable.dragon_sombrero_cerrados;
                break;

            case "gorra_guardabosques":
                resAbierto = R.drawable.drago_guardab;
                resMedio = R.drawable.drago_guardab_medio;
                resCerrado = R.drawable.drago_guardab_cerrados;
                break;

            case "mochila_exploradora":
                resAbierto = R.drawable.drago_mochila;
                resMedio = R.drawable.drago_mochila_medio;
                resCerrado = R.drawable.drago_mochila_cerrados;
                break;

            case "brujula_dorada":
                resAbierto = R.drawable.drago_brujula;
                resMedio = R.drawable.drago_brujula_medio;
                resCerrado = R.drawable.drago_brujula_cerrados;
                break;

            case "mariposa_magica":
                resAbierto = R.drawable.drago_mariposa;
                resMedio = R.drawable.drago_mariposa_medio;
                resCerrado = R.drawable.drago_mariposa_cerrados;
                break;

            case "sombrero_mago":
                resAbierto = R.drawable.drago_smago;
                resMedio = R.drawable.drago_smago_medio;
                resCerrado = R.drawable.drago_smago_cerrados;
                break;

            case "cristal_energia":
                resAbierto = R.drawable.drago_cristal;
                resMedio = R.drawable.drago_cristal_medio;
                resCerrado = R.drawable.drago_cristal_cerrados;
                break;

            case "zorrito_bosque":
                resAbierto = R.drawable.drago_zorro;
                resMedio = R.drawable.drago_zorro_medio;
                resCerrado = R.drawable.drago_zorro_cerrados;
                break;

            case "buho_sabio":
                resAbierto = R.drawable.drago_buho;
                resMedio = R.drawable.drago_buho_medio;
                resCerrado = R.drawable.drago_buho_cerrados;
                break;

            case "medalla_ecologica":
                resAbierto = R.drawable.drago_medalla;
                resMedio = R.drawable.drago_medalla_medio;
                resCerrado = R.drawable.drago_medalla_cerrados;
                break;

            case "corona_dorada":
                resAbierto = R.drawable.drago_corona;
                resMedio = R.drawable.drago_corona_medio;
                resCerrado = R.drawable.drago_corona_cerrados;
                break;

            case "alas_magicas":
                resAbierto = R.drawable.drago_alas_mariposa;
                resMedio = R.drawable.drago_alas_mariposa_medio;
                resCerrado = R.drawable.drago_alas_mariposa_cerrados;
                break;

            case "dragon_bebe":
                resAbierto = R.drawable.drago_bebe;
                resMedio = R.drawable.drago_bebe_medio;
                resCerrado = R.drawable.drago_bebe_cerrados;
                break;

            case "tunica_magica":
                resAbierto = R.drawable.drago_tunica;
                resMedio = R.drawable.drago_tunica_medio;
                resCerrado = R.drawable.drago_tunica_cerrados;
                break;

            case "guardian_bosque":
                resAbierto = R.drawable.drago_guardian;
                resMedio = R.drawable.drago_guardian_medio;
                resCerrado = R.drawable.drago_guardian_cerrados;
                break;

            case "traje_aventurero":
                resAbierto = R.drawable.drago_aventurero;
                resMedio = R.drawable.drago_aventurero_medio;
                resCerrado = R.drawable.drago_aventurero_cerrados;
                break;

            case "varita_natural":
                resAbierto = R.drawable.drago_varita;
                resMedio = R.drawable.drago_varita_medio;
                resCerrado = R.drawable.drago_varita_cerrados;
                break;

            case "cofre_misterioso":
                resAbierto = R.drawable.drago_cofre;
                resMedio = R.drawable.drago_cofre_medio;
                resCerrado = R.drawable.drago_cofre_cerrados;
                break;

            case "armadura_dorada":
                resAbierto = R.drawable.drago_armadura_caballero;
                resMedio = R.drawable.drago_armadura_caballero_medio;
                resCerrado = R.drawable.drago_armadura_caballero_cerrados;
                break;

            case "alas_celestiales":
                resAbierto = R.drawable.drago_alas_doradas;
                resMedio = R.drawable.drago_alas_doradas_medio;
                resCerrado = R.drawable.drago_alas_doradas_cerrados;
                break;

            default:
                resAbierto = R.drawable.drako;
                resMedio = R.drawable.drako_medio;
                resCerrado = R.drawable.drako_cerrado;
                break;
        }

        abierto = ContextCompat.getDrawable(context, resAbierto);
        medio = ContextCompat.getDrawable(context, resMedio);
        cerrado = ContextCompat.getDrawable(context, resCerrado);

        if (abierto == null) abierto = ContextCompat.getDrawable(context, R.drawable.drako);
        if (medio == null) medio = ContextCompat.getDrawable(context, R.drawable.drako_medio);
        if (cerrado == null) cerrado = ContextCompat.getDrawable(context, R.drawable.drako_cerrado);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("es", "CO"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts.setLanguage(new Locale("es", "ES"));
            }

            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsListo = true;
                tts.setPitch(1.35f);       // Tono más agudo para voz de dragoncito
                tts.setSpeechRate(0.95f);   // Velocidad cómoda de lectura

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        MusicaManager.bajarVolumen();
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        MusicaManager.subirVolumen();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        MusicaManager.subirVolumen();
                    }
                });

                if (tvGlobo != null && tvGlobo.getVisibility() == View.VISIBLE) {
                    String textoInicial = tvGlobo.getText().toString();
                    if (!textoInicial.isEmpty()) {
                        handler.post(() -> hablar(textoInicial));
                    }
                }
            }
        }
    }

    // LÓGICA ANTI-SATURACIÓN DE CLICS
    private void alHacerClickEnDrako() {
        long tiempoActual = SystemClock.elapsedRealtime();
        long tiempoTranscurrido = tiempoActual - ultimoClickTime;

        if (tiempoTranscurrido < 1200) {
            contadorClicksRapidos++;
        } else if (tiempoTranscurrido > 3000) {
            contadorClicksRapidos = 0;
        }

        if (contadorClicksRapidos >= 3) {
            ultimoClickTime = tiempoActual;
            contadorClicksRapidos = 0;
            decirMensajeGracioso();
            return;
        }

        if (tiempoTranscurrido < TIEMPO_ENTRE_HABLA_MS) {
            return;
        }

        ultimoClickTime = tiempoActual;
        contadorClicksRapidos = 0;
        darConsejoOSaludo();
    }

    private void decirMensajeGracioso() {
        String[] frasesSpam = {
                "¡Oye, me das cosquillas! Ve más despacio.",
                "¡Ey! Dame un segundito para tomar aire.",
                "¡Jajaja, alto ahí! No me presiones tan rápido.",
                "¡Despacio, aventurero! Me vas a marear.",
                "¡Uf! Espera a que termine de hablar."
        };

        String frase = frasesSpam[random.nextInt(frasesSpam.length)];
        mostrarTextoEspecial(frase);
    }

    // PARPADEO CORREGIDO
    private void parpadear(int paso) {
        Drawable[] frames = {medio, cerrado, medio, abierto};
        long[] pausas = {50, 70, 50};

        if (imgDrako == null) return;

        imgDrako.setImageDrawable(frames[paso]);

        // Sintaxis corregida: (paso < frames.length - 1)
        if (paso < frames.length - 1) {
            handler.postDelayed(() -> parpadear(paso + 1), pausas[paso]);
        } else {
            programarSiguiente();
        }
    }

    private void programarSiguiente() {
        handler.postDelayed(() -> parpadear(0), random.nextInt(3500) + 2500);
    }

    public void iniciar() {
        cargarSpritesSegunTraje(); // Refresca las imágenes por si se cambió de traje recientemente
        handler.removeCallbacksAndMessages(null);
        darConsejoOSaludo();
        handler.postDelayed(() -> parpadear(0), 1500);
    }

    public void iniciarSinSaludo() {
        cargarSpritesSegunTraje();
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> parpadear(0), 1500);
    }

    public void detener() {
        detenerVoz();
        handler.removeCallbacksAndMessages(null);
        if (imgDrako != null && abierto != null) {
            imgDrako.setImageDrawable(abierto);
        }
    }

    public void hablar(String texto) {
        detenerVoz();
        if (texto == null || texto.trim().isEmpty()) return;

        if (ttsListo && tts != null) {
            String textoLimpio = texto.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").trim();
            tts.speak(textoLimpio, TextToSpeech.QUEUE_FLUSH, null, "drako_speech");
        }
    }

    public void reproducirVozLocal(int rawId) {
        detenerVoz();
        if (rawId == 0) return;
        vozLocal = MediaPlayer.create(context, rawId);
        if (vozLocal == null) return;
        MusicaManager.bajarVolumen();
        vozLocal.setOnCompletionListener(mp -> detenerVoz());
        vozLocal.start();
    }

    private void detenerVoz() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
        if (vozLocal != null) {
            try {
                if (vozLocal.isPlaying()) vozLocal.stop();
                vozLocal.release();
            } catch (Exception ignored) {}
            vozLocal = null;
        }
        MusicaManager.subirVolumen();
    }

    public void liberar() {
        detenerVoz();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }

    private String[] obtenerMensajes() {
        switch (tipoPantalla) {
            case LOGIN:
                return new String[]{
                        "¡Hola! Ingresa tu cuenta para guardar tus ParquePuntos.",
                        "Al iniciar sesión tus avances y criaturas guardadas estarán seguros.",
                        "¡Escribe tus datos y exploremos el parque juntos!",
                        "¿Primera vez aquí? Recuerda registrarte para unirte a la aventura."
                };
            case REGISTRO:
                return new String[]{
                        "¡Qué bueno que te unes! Escribe tu nombre para empezar.",
                        "Usa un correo que recuerdes, ahí guardaremos tu aventura.",
                        "Tu contraseña debe tener al menos 6 caracteres. ¡Que sea secreta!",
                        "Repite la contraseña para confirmarla y listo."
                };
            case INICIO:
                return new String[]{
                        "¡Bienvenido a ParqueVivo! Elige una actividad para empezar.",
                        "¡Prueba la 'Caza de Colores' para encontrar criaturas mágicas!",
                        "Responde la 'Trivia' para demostrar cuánto sabes sobre el parque.",
                        "¡Revisa tus ParquePuntos arriba para ver tu nivel actual!",
                        "¡Caminar y explorar el parque suma experiencia para tus logros!"
                };
            case CAZA_COLORES:
                return new String[]{
                        "¡Lee con atención el acertijo para descubrir el color oculto!",
                        "Apunta la cámara a elementos del parque (flores, hojas, objetos).",
                        "Mantén la cámara estable sobre el color durante 2 segundos.",
                        "¡Si no aciertas a la tercera, te daré una pista directa!",
                        "¡Cada color desbloquea una criatura única en tu Álbum!"
                };
            case ALBUM:
                return new String[]{
                        "¡Aquí están todas las criaturas que has atrapado en el parque!",
                        "Las casillas bloqueadas indican criaturas que aún debes encontrar.",
                        "¡Vuelve a la Caza de Colores para completar toda tu colección!",
                        "Cada criatura representa un elemento especial de la naturaleza."
                };
            case TRIVIA:
                return new String[]{
                        "¡Demuestra que eres un gran Guardián del Parque!",
                        "Cuidar las plantas y no tirar basura te da puntos extras.",
                        "Lee bien cada pregunta antes de seleccionar la respuesta.",
                        "¡Aprender sobre naturaleza ayuda a proteger nuestro planeta!"
                };
            case PREMIOS:
                return new String[]{
                        "¡Mira todas las criaturas 3D que has desbloqueado!",
                        "Toca cualquier dragón para ver su historia o escucharlo hablar.",
                        "¡Presiona el botón de RA para traerlo al mundo real!",
                        "¡Sigue participando para coleccionar a los 7 guardianes!"
                };
            case CANJE:
                return new String[]{
                        "¡Bienvenido a la Tienda de ParquePuntos!",
                        "¡Usa los puntos que has ganado para vestir a Drako con nuevos accesorios!",
                        "¡Cada prenda o accesorio se equipará automáticamente en Drako!"
                };
            default:
                return new String[]{"¡Hola! Soy Drako, tu guía en ParqueVivo."};
        }
    }

    public void darConsejoOSaludo() {
        if (tvGlobo == null) return;

        String[] mensajes = obtenerMensajes();
        int indexActual = indicesMensajes.get(tipoPantalla);

        String textoAMostrar = mensajes[indexActual];

        tvGlobo.setText(textoAMostrar);
        tvGlobo.setAlpha(0.85f);
        tvGlobo.setVisibility(View.VISIBLE);

        hablar(textoAMostrar);

        int siguienteIndex = (indexActual + 1) % mensajes.length;
        indicesMensajes.put(tipoPantalla, siguienteIndex);
    }

    public void mostrarTextoEspecial(String texto) {
        if (tvGlobo != null) {
            tvGlobo.setText(texto);
            tvGlobo.setAlpha(0.85f);
            tvGlobo.setVisibility(View.VISIBLE);
            hablar(texto);
        }
    }
}