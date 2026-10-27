package com.joro.parquevivo;
//para facilitar la coneccion y envio de imagens o paerosonages al album o al asecion de premios
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

    public static Personaje desdeColor(String color) {
        for (Personaje p : values()) {
            if (p.name().equalsIgnoreCase(color)) return p;
        }
        return null;
    }
}
