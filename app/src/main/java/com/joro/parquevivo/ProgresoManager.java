package com.joro.parquevivo;

import android.content.Context;
import android.content.SharedPreferences;

public class ProgresoManager {

    private static final String PREF_NAME = "ParqueVivoProgreso";
    private static final String KEY_PUNTOS = "parque_puntos_totales";
    private static final String PREFIX_DESBLOQUEADO = "desbloqueado_";

    private final SharedPreferences prefs;

    public ProgresoManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Suma una cantidad específica de ParquePuntos al total acumulado.
     */
    public void sumarPuntos(int cantidad) {
        if (cantidad <= 0) return;
        int puntosActuales = getPuntos();
        prefs.edit().putInt(KEY_PUNTOS, puntosActuales + cantidad).apply();
    }

    /**
     * Guarda el personaje como capturado y le otorga +50 ParquePuntos al usuario.
     */
    public void marcarCapturado(String personajeId) {
        if (personajeId == null || personajeId.isEmpty()) return;

        // 1. Desbloquea el cromo del personaje (ej: "desbloqueado_naranja" = true)
        prefs.edit().putBoolean(PREFIX_DESBLOQUEADO + personajeId.toLowerCase().trim(), true).apply();

        // 2. Suma los 50 puntos
        sumarPuntos(50);
    }

    /**
     * Retorna el total de ParquePuntos acumulados.
     */
    public int getPuntos() {
        return prefs.getInt(KEY_PUNTOS, 0);
    }

    /**
     * Verifica si un personaje ya está desbloqueado.
     */
    public boolean estaCapturado(String personajeId) {
        if (personajeId == null) return false;
        return prefs.getBoolean(PREFIX_DESBLOQUEADO + personajeId.toLowerCase().trim(), false);
    }

    /**
     * Permite descontar puntos cuando el usuario canjee un premio.
     */
    public boolean descontarPuntos(int costo) {
        int actuales = getPuntos();
        if (actuales >= costo) {
            prefs.edit().putInt(KEY_PUNTOS, actuales - costo).apply();
            return true;
        }
        return false;
    }
}