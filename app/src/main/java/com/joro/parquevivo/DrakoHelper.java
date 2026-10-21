package com.joro.parquevivo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

// descomentar estos imports cuando activemos la voz de Drako
// import android.animation.ObjectAnimator;
// import android.animation.ValueAnimator;
// import android.speech.tts.TextToSpeech;
// import android.speech.tts.UtteranceProgressListener;
// import java.util.Locale;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DrakoHelper {

    // Pantallas principales de ParqueVivo
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

    // Los 3 frames del parpadeo
    private final Drawable abierto;
    private final Drawable medio;
    private final Drawable cerrado;

    // campos para la voz de Drako
    // private TextToSpeech tts;
    // private boolean ttsListo = false;
    // private boolean vozActivada = true;      // Luego se guarda en SharedPreferences
    // private ObjectAnimator animacionHabla;

    // Guarda qué mensaje le toca mostrar a Drako en cada pantalla
    private static final Map<TipoPantalla, Integer> indicesMensajes = new HashMap<>();

    public DrakoHelper(Context context,
                       ImageView imgDrako,
                       TextView tvGlobo,
                       TipoPantalla tipoPantalla) {

        this.context = context;
        this.imgDrako = imgDrako;
        this.tvGlobo = tvGlobo;
        this.tipoPantalla = tipoPantalla;

        // Imágenes necesarias para el parpadeo
        abierto = ContextCompat.getDrawable(context, R.drawable.drako);
        medio = ContextCompat.getDrawable(context, R.drawable.drako_medio);
        cerrado = ContextCompat.getDrawable(context, R.drawable.drako_cerrado);

        if (!indicesMensajes.containsKey(tipoPantalla)) {
            indicesMensajes.put(tipoPantalla, 0);
        }

        // Al tocar a Drako, mostrar el consejo en su propio globo
        if (this.imgDrako != null) {
            this.imgDrako.setOnClickListener(v -> darConsejoOSaludo());
        }

        //iniciar motor de voz
        /*
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(new Locale("es", "CO"));
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    r = tts.setLanguage(new Locale("es", "ES"));
                }
                // Si no hay voz en español instalada, Drako queda mudo (sin errores)
                ttsListo = (r != TextToSpeech.LANG_MISSING_DATA
                        && r != TextToSpeech.LANG_NOT_SUPPORTED);
                tts.setPitch(1.4f);        // Voz más aguda de dragoncito
                tts.setSpeechRate(0.95f);  // Un poco más lento
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) {
                        if (imgDrako != null) imgDrako.post(() -> empezarAnimacionHabla());
                    }
                    @Override public void onDone(String id) {
                        if (imgDrako != null) imgDrako.post(() -> detenerAnimacionHabla());
                    }
                    @Override public void onError(String id) {
                        if (imgDrako != null) imgDrako.post(() -> detenerAnimacionHabla());
                    }
                });
            }
        });
        */
    }


    // PARPADEO

    // Secuencia: medio -> cerrado -> medio -> abierto (unos 170 ms en total)
    private void parpadear(int paso) {
        Drawable[] frames = {medio, cerrado, medio, abierto};
        long[] pausas = {50, 70, 50};

        if (imgDrako == null) return;

        imgDrako.setImageDrawable(frames[paso]);

        if (paso < frames.length - 1) { // Corregido error sintáctico (- 1)
            handler.postDelayed(() -> parpadear(paso + 1), pausas[paso]);
        } else {
            programarSiguiente();
        }
    }

    // Espera entre 2.5 y 6 segundos antes del siguiente parpadeo
    private void programarSiguiente() {
        handler.postDelayed(() -> parpadear(0), random.nextInt(3500) + 2500);
    }

    public void iniciar() {
        handler.removeCallbacksAndMessages(null); // Cancela cualquier parpadeo pendiente
        darConsejoOSaludo(); // Muestra la burbuja inicial al arrancar
        handler.postDelayed(() -> parpadear(0), 1500);
    }

    public void detener() {
        handler.removeCallbacksAndMessages(null);
        if (imgDrako != null && abierto != null) {
            imgDrako.setImageDrawable(abierto); // Que no quede con los ojos cerrados al salir
        }

        //cortar la voz y la animación al salir de la pantalla
        // if (tts != null) tts.stop();
        // detenerAnimacionHabla();
    }

    //liberar el motor de voz; llamarlo desde onDestroy() en la Activity
    /*
    public void liberar() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
    */


    // leer el mensaje en voz alta
    /*
    private void hablar(String texto) {
        if (!vozActivada || !ttsListo || tts == null) return;

        // Quita los emojis: muchos motores de voz los leen como palabras
        String limpio = texto.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").trim();

        // QUEUE_FLUSH corta lo anterior si el niño toca varias veces seguidas
        tts.speak(limpio, TextToSpeech.QUEUE_FLUSH, null, "drako");
    }
    */

    //Rebote: Drako rebota suavemente mientras habla (no necesita imágenes extra)
    /*
    private void empezarAnimacionHabla() {
        detenerAnimacionHabla();
        if (imgDrako == null) return;
        imgDrako.setPivotY(imgDrako.getHeight()); // Rebota desde los pies
        animacionHabla = ObjectAnimator.ofFloat(imgDrako, "scaleY", 1f, 1.04f);
        animacionHabla.setDuration(250);
        animacionHabla.setRepeatMode(ValueAnimator.REVERSE);
        animacionHabla.setRepeatCount(ValueAnimator.INFINITE);
        animacionHabla.start();
    }

    private void detenerAnimacionHabla() {
        if (animacionHabla != null) {
            animacionHabla.cancel();
            animacionHabla = null;
        }
        if (imgDrako != null) imgDrako.setScaleY(1f);
    }
    */
//por ultimo integrarlo en el manifests

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

        // Mostrar el consejo dentro del globo flotante
        tvGlobo.setText(textoAMostrar);
        tvGlobo.setVisibility(View.VISIBLE);

        // activa esta llamada para hablar automáticamente:
        // hablar(textoAMostrar);

        // Preparar el siguiente mensaje para el próximo toque
        int siguienteIndex = (indexActual + 1) % mensajes.length;
        indicesMensajes.put(tipoPantalla, siguienteIndex);
    }
    public void mostrarTextoEspecial(String texto) {
        if (tvGlobo != null) {
            tvGlobo.setText(texto);
            tvGlobo.setVisibility(View.VISIBLE);
        }
    }
}