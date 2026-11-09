package com.joro.parquevivo;

import android.content.Context;
import android.content.SharedPreferences;

public class ProgresoManager {

    private static final String PREF_NAME = "ParqueVivoProgreso";
    private static final String KEY_PUNTOS = "parque_puntos_totales";
    private static final String PREFIX_DESBLOQUEADO = "desbloqueado_";
    private static final String KEY_TRAJE_EQUIPADO = "traje_equipado";

    private final SharedPreferences prefs;

    public ProgresoManager(Context context) {
        Context appCtx = context != null ? context.getApplicationContext() : context;
        this.prefs = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        if (personajeId == null || personajeId.trim().isEmpty()) return;

        if (estaCapturado(personajeId)) return;

        prefs.edit()
                .putBoolean(PREFIX_DESBLOQUEADO + personajeId.toLowerCase().trim(), true)
                .putInt(KEY_PUNTOS, getPuntos() + 50)
                .apply();
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

    /**
     * Guarda la clave del traje que Drako debe vestir en la interfaz.
     */
    public void setTrajeEquipado(String trajeId) {
        if (trajeId == null) return;
        prefs.edit().putString(KEY_TRAJE_EQUIPADO, trajeId.toUpperCase().trim()).apply();
    }

    /**
     * Retorna el traje actual equipado ("NORMAL" por defecto).
     */
    public String getTrajeEquipado() {
        return prefs.getString(KEY_TRAJE_EQUIPADO, "NORMAL");
    }
}