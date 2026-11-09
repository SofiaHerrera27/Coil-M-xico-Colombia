package com.joro.parquevivo;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.PixelCopy;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.sceneview.ar.ArSceneView;
import io.github.sceneview.ar.node.ArModelNode;
import io.github.sceneview.ar.node.PlacementMode;

public class ARViewerActivity extends AppCompatActivity {

    public static final String EXTRA_MODELO_GLB = "EXTRA_MODELO_GLB";
    public static final String EXTRA_NOMBRE_PERSONAJE = "EXTRA_NOMBRE_PERSONAJE";
    private static final int CAMERA_PERMISSION_CODE = 1001;

    private ArSceneView arSceneView;
    private ArModelNode modelNode;
    private String nombrePersonaje = "Guardián 3D";
    private DatabaseHelper dbHelper;
    private long usuarioId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_viewer);

        // Recuperar ID de usuario desde Intent
        usuarioId = getIntent().getLongExtra("usuario_id", -1);

        dbHelper = new DatabaseHelper(this);

        arSceneView = findViewById(R.id.arSceneView);
        ImageButton btnCerrar = findViewById(R.id.btnCerrarAR);
        FloatingActionButton btnTomarFoto = findViewById(R.id.btnTomarFoto);

        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> finish());
        }

        if (btnTomarFoto != null) {
            btnTomarFoto.setOnClickListener(v -> capturarFotoAR());
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            cargarModelo3D();
        }
    }

    private void cargarModelo3D() {
        String modelGlbPath = getIntent().getStringExtra(EXTRA_MODELO_GLB);
        String nombreExtra = getIntent().getStringExtra(EXTRA_NOMBRE_PERSONAJE);
        if (nombreExtra != null && !nombreExtra.isEmpty()) {
            nombrePersonaje = nombreExtra;
        }

        if (modelGlbPath == null || modelGlbPath.isEmpty()) {
            modelGlbPath = "models/morado.glb";
        }

        modelNode = new ArModelNode(arSceneView.getEngine());
        modelNode.setPlacementMode(PlacementMode.PLANE_HORIZONTAL);
        modelNode.setEditable(true);

        modelNode.loadModelGlbAsync(
                modelGlbPath,
                true,
                null,
                null,
                null,
                instance -> {
                    Toast.makeText(this, "Apunta a una superficie y toca la pantalla para fijar al personaje", Toast.LENGTH_LONG).show();
                    return null;
                }
        );

        arSceneView.setOnClickListener(v -> {
            if (modelNode != null && !modelNode.isAnchored()) {
                modelNode.anchor();
                Toast.makeText(this, "¡Personaje anclado! Puedes caminar a su alrededor", Toast.LENGTH_SHORT).show();
            }
        });

        arSceneView.addChild(modelNode);
    }

    /** Captura exacta de lo que ve la cámara + el modelo 3D renderizado */
    private void capturarFotoAR() {
        if (arSceneView == null || arSceneView.getWidth() <= 0 || arSceneView.getHeight() <= 0) return;

        Bitmap bitmap = Bitmap.createBitmap(arSceneView.getWidth(), arSceneView.getHeight(), Bitmap.Config.ARGB_8888);

        PixelCopy.request(arSceneView, bitmap, copyResult -> {
            if (copyResult == PixelCopy.SUCCESS) {
                File archivoFoto = guardarBitmapEnArchivo(bitmap);
                if (archivoFoto != null && archivoFoto.exists()) {

                    // 1. Guardar copia en la galería externa pública
                    copiarFotoAGaleria(archivoFoto);

                    // 2. Registrar en la base de datos de SQLite con la tabla del Álbum
                    String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    if (usuarioId != -1) {
                        dbHelper.registrarFoto(
                                usuarioId,
                                archivoFoto.getName(),
                                archivoFoto.getAbsolutePath(),
                                fecha
                        );
                    }

                    MusicaManager.reproducirEfecto(R.raw.sfx_moneda);
                    Toast.makeText(this, "📸 ¡Foto guardada en tu Álbum de Aventuras!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error al guardar la fotografía", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se pudo capturar la vista de RA", Toast.LENGTH_SHORT).show();
            }
        }, new Handler(Looper.getMainLooper()));
    }

    private File carpetaFotos() {
        File base = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (base == null) {
            base = getFilesDir();
        }
        File carpeta = new File(base, "ParqueVivo");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        return carpeta;
    }

    private File guardarBitmapEnArchivo(Bitmap bitmap) {
        String nombre = "PV_AR_" + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(new Date()) + ".jpg";
        File archivo = new File(carpetaFotos(), nombre);
        try (FileOutputStream out = new FileOutputStream(archivo)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            return archivo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void copiarFotoAGaleria(File archivo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, archivo.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ParqueVivo");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (FileInputStream entrada = new FileInputStream(archivo);
                 OutputStream salida = getContentResolver().openOutputStream(uri)) {
                if (salida != null) {
                    byte[] buffer = new byte[8192];
                    int cantidad;
                    while ((cantidad = entrada.read(buffer)) != -1) {
                        salida.write(buffer, 0, cantidad);
                    }
                    salida.flush();
                }
                ContentValues finalizado = new ContentValues();
                finalizado.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(uri, finalizado, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cargarModelo3D();
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para la Realidad Aumentada", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicaManager.pausar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arSceneView = null;
    }
}