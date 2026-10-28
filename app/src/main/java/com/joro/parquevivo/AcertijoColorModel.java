package com.joro.parquevivo;

/**
 * Modelo de datos que representa cada acertijo de color, sus umbrales HSV
 * y la criatura que se desbloquea al resolverlo.
 */
public class AcertijoColorModel {

    private final int id;
    private final String textoAcertijo;
    private final String colorNombre;
    private final float[] hsvLower;
    private final float[] hsvUpper;
    private final String mensajeExito;
    private final int imagenPersonajeResId;

    public AcertijoColorModel(int id,
                              String textoAcertijo,
                              String colorNombre,
                              float[] hsvLower,
                              float[] hsvUpper,
                              String mensajeExito,
                              int imagenPersonajeResId) {
        this.id = id;
        this.textoAcertijo = textoAcertijo;
        this.colorNombre = colorNombre;
        this.hsvLower = (hsvLower != null) ? hsvLower.clone() : new float[]{0f, 0f, 0f};
        this.hsvUpper = (hsvUpper != null) ? hsvUpper.clone() : new float[]{360f, 1f, 1f};
        this.mensajeExito = mensajeExito;
        this.imagenPersonajeResId = imagenPersonajeResId;
    }

    public int getId() {
        return id;
    }

    public String getTextoAcertijo() {
        return textoAcertijo;
    }

    public String getColorNombre() {
        return colorNombre;
    }

    public float[] getHsvLower() {
        return hsvLower.clone();
    }

    public float[] getHsvUpper() {
        return hsvUpper.clone();
    }

    public String getMensajeExito() {
        return mensajeExito;
    }

    public int getImagenPersonajeResId() {
        return imagenPersonajeResId;
    }
}