package com.joro.parquevivo;
//integro acertijos para evitar sobrecargar la vista de la caza de colores

public class AcertijoColorModel {
    private int id;//manejo de datos identificador en la DB
    private String textoAcertijo;//Acertijo o pistas
    private String colorNombre;
    private float[] hsvLower; //limite superior
    //para activar validacion en tiempo real y q nos de una margen de busqueda en ese caso la pantalla o lo q se ve
    private float[] hsvUpper;//limite inferior
    private String mensajeExito;
    private int imagenPersonajeResId; // <--- Guardará la imagen específica de cada color


    //Metodo Contructor
    public AcertijoColorModel(int id,String textoAcertijo, String colorNombre, float[] hsvLower, float[] hsvUpper, String mensajeExito, int imagenPersonajeResId) {
        this.id = id;
        this.textoAcertijo = textoAcertijo;
        this.colorNombre = colorNombre;
        this.hsvLower = hsvLower;
        this.hsvUpper = hsvUpper;
        this.mensajeExito = mensajeExito;
        this.imagenPersonajeResId=imagenPersonajeResId;
    }

    //Metodo Getters
    //solo para leer evitando q los modifiquen directamente alguien indeceado
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
        return hsvLower;
    }

    public float[] getHsvUpper() {
        return hsvUpper;
    }

    public String getMensajeExito() {
        return mensajeExito;
    }
    public int getImagenPersonajeResId() { return imagenPersonajeResId; }
}
