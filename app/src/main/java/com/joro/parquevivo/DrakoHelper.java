package com.joro.parquevivo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

// TODO(VOZ): descomentar estos imports cuando se implemente la voz
// import android.animation.ObjectAnimator;
// import android.animation.ValueAnimator;
// import android.speech.tts.TextToSpeech;
// import android.speech.tts.UtteranceProgressListener;
// import java.util.Locale;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import android.widget.TextView;

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

    // Los 3 frames del parpadeo (se cargan una sola vez en el constructor)
    private final Drawable abierto;
    private final Drawable medio;
    private final Drawable cerrado;

    // TODO(VOZ): campos para la voz de Drako
    // private TextToSpeech tts;
    // private boolean ttsListo = false;
    // private boolean vozActivada = true;      // luego se guarda en SharedPreferences
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
        this.imgDrako.setOnClickListener(v -> darConsejoOSaludo());


    // TODO(VOZ): iniciar el motor de voz (descomentar todo este bloque)
        // tts = new TextToSpeech(context.getApplicationContext(), status -> {
        //     if (status == TextToSpeech.SUCCESS) {
        //         int r = tts.setLanguage(new Locale("es", "CO"));
        //         if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
        //             r = tts.setLanguage(new Locale("es", "ES"));
        //         }
        //         // Si no hay voz en español instalada, Drako queda mudo (sin errores)
        //         ttsListo = (r != TextToSpeech.LANG_MISSING_DATA
        //                 && r != TextToSpeech.LANG_NOT_SUPPORTED);
        //         tts.setPitch(1.4f);        // voz más aguda de dragoncito
        //         tts.setSpeechRate(0.95f);  // un poco más lento
        //         tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
        //             @Override public void onStart(String id) {
        //                 imgDrako.post(() -> empezarAnimacionHabla());
        //             }
        //             @Override public void onDone(String id) {
        //                 imgDrako.post(() -> detenerAnimacionHabla());
        //             }
        //             @Override public void onError(String id) {
        //                 imgDrako.post(() -> detenerAnimacionHabla());
        //             }
        //         });
        //     }
        // });
    }


    // PARPADEO

    // Secuencia: medio -> cerrado -> medio -> abierto (unos 170 ms en total)
    private void parpadear(int paso) {
        Drawable[] frames = {medio, cerrado, medio, abierto};
        long[] pausas = {50, 70, 50};

        imgDrako.setImageDrawable(frames[paso]);
        if (paso < frames.length - 1) {
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
        handler.removeCallbacksAndMessages(null); // cancela cualquier parpadeo pendiente
        handler.postDelayed(() -> parpadear(0), 1500);
    }

    public void detener() {
        handler.removeCallbacksAndMessages(null);
        imgDrako.setImageDrawable(abierto); // que no quede con los ojos cerrados

        // TODO(VOZ): cortar la voz y la animación al salir de la pantalla
        // if (tts != null) tts.stop();
        // detenerAnimacionHabla();
    }

    // TODO(VOZ): liberar el motor de voz; llamarlo desde onDestroy() de la Activity
    // public void liberar() {
    //     if (tts != null) {
    //         tts.stop();
    //         tts.shutdown();
    //         tts = null;
    //     }
    // }


    // VOZ (pendiente de implementar)

    // TODO(VOZ): leer el mensaje en voz alta
    // private void hablar(String texto) {
    //     if (!vozActivada || !ttsListo || tts == null) return;
    //
    //     // Quita los emojis: muchos motores de voz los leen como palabras
    //     String limpio = texto.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").trim();
    //
    //     // QUEUE_FLUSH corta lo anterior si el niño toca varias veces seguidas
    //     tts.speak(limpio, TextToSpeech.QUEUE_FLUSH, null, "drako");
    // }

    // TODO(VOZ): Drako rebota suavemente mientras habla (no necesita imágenes extra)
    // private void empezarAnimacionHabla() {
    //     detenerAnimacionHabla();
    //     imgDrako.setPivotY(imgDrako.getHeight()); // rebota desde los pies
    //     animacionHabla = ObjectAnimator.ofFloat(imgDrako, "scaleY", 1f, 1.04f);
    //     animacionHabla.setDuration(250);
    //     animacionHabla.setRepeatMode(ValueAnimator.REVERSE);
    //     animacionHabla.setRepeatCount(ValueAnimator.INFINITE);
    //     animacionHabla.start();
    // }
    //
    // private void detenerAnimacionHabla() {
    //     if (animacionHabla != null) {
    //         animacionHabla.cancel();
    //         animacionHabla = null;
    //     }
    //     imgDrako.setScaleY(1f);
    // }

    // TODO(VOZ): también falta
    //  - AndroidManifest.xml, dentro de <manifest> y antes de <application>:
    //      <queries>
    //          <intent>
    //              <action android:name="android.intent.action.TTS_SERVICE" />
    //          </intent>
    //      </queries>
    //  - LoginActivity: llamar drakoHelper.liberar() en onDestroy()
    //  - Un botón o switch para silenciar la voz (vozActivada) guardado en SharedPreferences
    //  - Probar en un celular real: BlueStacks puede no traer motor de voz


    // MENSAJES

    // Múltiples mensajes generales por pantalla (sin distinción de edad)
    // Múltiples mensajes con emojis codificados en Unicode nativo de Java
    private String[] obtenerMensajes() {
        switch (tipoPantalla) {
            case LOGIN:
                return new String[]{
                        "¡Hola! Ingresa tu cuenta para guardar tus ParquePuntos. \uD83D\uDC32",
                        "Al iniciar sesión tus avances y criaturas guardadas estarán seguros. \uD83C\uDF33",
                        "¡Escribe tus datos y exploremos el parque juntos! \u2728",
                        "¿Primera vez aquí? Recuerda registrarte para unirte a la aventura. \uD83D\uDE80"
                };
            case REGISTRO:
                return new String[]{
                        "¡Qué bueno que te unes! Escribe tu nombre para empezar. \uD83D\uDC32",
                        "Usa un correo que recuerdes, ahí guardaremos tu aventura. \u2728",
                        "Tu contraseña debe tener al menos 6 caracteres. ¡Que sea secreta! \uD83D\uDD12",
                        "Repite la contraseña para confirmarla y listo. \uD83C\uDF33"
                };

            case INICIO:
                return new String[]{
                        "¡Bienvenido a ParqueVivo! Elige una actividad para empezar. \uD83C\uDF32",
                        "¡Prueba la 'Caza de Colores' para encontrar criaturas mágicas! \uD83D\uDD0D",
                        "Responde la 'Trivia' para demostrar cuánto sabes sobre el parque. \uD83E\uDDE0",
                        "¡Revisa tus ParquePuntos arriba para ver tu nivel actual! \u2B50",
                        "¡Caminar y explorar el parque suma experiencia para tus logros! \uD83C\uDF43"
                };

            case CAZA_COLORES:
                return new String[]{
                        "¡Lee con atención el acertijo para descubrir el color oculto! \uD83C\uDFA8",
                        "Apunta la cámara a elementos del parque (flores, hojas, objetos). \uD83D\uDCF8",
                        "Mantén la cámara estable sobre el color durante 2 segundos. \uD83D\uDC32",
                        "¡Si no aciertas a la tercera, te daré una pista directa! \uD83D\uDCA1",
                        "¡Cada color desbloquea una criatura única en tu Álbum! \uD83E\uDD84"
                };

            case ALBUM:
                return new String[]{
                        "¡Aquí están todas las criaturas que has atrapado en el parque! \uD83D\uDCD6",
                        "Las casillas bloqueadas indican criaturas que aún debes encontrar. \uD83D\uDC3E",
                        "¡Vuelve a la Caza de Colores para completar toda tu colección! \uD83C\uDFC6",
                        "Cada criatura representa un elemento especial de la naturaleza. \uD83C\uDF3F"
                };

            case TRIVIA:
                return new String[]{
                        "¡Demuestra que eres un gran Guardián del Parque! \uD83C\uDF3F",
                        "Cuidar las plantas y no tirar basura te da puntos extras. \uD83C\uDF33",
                        "Lee bien cada pregunta antes de seleccionar la respuesta. \uD83D\uDCDC",
                        "¡Aprender sobre naturaleza ayuda a proteger nuestro planeta! \uD83C\uDF0D"
                };

            case PREMIOS:
                return new String[]{
                        "¡Mira todas las recompensas que puedes canjear con tus puntos! \uD83D\uDC81",
                        "Sigue jugando en las distintas actividades para acumular más puntos. \uD83C\uDFC5",
                        "¡Muéstrale a tus amigos y familia todos tus logros en el parque! \uD83C\uDF33",
                        "¡Alcanzar niveles altos desbloquea medallas especiales! \uD83D\uDC8E"
                };

            default:
                return new String[]{"¡Hola! Soy Drako, tu guía en ParqueVivo. \uD83D\uDC32"};
        }
    }


    private void darConsejoOSaludo() {
        String[] mensajes = obtenerMensajes();
        int indexActual = indicesMensajes.get(tipoPantalla);

        // Mostrar el consejo dentro del layout flotante
        tvGlobo.setText(mensajes[indexActual]);
        tvGlobo.setVisibility(View.VISIBLE);

        // Preparar el siguiente mensaje para el próximo toque
        int siguienteIndex = (indexActual + 1) % mensajes.length;
        indicesMensajes.put(tipoPantalla, siguienteIndex);
    }

}