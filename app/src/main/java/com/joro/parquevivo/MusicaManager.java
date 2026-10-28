package com.joro.parquevivo;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicaManager {

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
    private static Pista pistaActual = Pista.AMBIENTE;
    private static int bajadas = 0; // Contador de atenuación de volumen

    public static void inicializar(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void iniciar() {
        debeSonar = true;
        actualizar();
    }

    public static void pausar() {
        debeSonar = false;
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    public static void detener() {
        debeSonar = false;
        bajadas = 0;
        liberar();
    }

    public static boolean musicaActiva() {
        return debeSonar;
    }

    public static void cambiarPista(Pista nueva) {
        if (nueva == pistaActual && player != null && player.isPlaying()) return;
        pistaActual = nueva;
        liberar();
        actualizar();
    }

    public static void reproducirEfecto(int rawId) {
        if (appContext == null || !musicaActiva()) return;
        try {
            MediaPlayer fx = MediaPlayer.create(appContext, rawId);
            if (fx == null) return;
            bajarVolumen();
            fx.setOnCompletionListener(mp -> {
                mp.release();
                subirVolumen();
            });
            fx.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bajarVolumen() {
        bajadas++;
        aplicarVolumen();
    }

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
        if (appContext == null) return;
        if (debeSonar) {
            if (player == null) {
                try {
                    player = MediaPlayer.create(appContext, pistaActual.rawId);
                    if (player != null) {
                        player.setLooping(true);
                        aplicarVolumen();
                        player.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (!player.isPlaying()) {
                player.start();
            }
        }
    }

    private static void liberar() {
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
                player.release();
            } catch (Exception ignored) {}
            player = null;
        }
    }
}