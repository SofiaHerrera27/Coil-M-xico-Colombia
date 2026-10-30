package com.joro.parquevivo;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class MusicaManager {

    private static final String TAG = "MUSICA";
    private static final float VOL_NORMAL = 0.8f;
    private static final float VOL_BAJO = 0.2f;

    public enum Pista {
        AMBIENTE(R.raw.musica_ambiente),
        ACERTIJO(R.raw.musica_acertijo);

        final int rawId;
        Pista(int rawId) { this.rawId = rawId; }
    }

    private static Context appContext;
    private static MediaPlayer player;
    private static boolean debeSonar = false;
    private static boolean musicaHabilitada = true; // Control global del usuario
    private static Pista pistaActual = Pista.AMBIENTE;
    private static int bajadas = 0;

    public static void init(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
    }

    // --- NUEVO: Control de interruptor de música ---
    public static boolean isMusicaHabilitada() {
        return musicaHabilitada;
    }

    public static boolean toggleMusica(Context context) {
        init(context);
        musicaHabilitada = !musicaHabilitada;

        if (musicaHabilitada) {
            debeSonar = true;
            actualizar();
        } else {
            pausar();
        }
        return musicaHabilitada;
    }

    public static void reanudar(Context context, Pista pista) {
        init(context);
        if (!musicaHabilitada) return; // Si el usuario la apagó, se respeta la decisión

        if (pista != pistaActual || player == null) {
            pistaActual = pista;
            liberar();
        }
        debeSonar = true;
        actualizar();
    }

    public static void cambiarPista(Context context, Pista pista) {
        reanudar(context, pista);
    }

    public static void cambiarPista(Pista pista) {
        if (appContext != null) {
            reanudar(appContext, pista);
        }
    }

    public static void pausar() {
        debeSonar = false;
        try {
            if (player != null && player.isPlaying()) player.pause();
        } catch (Exception ignored) { }
    }

    public static void detener() {
        debeSonar = false;
        bajadas = 0;
        liberar();
    }

    public static boolean musicaActiva() { return debeSonar && musicaHabilitada; }

    public static void reproducirEfecto(Context context, int rawId) {
        init(context);
        reproducirEfecto(rawId);
    }

    public static void reproducirEfecto(int rawId) {
        if (appContext == null) {
            Log.e(TAG, "Efecto ignorado: appContext es null. Llama a MusicaManager.init(context) primero.");
            return;
        }
        try {
            MediaPlayer fx = MediaPlayer.create(appContext, rawId);
            if (fx == null) {
                Log.e(TAG, "ERROR: no se pudo crear el efecto con ID: " + rawId);
                return;
            }
            bajarVolumen();
            fx.setOnCompletionListener(mp -> {
                mp.release();
                subirVolumen();
            });
            fx.start();
        } catch (Exception e) {
            Log.e(TAG, "error en efecto", e);
        }
    }

    public static void bajarVolumen() { bajadas++; aplicarVolumen(); }

    public static void subirVolumen() {
        if (bajadas > 0) bajadas--;
        aplicarVolumen();
    }

    private static void aplicarVolumen() {
        if (player != null) {
            float v = (bajadas > 0) ? VOL_BAJO : VOL_NORMAL;
            player.setVolume(v, v);
        }
    }

    private static void actualizar() {
        if (appContext == null || !debeSonar || !musicaHabilitada) return;
        try {
            if (player == null) {
                player = MediaPlayer.create(appContext, pistaActual.rawId);
                if (player == null) { Log.e(TAG, "no se pudo crear " + pistaActual); return; }
                player.setLooping(true);
                aplicarVolumen();
            }
            if (!player.isPlaying()) player.start();
            Log.d(TAG, "sonando " + pistaActual);
        } catch (Exception e) {
            Log.e(TAG, "error al reproducir", e);
        }
    }

    private static void liberar() {
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
                player.release();
            } catch (Exception ignored) { }
            player = null;
        }
    }
}