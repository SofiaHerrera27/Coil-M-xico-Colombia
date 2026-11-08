package com.joro.parquevivo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DrakoHelper {

    public enum TipoPantalla {
        LOGIN, REGISTRO, INICIO, CAZA_COLORES, ALBUM, TRIVIA, PREMIOS
    }

    private final Context context;
    private final ImageView imgDrako;
    private final TextView tvGlobo;
    private final TipoPantalla tipoPantalla;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private Drawable abierto;
    private Drawable medio;
    private Drawable cerrado;

    private boolean puedeParpadear = false;

    private static final String PREFS_EQUIPAMIENTO = "parque_prefs";
    private static final String KEY_ITEM_EQUIPADO = "item_equipado";

    private static final Map<TipoPantalla, Integer> indicesMensajes = new HashMap<>();

    public DrakoHelper(Context context, ImageView imgDrako, TextView tvGlobo, TipoPantalla tipoPantalla) {
        this.context = context;
        this.imgDrako = imgDrako;
        this.tvGlobo = tvGlobo;
        this.tipoPantalla = tipoPantalla;

        if (!indicesMensajes.containsKey(tipoPantalla)) {
            indicesMensajes.put(tipoPantalla, 0);
        }

        if (this.imgDrako != null) {
            this.imgDrako.setBackgroundColor(Color.TRANSPARENT);
        }

        actualizarTraje();

        if (this.imgDrako != null) {
            this.imgDrako.setOnClickListener(v -> darConsejoOSaludo());
        }
    }

    public void actualizarTraje() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_EQUIPAMIENTO, Context.MODE_PRIVATE);
        String itemEquipado = prefs.getString(KEY_ITEM_EQUIPADO, "default");

        int idAbierto = R.drawable.drako;
        String nombreDrawableBase = "drako";

        // Mapeo de los trajes (Nombre base para buscar los _medio y _cerrados)
        // NOTA: Se eliminó cualquier referencia a drago_guerrero
        switch (itemEquipado) {
            case "sombrero_explorador": idAbierto = R.drawable.dragon_sombrero; nombreDrawableBase = "dragon_sombrero"; break;
            case "gorra_guardabosques": idAbierto = R.drawable.drago_guardab; nombreDrawableBase = "drago_guardab"; break;
            case "mochila_exploradora": idAbierto = R.drawable.drago_mochila; nombreDrawableBase = "drago_mochila"; break;
            case "brujula_dorada": idAbierto = R.drawable.drago_brujula; nombreDrawableBase = "drago_brujula"; break;
            case "mariposa_magica": idAbierto = R.drawable.drago_mariposa; nombreDrawableBase = "drago_mariposa"; break;
            case "sombrero_mago": idAbierto = R.drawable.drago_smago; nombreDrawableBase = "drago_smago"; break;
            case "cristal_energia": idAbierto = R.drawable.drago_cristal; nombreDrawableBase = "drago_cristal"; break;
            case "zorrito_bosque": idAbierto = R.drawable.drago_zorro; nombreDrawableBase = "drago_zorro"; break;
            case "buho_sabio": idAbierto = R.drawable.drago_buho; nombreDrawableBase = "drago_buho"; break;
            case "medalla_ecologica": idAbierto = R.drawable.drago_medalla; nombreDrawableBase = "drago_medalla"; break;
            case "corona_dorada": idAbierto = R.drawable.drago_corona; nombreDrawableBase = "drago_corona"; break;
            case "alas_magicas": idAbierto = R.drawable.drago_alas_mariposa; nombreDrawableBase = "drago_alas_mariposa"; break;
            case "dragon_bebe": idAbierto = R.drawable.drago_bebe; nombreDrawableBase = "drago_bebe"; break;
            case "tunica_magica": idAbierto = R.drawable.drago_tunica; nombreDrawableBase = "drago_tunica"; break;
            case "guardian_bosque": idAbierto = R.drawable.drago_guardian; nombreDrawableBase = "drago_guardian"; break;
            case "traje_aventurero": idAbierto = R.drawable.drago_aventurero; nombreDrawableBase = "drago_aventurero"; break;
            case "varita_natural": idAbierto = R.drawable.drago_varita; nombreDrawableBase = "drago_varita"; break;
            case "cofre_misterioso": idAbierto = R.drawable.drago_cofre; nombreDrawableBase = "drago_cofre"; break;
            case "armadura_dorada": idAbierto = R.drawable.drago_armadura_caballero; nombreDrawableBase = "drago_armadura_caballero"; break;
            case "alas_celestiales": idAbierto = R.drawable.drago_alas_doradas; nombreDrawableBase = "drago_alas_doradas"; break;
        }

        if (itemEquipado.equals("default")) {
            // Configuración para el Drako base
            puedeParpadear = true;
            abierto = ContextCompat.getDrawable(context, R.drawable.drako);
            medio = ContextCompat.getDrawable(context, R.drawable.drako_medio);
            cerrado = ContextCompat.getDrawable(context, R.drawable.drako_cerrado);
        } else {
            // Configuración para trajes
            abierto = ContextCompat.getDrawable(context, idAbierto);

            // Buscamos dinámicamente las imágenes _medio y _cerrados (en plural)
            int idMedio = context.getResources().getIdentifier(nombreDrawableBase + "_medio", "drawable", context.getPackageName());
            int idCerrado = context.getResources().getIdentifier(nombreDrawableBase + "_cerrados", "drawable", context.getPackageName());

            // Si por alguna razón en el futuro la nombras en singular (_cerrado), hacemos un respaldo
            if (idCerrado == 0) {
                idCerrado = context.getResources().getIdentifier(nombreDrawableBase + "_cerrado", "drawable", context.getPackageName());
            }

            if (idMedio != 0 && idCerrado != 0) {
                // ¡Encontró las imágenes! Activamos el parpadeo para este traje
                medio = ContextCompat.getDrawable(context, idMedio);
                cerrado = ContextCompat.getDrawable(context, idCerrado);
                puedeParpadear = true;
            } else {
                // No hay imágenes de parpadeo para este traje, se queda quieto
                medio = abierto;
                cerrado = abierto;
                puedeParpadear = false;
            }
        }

        if (imgDrako != null && abierto != null) {
            imgDrako.setImageDrawable(abierto);
        }
    }

    private void parpadear(int paso) {
        if (imgDrako == null) return;

        // Si no tiene las imágenes de parpadeo, espera al siguiente ciclo sin hacer nada
        if (!puedeParpadear) {
            programarSiguiente();
            return;
        }

        Drawable[] frames = {medio, cerrado, medio, abierto};
        long[] pausas = {50, 70, 50};

        if (frames[paso] != null) {
            imgDrako.setImageDrawable(frames[paso]);
        }

        if (paso < frames.length - 1) {
            handler.postDelayed(() -> parpadear(paso + 1), pausas[paso]);
        } else {
            programarSiguiente();
        }
    }

    private void programarSiguiente() {
        // Espera entre 2.5 y 6 segundos para el próximo parpadeo
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
        handler.removeCallbacksAndMessages(null);
        if (imgDrako != null && abierto != null) {
            imgDrako.setImageDrawable(abierto); // Asegura que se quede con los ojos abiertos
        }
    }

    private String[] obtenerMensajes() {
        switch (tipoPantalla) {
            case LOGIN: return new String[]{"¡Hola! Ingresa tu cuenta para guardar tus ParquePuntos.", "Al iniciar sesión tus avances y criaturas guardadas estarán seguros.", "¡Escribe tus datos y exploremos el parque juntos!", "¿Primera vez aquí? Recuerda registrarte para unirte a la aventura."};
            case REGISTRO: return new String[]{"¡Qué bueno que te unes! Escribe tu nombre para empezar.", "Usa un correo que recuerdes, ahí guardaremos tu aventura.", "Tu contraseña debe tener al menos 6 caracteres. ¡Que sea secreta!", "Repite la contraseña para confirmarla y listo."};
            case INICIO: return new String[]{"¡Bienvenido a ParqueVivo! Elige una actividad para empezar.", "¡Prueba la 'Caza de Colores' para encontrar criaturas mágicas!", "Responde la 'Trivia' para demostrar cuánto sabes sobre el parque.", "¡Revisa tus ParquePuntos arriba para ver tu nivel actual!", "¡Caminar y explorar el parque suma experiencia para tus logros!"};
            case CAZA_COLORES: return new String[]{"¡Lee con atención el acertijo para descubrir el color oculto!", "Apunta la cámara a elementos del parque (flores, hojas, objetos).", "Mantén la cámara estable sobre el color durante 2 segundos.", "¡Si no aciertas a la tercera, te daré una pista directa!", "¡Cada color desbloquea una criatura única en tu Álbum!"};
            case ALBUM: return new String[]{"¡Aquí están todas las criaturas que has atrapado en el parque!", "Las casillas bloqueadas indican criaturas que aún debes encontrar.", "¡Vuelve a la Caza de Colores para completar toda tu colección!", "Cada criatura representa un elemento especial de la naturaleza."};
            case TRIVIA: return new String[]{"¡Demuestra que eres un gran Guardián del Parque!", "Cuidar las plantas y no tirar basura te da puntos extras.", "Lee bien cada pregunta antes de seleccionar la respuesta.", "¡Aprender sobre naturaleza ayuda a proteger nuestro planeta!"};
            case PREMIOS: return new String[]{"¡Mira todas las recompensas que puedes canjear con tus puntos!", "Sigue jugando en las distintas actividades para acumular más puntos.", "¡Muéstrale a tus amigos y familia todos tus logros en el parque!", "¡Alcanzar niveles altos desbloquea medallas especiales!"};
            default: return new String[]{"¡Hola! Soy Drako, tu guía en ParqueVivo."};
        }
    }

    public void darConsejoOSaludo() {
        if (tvGlobo == null) return;
        String[] mensajes = obtenerMensajes();
        int indexActual = indicesMensajes.get(tipoPantalla);
        String textoAMostrar = mensajes[indexActual];

        tvGlobo.setText(textoAMostrar);
        tvGlobo.setVisibility(View.VISIBLE);

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