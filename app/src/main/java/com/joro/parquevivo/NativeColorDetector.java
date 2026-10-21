package com.joro.parquevivo;

//Clase encargada de :
//examinar los píxeles de la cámara y determinar si coinciden con los rangos del color que Drako solicitó en el acertijo.

import android.graphics.Bitmap;
import android.graphics.Color;
public class NativeColorDetector {
//Evalúa si el área central del Bitmap de la cámara contiene el color objetivo.
    public static boolean isColorInCenter(Bitmap bitmap, float[] lowerHsv, float[] upperHsv){
        // valida de seguridad
        //evita q la palicacion se cierre cuando la camra no entrega el  frame
        if (bitmap==null || lowerHsv==null || upperHsv==null)return false;
        //Variables de imagen en estas esta la dimension
        int width= bitmap.getWidth();//ancho del frame en pixeles
        int height= bitmap.getHeight();//alto

        //cordenadas para el punto central exacto de la pantalla
        int centerX= width/2;
        int centerY=height/2;

        //cuadricula al rededor del centro de 40*40
        int sampleSize=20;

        //Contador para calcular porcentaje.
        int matchingPixels=0;//cantidad depines que contiene el color buscado
        int totalPixelsTested=0;//total pixeles evaluados en la muestra

        float[] pixelHsv =new float[3];

        // Bucle que recorre la región de interés (ROI) en el centro de la pantalla.
        // Se salta de a 4 píxeles (x += 4) para optimizar el rendimiento de la CPU.
        for(int x=centerX-sampleSize;x<centerX+sampleSize;x+=4){
            for(int y=centerY-sampleSize; y<centerX+sampleSize;x+=4){

                if(x>=0 && x<width && y>=0 && y<height){
                    //traer color del pixel en formato ARGB(int)
                    int pixel=bitmap.getPixel(x,y);
                    //converir lo traido de ARGB  a HSV{Tono, Saturación, Brillo}
                    Color.colorToHSV(pixel,pixelHsv);

                    if (isHsvInRange(pixelHsv, lowerHsv, upperHsv)) {
                        matchingPixels++;
                    }
                    totalPixelsTested++;

                }

            }

        }
        // Umbral de éxito: si más del 40% de los píxeles muestreados en el centro coinciden, la detección es VÁLIDA
        return totalPixelsTested > 0 && ((float) matchingPixels / totalPixelsTested) > 0.4f;

    }
    private static boolean isHsvInRange(float[] hsv, float[] lower, float[] upper) {
        boolean hueMatch;

        // El Tono (Hue) para el color Rojo va de 350° a 10° cruzando el origen (0°/360°).
        // Esta condición especial evita fallos al detectar tonos rojos.
        if (lower[0] > upper[0]) {
            hueMatch = (hsv[0] >= lower[0] || hsv[0] <= upper[0]);
        } else {
            hueMatch = (hsv[0] >= lower[0] && hsv[0] <= upper[0]);
        }

        // Comprobación de límites para Saturación [1] y Brillo / Value [2]
        boolean satMatch = (hsv[1] >= lower[1] && hsv[1] <= upper[1]);
        boolean valMatch = (hsv[2] >= lower[2] && hsv[2] <= upper[2]);

        return hueMatch && satMatch && valMatch;
    }
}
