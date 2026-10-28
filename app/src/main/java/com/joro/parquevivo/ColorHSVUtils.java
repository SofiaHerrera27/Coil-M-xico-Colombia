package com.joro.parquevivo;

/**
 * Clase utilitaria para obtener los rangos umbral en espacio de color HSV
 * (Hue: 0-360°, Saturation: 0-1, Value/Brightness: 0-1) para cada acertijo.
 */
public class ColorHSVUtils {

    private ColorHSVUtils() {
        // Constructor privado para evitar instanciación
    }

    // Margen inferior de validación (Hue, Sat, Val)
    public static float[] getLowerHSV(String color) {
        if (color == null || color.trim().isEmpty()) {
            return new float[]{0f, 0f, 0f};
        }

        switch (color.trim().toUpperCase()) {
            case "ROJO":
                return new float[]{340f, 0.25f, 0.20f};
            case "VERDE":
                return new float[]{65f, 0.20f, 0.20f};
            case "AZUL":
                return new float[]{170f, 0.20f, 0.20f};
            case "AMARILLO":
                return new float[]{35f, 0.25f, 0.25f};
            case "NARANJA":
                return new float[]{10f, 0.25f, 0.25f};
            case "MORADO":
                return new float[]{240f, 0.15f, 0.15f};
            case "ROSA":
                return new float[]{290f, 0.15f, 0.25f};
            default:
                return new float[]{0f, 0f, 0f};
        }
    }

    // Margen superior de validación (Hue, Sat, Val)
    public static float[] getUpperHSV(String color) {
        if (color == null || color.trim().isEmpty()) {
            return new float[]{360f, 1.0f, 1.0f};
        }

        switch (color.trim().toUpperCase()) {
            case "ROJO":
                return new float[]{25f, 1.0f, 1.0f};
            case "VERDE":
                return new float[]{160f, 1.0f, 1.0f};
            case "AZUL":
                return new float[]{255f, 1.0f, 1.0f};
            case "AMARILLO":
                return new float[]{68f, 1.0f, 1.0f};
            case "NARANJA":
                return new float[]{34f, 1.0f, 1.0f};
            case "MORADO":
                return new float[]{289f, 1.0f, 1.0f};
            case "ROSA":
                return new float[]{350f, 1.0f, 1.0f};
            default:
                return new float[]{360f, 1.0f, 1.0f};
        }
    }
}