package com.joro.parquevivo;

import java.util.Locale;

public class ColorHSVUtils {
    //Declaracion de variables


    //Cordenadas para validacion
    //margen inferior de validacion
    public static float[] getLowerHSV(String color) {
        switch (color.toUpperCase()) {
            case "ROJO":
                return new float[]{0f, 0.5f, 0.3f};
            case "Verde":
                return new float[]{70f, 0.3f, 0.3f};
            case "Azul":
                return new float[]{180f, 0.3f, 0.3f};
            case "AMARILLO":
                return new float[]{40f, 0.5f, 0.5f};
            default:
                return new float[]{0f, 0f, 0f};
        }
    }
    //margen superior de validacion
        public static float[]getUpperHSV(String color){
            switch (color.toUpperCase()){
                case "ROJO":
                    return new float[]{20f, 1.0f, 1.0f};
                case "VERDE":
                    return new float[]{160f, 1.0f, 1.0f};
                case "AZUL":
                    return new float[]{250f, 1.0f, 1.0f};
                case "AMARILLO":
                    return new float[]{70f, 1.0f, 1.0f};
                default:
                    return new float[]{0f, 0f, 0f};
            }

        }

    }
