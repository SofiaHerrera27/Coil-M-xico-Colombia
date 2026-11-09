package com.joro.parquevivo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PremiosActivity extends AppCompatActivity {

    private TextToSpeech ttsEngine;
    private boolean ttsListo = false;
    private long usuarioId = -1;
    private ProgresoManager progreso;
    private LinearLayout containerPremios;

    private static class PremioModel {
        String colorId;
        String nombre;
        String gustos;
        String historia;
        int imgRes;
        String glb;
        boolean desbloqueado;

        PremioModel(String colorId, String nombre, String gustos, String historia, int imgRes, String glb, boolean desbloqueado) {
            this.colorId = colorId;
            this.nombre = nombre;
            this.gustos = gustos;
            this.historia = historia;
            this.imgRes = imgRes;
            this.glb = glb;
            this.desbloqueado = desbloqueado;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premios);

        MusicaManager.init(this);

        usuarioId = getIntent().getLongExtra("usuario_id", -1);
        progreso = new ProgresoManager(this);
        containerPremios = findViewById(R.id.containerPremios);

        View btnVolverMenu = findViewById(R.id.btnVolverMenu);
        if (btnVolverMenu != null) {
            btnVolverMenu.setOnClickListener(v -> finish());
        }

        inicializarMotorVoz();
        actualizarListaPremios();
    }

    private void inicializarMotorVoz() {
        ttsEngine = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = ttsEngine.setLanguage(new Locale("es", "ES"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = ttsEngine.setLanguage(new Locale("es"));
                }
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = ttsEngine.setLanguage(Locale.getDefault());
                }

                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsListo = true;
                }
            }
        });
    }

    private void actualizarListaPremios() {
        if (containerPremios == null) return;
        containerPremios.removeAllViews();

        List<PremioModel> lista = new ArrayList<>();

        lista.add(new PremioModel(
                "MORADO", "Drako Morado",
                "Frutas silvestres nocturnas y acertijos.",
                "Nació en la cueva cristalina del parque. Es el líder ancestral de los guardianes.",
                R.drawable.drako_morado, "models/morado.glb", true));

        lista.add(new PremioModel(
                "ROJO", "Ignis Rojo",
                "Manzanas crujientes y sol de mediodía.",
                "Es el dragón del coraje y la energía vital. Protege los senderos densos del bosque.",
                R.drawable.drako_rojo, "models/rojo.glb", progreso.estaCapturado("ROJO")));

        lista.add(new PremioModel(
                "VERDE", "Silvano Verde",
                "Tierra mojada y brisas entre las hojas.",
                "Habita en el roble milenario. Hace florecer los caminitos por donde pisa.",
                R.drawable.drako_verde, "models/verde.glb", progreso.estaCapturado("VERDE")));

        lista.add(new PremioModel(
                "AZUL", "Glacius Azul",
                "Zambullirse en fuentes y arroyos.",
                "Guardián de la serenidad. Limpia las fuentes del parque con su aliento fresco.",
                R.drawable.drako_azul, "models/azul.glb", progreso.estaCapturado("AZUL")));

        lista.add(new PremioModel(
                "AMARILLO", "Aureo Amarillo",
                "Baños de sol y polen de girasol.",
                "Resplandece como un rayo de sol radiante al amanecer llenando de alegría el parque.",
                R.drawable.drako_amarrillo, "models/amarrillo.glb", progreso.estaCapturado("AMARILLO")));

        lista.add(new PremioModel(
                "NARANJA", "Amber Naranja",
                "Mandarinas maduras y hojas de otoño.",
                "Protector de la calidez del atardecer. Guía a las familias de regreso a casa.",
                R.drawable.drako_naranja, "models/naranja.glb", progreso.estaCapturado("NARANJA")));

        lista.add(new PremioModel(
                "ROSA", "Sakura Rosa",
                "Néctar de flores y guirnaldas.",
                "Guardián de la armonía. Transforma cualquier rincón del parque en un oasis de calma.",
                R.drawable.drako_rosa, "models/rosa.glb", progreso.estaCapturado("ROSA")));

        float density = getResources().getDisplayMetrics().density;
        LinearLayout rowActual = null;

        for (int i = 0; i < lista.size(); i++) {
            if (i % 2 == 0) {
                rowActual = new LinearLayout(this);
                rowActual.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, (int) (12 * density));
                rowActual.setLayoutParams(rowParams);
                containerPremios.addView(rowActual);
            }

            PremioModel p = lista.get(i);
            View tarjetaView = crearTarjetaBoceto(p, density);

            if (rowActual != null) {
                rowActual.addView(tarjetaView);
            }
        }

        if (lista.size() % 2 != 0 && rowActual != null) {
            View dummy = new View(this);
            LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            dummy.setLayoutParams(dummyParams);
            rowActual.addView(dummy);
        }
    }

    private View crearTarjetaBoceto(PremioModel p, float density) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cardParams.setMargins((int) (6 * density), 0, (int) (6 * density), 0);
        card.setLayoutParams(cardParams);
        card.setRadius(18 * density);
        card.setCardElevation(4 * density);
        card.setCardBackgroundColor(p.desbloqueado ? Color.parseColor("#FFFFFF") : Color.parseColor("#F0F0F0"));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        int pad = (int) (12 * density);
        box.setPadding(pad, pad, pad, pad);

        ImageView img = new ImageView(this);
        int imgSize = (int) (95 * density);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(imgSize, imgSize);
        img.setLayoutParams(imgParams);
        img.setImageResource(p.imgRes);

        img.setAlpha(p.desbloqueado ? 1.0f : 0.28f);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(p.desbloqueado ? p.nombre : "🔒 " + p.colorId);
        tvNombre.setTextSize(14f);
        tvNombre.setTypeface(null, Typeface.BOLD);
        tvNombre.setTextColor(p.desbloqueado ? Color.parseColor("#1B4D3E") : Color.parseColor("#777777"));
        tvNombre.setGravity(Gravity.CENTER);
        tvNombre.setPadding(0, (int) (6 * density), 0, 0);

        TextView tvEstado = new TextView(this);
        tvEstado.setText(p.desbloqueado ? "✨ Capturado" : "🔒 Bloqueado");
        tvEstado.setTextSize(11.5f);
        tvEstado.setTextColor(p.desbloqueado ? Color.parseColor("#2E7D32") : Color.parseColor("#999999"));
        tvEstado.setGravity(Gravity.CENTER);

        box.addView(img);
        box.addView(tvNombre);
        box.addView(tvEstado);
        card.addView(box);

        card.setOnClickListener(v -> mostrarDetallePremio(p.colorId, p.nombre, p.gustos, p.historia, p.imgRes, p.glb, p.desbloqueado));

        return card;
    }

    public void mostrarDetallePremio(String colorId, String nombre, String gustos, String historia, int imagenResId, String rutaGlb, boolean desbloqueado) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_detalle_premio);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView imgPremioGrande = dialog.findViewById(R.id.imgPremioGrande);
        TextView tvNombrePremio = dialog.findViewById(R.id.tvNombrePremio);
        TextView tvGustosPremio = dialog.findViewById(R.id.tvGustosPremio);
        TextView tvHistoriaPremio = dialog.findViewById(R.id.tvHistoriaPremio);
        Button btnVerEnRA = dialog.findViewById(R.id.btnVerEnRA);
        Button btnCerrarDialog = dialog.findViewById(R.id.btnCerrarDialog);
        ImageButton btnEscucharVoz = dialog.findViewById(R.id.btnEscucharVoz);

        if (imgPremioGrande != null) {
            imgPremioGrande.setImageResource(imagenResId);
            imgPremioGrande.setAlpha(desbloqueado ? 1.0f : 0.35f);
            if (desbloqueado) {
                aplicarAnimacion3DInteractiva(imgPremioGrande);
            }
        }

        if (tvNombrePremio != null) {
            tvNombrePremio.setText(desbloqueado ? nombre : "🔒 Boceto: " + nombre);
        }

        if (tvGustosPremio != null) {
            tvGustosPremio.setText(desbloqueado ? "🍃 Le gusta: " + gustos : "🔍 Pista de escaneo: Color " + colorId);
        }

        if (tvHistoriaPremio != null) {
            if (desbloqueado) {
                tvHistoriaPremio.setText(historia);
            } else {
                tvHistoriaPremio.setText("¡Aún no has capturado a este guardián mágico!\n\nDirígete a la 'Caza de Colores' y escanea un objeto de color " + colorId + " para atraparlo y liberar su modelo 3D en Realidad Aumentada.");
            }
        }

        String textoVoz = "¡Hola! Soy " + nombre + ". " + historia;

        if (btnEscucharVoz != null) {
            btnEscucharVoz.setVisibility(desbloqueado ? View.VISIBLE : View.GONE);
            if (desbloqueado) {
                btnEscucharVoz.setOnClickListener(v -> reproducirVozDragon(colorId, textoVoz));
                new Handler(Looper.getMainLooper()).postDelayed(() -> reproducirVozDragon(colorId, textoVoz), 300);
            }
        }

        if (btnVerEnRA != null) {
            btnVerEnRA.setVisibility(desbloqueado ? View.VISIBLE : View.GONE);
            btnVerEnRA.setOnClickListener(v -> {
                detenerVoz();
                dialog.dismiss();
                Intent intent = new Intent(this, ARViewerActivity.class);
                intent.putExtra(ARViewerActivity.EXTRA_MODELO_GLB, rutaGlb);
                intent.putExtra(ARViewerActivity.EXTRA_NOMBRE_PERSONAJE, nombre);
                intent.putExtra("usuario_id", usuarioId);
                startActivity(intent);
            });
        }

        if (btnCerrarDialog != null) {
            btnCerrarDialog.setText(desbloqueado ? "¡Genial!" : "Entendido");
            btnCerrarDialog.setOnClickListener(v -> {
                detenerVoz();
                dialog.dismiss();
            });
        }

        dialog.setOnDismissListener(d -> detenerVoz());
        dialog.show();
    }

    private void aplicarAnimacion3DInteractiva(ImageView img) {
        float density = getResources().getDisplayMetrics().density;
        img.setCameraDistance(8000f * density);

        ObjectAnimator animFlotar = ObjectAnimator.ofFloat(img, View.TRANSLATION_Y, 0f, -12f * density);
        animFlotar.setDuration(1600);
        animFlotar.setRepeatMode(ValueAnimator.REVERSE);
        animFlotar.setRepeatCount(ValueAnimator.INFINITE);
        animFlotar.start();

        ObjectAnimator animGiroBase = ObjectAnimator.ofFloat(img, View.ROTATION_Y, -15f, 15f);
        animGiroBase.setDuration(2200);
        animGiroBase.setInterpolator(new AccelerateDecelerateInterpolator());
        animGiroBase.setRepeatMode(ValueAnimator.REVERSE);
        animGiroBase.setRepeatCount(ValueAnimator.INFINITE);
        animGiroBase.start();

        img.setOnClickListener(v -> {
            MusicaManager.reproducirEfecto(this, R.raw.sfx_moneda);
            ObjectAnimator pirueta = ObjectAnimator.ofFloat(img, View.ROTATION_Y, img.getRotationY(), img.getRotationY() + 360f);
            pirueta.setDuration(850);
            pirueta.setInterpolator(new OvershootInterpolator());
            pirueta.start();
        });
    }

    private void reproducirVozDragon(String colorId, String texto) {
        if (!ttsListo || ttsEngine == null) {
            Toast.makeText(this, "Cargando motor de voz del dragón...", Toast.LENGTH_SHORT).show();
            inicializarMotorVoz();
            return;
        }

        ttsEngine.stop();
        MusicaManager.bajarVolumen();

        float pitch;
        float rate;

        switch (colorId.toUpperCase()) {
            case "ROJO": pitch = 0.5f; rate = 1.1f; break;
            case "VERDE": pitch = 1.25f; rate = 1.0f; break;
            case "AZUL": pitch = 0.75f; rate = 0.8f; break;
            case "AMARILLO": pitch = 1.65f; rate = 1.25f; break;
            case "NARANJA": pitch = 1.0f; rate = 0.95f; break;
            case "ROSA": pitch = 1.45f; rate = 0.9f; break;
            case "MORADO": default: pitch = 0.85f; rate = 0.85f; break;
        }

        ttsEngine.setPitch(pitch);
        ttsEngine.setSpeechRate(rate);
        ttsEngine.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "DragonVoiceID_" + colorId);

        new Handler(Looper.getMainLooper()).postDelayed(() -> MusicaManager.subirVolumen(), 6000);
    }

    private void detenerVoz() {
        if (ttsEngine != null && ttsEngine.isSpeaking()) {
            ttsEngine.stop();
        }
        MusicaManager.subirVolumen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicaManager.init(this);
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);
        actualizarListaPremios();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerVoz();
        MusicaManager.pausar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsEngine != null) {
            ttsEngine.stop();
            ttsEngine.shutdown();
        }
    }
}