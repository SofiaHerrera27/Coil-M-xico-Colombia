package com.joro.parquevivo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
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

    public enum TipoPantalla {
        LOGIN,
        REGISTRO,
        INICIO,
        CAZA_COLORES,
        ALBUM,
        TRIVIA,
        PREMIOS
    }

    private final Context context;
    private final ImageView imgDrako;
    private final TextView tvGlobo;
    private final TipoPantalla tipoPantalla;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private final Drawable abierto;
    private final Drawable medio;
    private final Drawable cerrado;

    private static final Map<TipoPantalla, Integer> indicesMensajes = new HashMap<>();

    public DrakoHelper(Context context,
                       ImageView imgDrako,
                       TextView tvGlobo,
                       TipoPantalla tipoPantalla) {

        this.context = context;
        this.imgDrako = imgDrako;
        this.tvGlobo = tvGlobo;
        this.tipoPantalla = tipoPantalla;

        abierto = ContextCompat.getDrawable(context, R.drawable.drako);
        medio = ContextCompat.getDrawable(context, R.drawable.drako_medio);
        cerrado = ContextCompat.getDrawable(context, R.drawable.drako_cerrado);

        if (!indicesMensajes.containsKey(tipoPantalla)) {
            indicesMensajes.put(tipoPantalla, 0);
        }

        if (this.imgDrako != null) {
            this.imgDrako.setOnClickListener(v -> darConsejoOSaludo());
        }

        // Inicializar motor de voz TTS
        tts = new TextToSpeech(context.getApplicationContext(), this);
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

                // Reproduce el texto del globo que ya estaba visible al abrir la pantalla
                if (tvGlobo != null && tvGlobo.getVisibility() == View.VISIBLE) {
                    String textoInicial = tvGlobo.getText().toString();
                    if (!textoInicial.isEmpty()) {
                        handler.post(() -> hablar(textoInicial));
                    }
                }
            }
        }
    }

    // PARPADEO
    private void parpadear(int paso) {
        Drawable[] frames = {medio, cerrado, medio, abierto};
        long[] pausas = {50, 70, 50};

        if (imgDrako == null) return;

        imgDrako.setImageDrawable(frames[paso]);

        if (paso < frames.length - 1) { // CORREGIDO: Se agregó el operador '- 1'
            handler.postDelayed(() -> parpadear(paso + 1), pausas[paso]);
        } else {
            programarSiguiente();
        }
    }

    private void programarSiguiente() {
        handler.postDelayed(() -> parpadear(0), random.nextInt(3500) + 2500);
    }

    public void iniciar() {
        handler.removeCallbacksAndMessages(null);
        darConsejoOSaludo();
        handler.postDelayed(() -> parpadear(0), 1500);
    }

    public void iniciarSinSaludo() {
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

    // Método para leer cualquier texto mediante TTS
    public void hablar(String texto) {
        detenerVoz();
        if (texto == null || texto.trim().isEmpty()) return;

        if (ttsListo && tts != null) {
            // Quita emojis y símbolos que interrumplan la lectura
            String textoLimpio = texto.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").trim();
            tts.speak(textoLimpio, TextToSpeech.QUEUE_FLUSH, null, "drako_speech");
        }
    }

    // Permite reproducir audios grabados manualmente de res/raw/
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

    // MENSAJES
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
                        "¡Mira todas las recompensas que puedes canjear con tus puntos!",
                        "Sigue jugando en las distintas actividades para acumular más puntos.",
                        "¡Muéstrale a tus amigos y familia todos tus logros en el parque!",
                        "¡Alcanzar niveles altos desbloquea medallas especiales!"
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
        tvGlobo.setVisibility(View.VISIBLE);

        hablar(textoAMostrar);

        int siguienteIndex = (indexActual + 1) % mensajes.length;
        indicesMensajes.put(tipoPantalla, siguienteIndex);
    }

    public void mostrarTextoEspecial(String texto) {
        if (tvGlobo != null) {
            tvGlobo.setText(texto);
            tvGlobo.setVisibility(View.VISIBLE);
            hablar(texto);
        }
    }
}