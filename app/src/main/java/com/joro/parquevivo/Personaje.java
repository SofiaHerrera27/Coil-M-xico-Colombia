package com.joro.parquevivo;

/**
 * Enum que vincula cada color de los acertijos con su respectivo personaje Drako
 * y su recurso gráfico en res/drawable.
 */
public enum Personaje {
    ROJO("Drako Rojo", R.drawable.drako_rojo),
    VERDE("Drako Verde", R.drawable.drako_verde),
    AZUL("Drako Azul", R.drawable.drako_azul),
    AMARILLO("Drako Amarillo", R.drawable.drako_amarrillo),
    NARANJA("Drako Naranja", R.drawable.drako_naranja),
    MORADO("Drako Morado", R.drawable.drako_morado),
    ROSA("Drako Rosa", R.drawable.drako_rosa);

    public final String nombre;
    public final int imagenResId;

    Personaje(String nombre, int imagenResId) {
        this.nombre = nombre;
        this.imagenResId = imagenResId;
    }

    public String getNombre() {
        return nombre;
    }

    public int getImagenResId() {
        return imagenResId;
    }

    /**
     * Busca y retorna la instancia del Personaje correspondiente al nombre del color.
     */
    public static Personaje desdeColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return null;
        }

        String colorLimpio = color.trim();
        for (Personaje p : values()) {
            if (p.name().equalsIgnoreCase(colorLimpio)) {
                return p;
            }
        }
        return null;
    }
}