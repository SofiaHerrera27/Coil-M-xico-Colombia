package com.joro.parquevivo;

// Karen Fonseca

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class AlbumActivity extends AppCompatActivity {

    // Secciones (layoutInicio es RelativeLayout)
    private RelativeLayout layoutInicio;
    private LinearLayout layoutCamara, layoutGaleria, layoutDetalle;
    private DrakoHelper drakoAlbum;
    private DatabaseHelper db;
    private Executor executor;
    private long usuarioId = -1;

    // Controles de cámara
    private PreviewView previewView;
    private ImageView imagePreview;
    private ImageButton btnCapturar;
    private MaterialButton btnCambiar, btnGuardar, btnRepetir;

    // Galería
    private GridView gridFotos;
    private TextView tvVacio;
    private ImageView imageFotoGrande;
    private File fotoSeleccionada;

    private ImageCapture imageCapture;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;

    private File fotoPendiente;
    private final ArrayList<File> fotos = new ArrayList<>();
    private FotosAdapter adapter;

    // Permiso de cámara
    private final ActivityResultLauncher<String> permisoCamara =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    concedido -> {
                        if (concedido) {
                            iniciarCamara();
                        } else {
                            Toast.makeText(this,
                                    "Necesitamos permiso para usar la cámara",
                                    Toast.LENGTH_LONG).show();
                            mostrarInicio();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // 1. Inicializar administradores base
        MusicaManager.init(this);
        db = new DatabaseHelper(this);

        // 2. Obtener el ID del usuario desde el intent
        usuarioId = getIntent().getLongExtra("usuario_id", -1);

        // 3. Conectar la vista de Drako y su globo de diálogo flotante
        ImageView imgDrakoAlbum = findViewById(R.id.imgDrakoAlbum);
        TextView tvGloboAlbum = findViewById(R.id.tvGloboAlbum);

        // 4. Inicialización segura de DrakoHelper (atrapa tanto Exception como OutOfMemoryError)
        try {
            if (imgDrakoAlbum != null && tvGloboAlbum != null) {
                drakoAlbum = new DrakoHelper(
                        this,
                        imgDrakoAlbum,
                        tvGloboAlbum,
                        DrakoHelper.TipoPantalla.ALBUM
                );
            }
        } catch (Throwable t) {
            Log.e("AlbumActivity", "No se pudo cargar a Drako en el Álbum por memoria/recursos", t);
            drakoAlbum = null;
        }

        // Conectar secciones (layoutInicio resuelto como RelativeLayout)
        layoutInicio = findViewById(R.id.layoutInicioAlbum);
        layoutCamara = findViewById(R.id.layoutCamara);
        layoutGaleria = findViewById(R.id.layoutGaleria);
        layoutDetalle = findViewById(R.id.layoutDetalleFoto);

        // Conectar cámara
        previewView = findViewById(R.id.previewCamara);
        imagePreview = findViewById(R.id.imagePreview);
        btnCapturar = findViewById(R.id.btnCapturar);
        btnCambiar = findViewById(R.id.btnCambiarCamara);
        btnGuardar = findViewById(R.id.btnGuardarFoto);
        btnRepetir = findViewById(R.id.btnRepetirFoto);

        // Conectar galería
        gridFotos = findViewById(R.id.gridFotos);
        tvVacio = findViewById(R.id.tvAlbumVacio);
        imageFotoGrande = findViewById(R.id.imageFotoGrande);

        executor = ContextCompat.getMainExecutor(this);

        // Adaptador de fotografías
        adapter = new FotosAdapter();
        gridFotos.setAdapter(adapter);
        cargarFotos();

        // Registrar Callback para el botón Atrás del sistema (Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (layoutDetalle.getVisibility() == View.VISIBLE) {
                    mostrarGaleria();
                } else if (layoutGaleria.getVisibility() == View.VISIBLE) {
                    mostrarInicio();
                } else if (layoutCamara.getVisibility() == View.VISIBLE) {
                    if (fotoPendiente != null) {
                        descartarFoto();
                    }
                    mostrarInicio();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // BOTONES DEL MENÚ PRINCIPAL
        findViewById(R.id.btnIrCamara).setOnClickListener(v -> {
            if (drakoAlbum != null) drakoAlbum.detener();
            abrirCamara();
        });

        findViewById(R.id.btnIrGaleria).setOnClickListener(v -> {
            if (drakoAlbum != null) drakoAlbum.detener();
            mostrarGaleria();
        });

        findViewById(R.id.btnVolverInicio).setOnClickListener(v -> finish());

        // BOTONES DE LA CÁMARA
        btnCapturar.setOnClickListener(v -> tomarFoto());

        btnCambiar.setOnClickListener(v -> {
            cameraFacing = cameraFacing == CameraSelector.LENS_FACING_BACK
                    ? CameraSelector.LENS_FACING_FRONT
                    : CameraSelector.LENS_FACING_BACK;

            iniciarCamara();
        });

        btnGuardar.setOnClickListener(v -> guardarFoto());

        btnRepetir.setOnClickListener(v -> descartarFoto());

        findViewById(R.id.btnVolverCamara).setOnClickListener(v -> {
            if (fotoPendiente != null) {
                descartarFoto();
            }
            mostrarInicio();
        });

        // BOTONES DE LA GALERÍA
        findViewById(R.id.btnVolverGaleria).setOnClickListener(v -> mostrarInicio());

        // Tocar una foto para verla ampliada
        gridFotos.setOnItemClickListener((parent, view, position, id) ->
                mostrarFotoGrande(fotos.get(position)));

        // Mantener presionada una foto para eliminarla
        gridFotos.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmarEliminarFoto(fotos.get(position));
            return true;
        });

        findViewById(R.id.btnVolverDetalle).setOnClickListener(v -> mostrarGaleria());

        // Eliminar desde la fotografía ampliada
        findViewById(R.id.btnEliminarFoto).setOnClickListener(v -> {
            if (fotoSeleccionada != null) {
                confirmarEliminarFoto(fotoSeleccionada);
            }
        });
    }

    // --------------------------------
    // NAVEGACIÓN ENTRE PANTALLAS
    // --------------------------------

    private void ocultarTodo() {
        layoutInicio.setVisibility(View.GONE);
        layoutCamara.setVisibility(View.GONE);
        layoutGaleria.setVisibility(View.GONE);
        layoutDetalle.setVisibility(View.GONE);
    }

    private void mostrarInicio() {
        detenerCamara();
        ocultarTodo();
        layoutInicio.setVisibility(View.VISIBLE);
        if (drakoAlbum != null) {
            try {
                drakoAlbum.iniciar();
            } catch (Throwable t) {
                Log.e("AlbumActivity", "Error al iniciar Drako", t);
            }
        }
    }

    private void abrirCamara() {
        ocultarTodo();
        layoutCamara.setVisibility(View.VISIBLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            permisoCamara.launch(Manifest.permission.CAMERA);
        }
    }

    private void mostrarGaleria() {
        detenerCamara();
        cargarFotos();

        ocultarTodo();
        layoutGaleria.setVisibility(View.VISIBLE);
    }

    private void mostrarFotoGrande(File archivo) {
        fotoSeleccionada = archivo;

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = 2;

        imageFotoGrande.setImageBitmap(
                BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones)
        );

        ocultarTodo();
        layoutDetalle.setVisibility(View.VISIBLE);
    }

    // --------------------------------
    // ELIMINAR FOTOGRAFÍAS
    // --------------------------------

    private void confirmarEliminarFoto(File archivo) {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar fotografía")
                .setMessage("¿Estás seguro de que deseas eliminar esta fotografía? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {

                    if (archivo == null) {
                        return;
                    }

                    long fotoId = db.obtenerIdFoto(
                            usuarioId,
                            archivo.getAbsolutePath()
                    );

                    boolean archivoEliminado =
                            !archivo.exists() || archivo.delete();

                    boolean registroEliminado = false;

                    if (fotoId != -1) {
                        registroEliminado =
                                db.eliminarFoto(fotoId, usuarioId);
                    }

                    if (archivoEliminado && (fotoId == -1 || registroEliminado)) {

                        if (archivo.equals(fotoSeleccionada)) {
                            fotoSeleccionada = null;
                            imageFotoGrande.setImageDrawable(null);
                        }

                        cargarFotos();

                        Toast.makeText(
                                AlbumActivity.this,
                                "Fotografía eliminada correctamente",
                                Toast.LENGTH_SHORT
                        ).show();

                        mostrarGaleria();

                    } else {

                        Toast.makeText(
                                AlbumActivity.this,
                                "No se pudo eliminar completamente la fotografía",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .setNegativeButton(
                        "Cancelar",
                        (dialog, which) -> dialog.dismiss()
                )
                .show();
    }

    // --------------------------------
    // CÁMARA
    // --------------------------------

    private void iniciarCamara() {

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(
                                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                        )
                        .build();

                CameraSelector selector =
                        new CameraSelector.Builder()
                                .requireLensFacing(cameraFacing)
                                .build();

                provider.unbindAll();

                provider.bindToLifecycle(
                        this,
                        selector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                Toast.makeText(this,
                        "No se pudo iniciar esta cámara",
                        Toast.LENGTH_LONG).show();
            }

        }, executor);
    }

    private void detenerCamara() {
        try {
            ProcessCameraProvider.getInstance(this)
                    .get()
                    .unbindAll();
        } catch (Exception ignored) {
        }

        imageCapture = null;
    }

    private File carpetaFotos() {
        File base = getExternalFilesDir(
                android.os.Environment.DIRECTORY_PICTURES
        );

        if (base == null) {
            base = getFilesDir();
        }

        File carpeta = new File(base, "ParqueVivo");

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        return carpeta;
    }

    private void tomarFoto() {

        if (imageCapture == null) {
            Toast.makeText(this,
                    "La cámara todavía no está lista",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = "PV_" +
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss_SSS",
                        Locale.getDefault()
                ).format(new Date()) + ".jpg";

        fotoPendiente = new File(carpetaFotos(), nombre);

        ImageCapture.OutputFileOptions opciones =
                new ImageCapture.OutputFileOptions.Builder(fotoPendiente)
                        .build();

        imageCapture.takePicture(
                opciones,
                executor,
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults output
                    ) {
                        runOnUiThread(() -> mostrarVistaPrevia());
                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException error
                    ) {
                        runOnUiThread(() -> Toast.makeText(
                                AlbumActivity.this,
                                "Error al tomar la foto",
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }
        );
    }

    private void mostrarVistaPrevia() {

        detenerCamara();

        previewView.setVisibility(View.GONE);
        imagePreview.setVisibility(View.VISIBLE);

        imagePreview.setImageBitmap(
                BitmapFactory.decodeFile(fotoPendiente.getAbsolutePath())
        );

        btnCapturar.setVisibility(View.GONE);
        btnCambiar.setVisibility(View.GONE);

        btnGuardar.setVisibility(View.VISIBLE);
        btnRepetir.setVisibility(View.VISIBLE);
    }

    private void guardarFoto() {

        if (fotoPendiente == null || !fotoPendiente.exists()) {
            return;
        }

        if (usuarioId == -1) {
            Toast.makeText(this,
                    "No se pudo identificar al usuario",
                    Toast.LENGTH_LONG).show();
            return;
        }

        boolean guardadaEnGaleria = copiarFotoAGaleria(fotoPendiente);

        String fecha = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        boolean registradaEnBD = db.registrarFoto(
                usuarioId,
                fotoPendiente.getName(),
                fotoPendiente.getAbsolutePath(),
                fecha
        );

        if (registradaEnBD) {
            if (guardadaEnGaleria) {
                Toast.makeText(this,
                        "¡Foto guardada en ParqueVivo y en la galería del celular!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        "Foto guardada en ParqueVivo y registrada correctamente.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,
                    "La foto se guardó, pero no se pudo registrar en la base de datos.",
                    Toast.LENGTH_LONG).show();
        }

        fotoPendiente = null;

        cargarFotos();

        volverACamara();
    }

    private boolean copiarFotoAGaleria(File archivo) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false;
        }

        ContentValues valores = new ContentValues();

        valores.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                archivo.getName()
        );

        valores.put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/jpeg"
        );

        valores.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/ParqueVivo"
        );

        valores.put(
                MediaStore.Images.Media.IS_PENDING,
                1
        );

        Uri uri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                valores
        );

        if (uri == null) {
            return false;
        }

        try (
                FileInputStream entrada =
                        new FileInputStream(archivo);

                OutputStream salida =
                        getContentResolver().openOutputStream(uri)
        ) {

            if (salida == null) {
                getContentResolver().delete(uri, null, null);
                return false;
            }

            byte[] buffer = new byte[8192];
            int cantidad;

            while ((cantidad = entrada.read(buffer)) != -1) {
                salida.write(buffer, 0, cantidad);
            }

            salida.flush();

            ContentValues finalizado = new ContentValues();
            finalizado.put(
                    MediaStore.Images.Media.IS_PENDING,
                    0
            );

            int actualizado = getContentResolver().update(
                    uri,
                    finalizado,
                    null,
                    null
            );

            return actualizado > 0;

        } catch (IOException e) {

            getContentResolver().delete(uri, null, null);
            return false;
        }
    }

    private void descartarFoto() {

        if (fotoPendiente != null && fotoPendiente.exists()) {
            fotoPendiente.delete();
        }

        fotoPendiente = null;
        volverACamara();
    }

    private void volverACamara() {

        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);

        previewView.setVisibility(View.VISIBLE);

        btnCapturar.setVisibility(View.VISIBLE);
        btnCambiar.setVisibility(View.VISIBLE);

        btnGuardar.setVisibility(View.GONE);
        btnRepetir.setVisibility(View.GONE);

        iniciarCamara();
    }

    // --------------------------------
    // GALERÍA
    // --------------------------------

    private void cargarFotos() {

        fotos.clear();

        if (usuarioId == -1 || db == null) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            if (tvVacio != null) {
                tvVacio.setVisibility(View.VISIBLE);
            }
            return;
        }

        Cursor cursor = null;
        try {
            cursor = db.obtenerFotosUsuario(usuarioId);

            while (cursor != null && cursor.moveToNext()) {
                int colIndex = cursor.getColumnIndex("ruta");
                if (colIndex != -1) {
                    String ruta = cursor.getString(colIndex);
                    File archivo = new File(ruta);

                    if (archivo.exists()) {
                        fotos.add(archivo);
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (tvVacio != null) {
            tvVacio.setVisibility(
                    fotos.isEmpty() ? View.VISIBLE : View.GONE
            );
        }
    }

    private class FotosAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fotos.size();
        }

        @Override
        public Object getItem(int position) {
            return fotos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {

            ImageView image = convertView instanceof ImageView
                    ? (ImageView) convertView
                    : new ImageView(AlbumActivity.this);

            int size = (int) (105 * getResources().getDisplayMetrics().density);

            image.setLayoutParams(
                    new GridView.LayoutParams(size, size)
            );

            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setPadding(4, 4, 4, 4);

            BitmapFactory.Options opciones = new BitmapFactory.Options();
            opciones.inSampleSize = 4;

            image.setImageBitmap(
                    BitmapFactory.decodeFile(
                            fotos.get(position).getAbsolutePath(),
                            opciones
                    )
            );

            return image;
        }
    }

    // --------------------------------
    // CICLO DE VIDA (MÚSICA Y DRAKO)
    // --------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        MusicaManager.reanudar(this, MusicaManager.Pista.AMBIENTE);

        if (drakoAlbum != null) {
            try {
                drakoAlbum.iniciar();
            } catch (Throwable t) {
                Log.e("AlbumActivity", "Error al reanudar Drako", t);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicaManager.pausar();

        if (drakoAlbum != null) {
            try {
                drakoAlbum.detener();
            } catch (Throwable t) {
                Log.e("AlbumActivity", "Error al pausar Drako", t);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (drakoAlbum != null) {
            try {
                drakoAlbum.liberar();
            } catch (Throwable t) {
                Log.e("AlbumActivity", "Error al liberar Drako", t);
            }
        }
    }
}