package com.joro.parquevivo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "parquevivo.db";
    private static final int DB_VERSION = 5;

    // Tabla Usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_USUARIO_ID = "id";
    public static final String COL_USUARIO_NOMBRE = "nombre";
    public static final String COL_USUARIO_EMAIL = "email";
    public static final String COL_USUARIO_SALT = "salt";
    public static final String COL_USUARIO_HASH = "password_hash";
    public static final String COL_USUARIO_PUNTOS = "puntos";

    // Tabla Premios (Visor RA y Modelos GLB)
    public static final String TABLE_PREMIOS = "premios";
    public static final String COL_PREMIO_ID = "id";
    public static final String COL_PREMIO_NOMBRE = "nombre";
    public static final String COL_PREMIO_DESCRIPCION = "descripcion";
    public static final String COL_PREMIO_MODELO_GLB = "modelo_glb";
    public static final String COL_PREMIO_COSTO = "costo";
    public static final String COL_PREMIO_DESBLOQUEADO = "desbloqueado";

    // Tabla Álbum de Aventuras
    public static final String TABLE_ALBUM = "album_aventuras";
    public static final String COL_ALBUM_ID = "id";
    public static final String COL_ALBUM_RUTA = "ruta_foto";
    public static final String COL_ALBUM_TITULO = "titulo";
    public static final String COL_ALBUM_DESCRIPCION = "descripcion";
    public static final String COL_ALBUM_ORIGEN = "origen";
    public static final String COL_ALBUM_FECHA = "fecha";

    // Tabla Fotos (Módulo Álbum)
    public static final String TABLE_FOTOS = "fotos";

    public DatabaseHelper(Context context) {
        super(context != null ? context.getApplicationContext() : null, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_USUARIOS = "CREATE TABLE " + TABLE_USUARIOS + " ("
                + COL_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USUARIO_NOMBRE + " TEXT NOT NULL, "
                + COL_USUARIO_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_USUARIO_SALT + " TEXT NOT NULL, "
                + COL_USUARIO_HASH + " TEXT NOT NULL, "
                + COL_USUARIO_PUNTOS + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE_USUARIOS);

        crearTablaPremios(db);
        crearTablaAlbum(db);
        crearTablaFotos(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 2) {
                crearTablaPremios(db);
            }
            if (oldVersion < 3) {
                crearTablaAlbum(db);
            }
            if (oldVersion < 4) {
                crearTablaFotos(db);
            }
            if (oldVersion < 5) {
                if (!existeColumna(db, TABLE_USUARIOS, COL_USUARIO_PUNTOS)) {
                    db.execSQL("ALTER TABLE " + TABLE_USUARIOS + " ADD COLUMN " + COL_USUARIO_PUNTOS + " INTEGER DEFAULT 0;");
                }
                if (!existeColumna(db, TABLE_PREMIOS, COL_PREMIO_COSTO)) {
                    db.execSQL("ALTER TABLE " + TABLE_PREMIOS + " ADD COLUMN " + COL_PREMIO_COSTO + " INTEGER DEFAULT 100;");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error durante migración de BD", e);
        }
    }

    private boolean existeColumna(SQLiteDatabase db, String tabla, String columna) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tabla + ")", null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    if (nameIndex != -1 && columna.equalsIgnoreCase(cursor.getString(nameIndex))) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    private void crearTablaPremios(SQLiteDatabase db) {
        String CREATE_TABLE_PREMIOS = "CREATE TABLE IF NOT EXISTS " + TABLE_PREMIOS + " (" +
                COL_PREMIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PREMIO_NOMBRE + " TEXT NOT NULL, " +
                COL_PREMIO_DESCRIPCION + " TEXT, " +
                COL_PREMIO_MODELO_GLB + " TEXT NOT NULL, " +
                COL_PREMIO_COSTO + " INTEGER DEFAULT 100, " +
                COL_PREMIO_DESBLOQUEADO + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE_PREMIOS);

        poblarPremiosIniciales(db);
    }

    private void crearTablaAlbum(SQLiteDatabase db) {
        String CREATE_TABLE_ALBUM = "CREATE TABLE IF NOT EXISTS " + TABLE_ALBUM + " (" +
                COL_ALBUM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ALBUM_RUTA + " TEXT NOT NULL, " +
                COL_ALBUM_TITULO + " TEXT NOT NULL, " +
                COL_ALBUM_DESCRIPCION + " TEXT, " +
                COL_ALBUM_ORIGEN + " TEXT, " +
                COL_ALBUM_FECHA + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_TABLE_ALBUM);
    }

    private void crearTablaFotos(SQLiteDatabase db) {
        String CREATE_TABLE_FOTOS = "CREATE TABLE IF NOT EXISTS " + TABLE_FOTOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER NOT NULL, " +
                "nombre_archivo TEXT NOT NULL, " +
                "ruta TEXT NOT NULL, " +
                "fecha TEXT NOT NULL, " +
                "FOREIGN KEY(usuario_id) REFERENCES " + TABLE_USUARIOS + "(id))";
        db.execSQL(CREATE_TABLE_FOTOS);
    }

    private void poblarPremiosIniciales(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PREMIOS, null);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                if (count > 0) return;
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }

        insertarPremioInicial(db, "Drako Morado", "El místico guardián principal de ParqueVivo.", "models/morado.glb", 0, 1);
        insertarPremioInicial(db, "Guardián Rojo", "Furia de fuego y energía del bosque.", "models/rojo.glb", 100, 0);
        insertarPremioInicial(db, "Guardián Verde", "Protector de los árboles y hojas secas.", "models/verde.glb", 150, 0);
        insertarPremioInicial(db, "Guardián Azul", "Espíritu cristalino de los lagos y arroyos.", "models/azul.glb", 200, 0);
    }

    private void insertarPremioInicial(SQLiteDatabase db, String nombre, String desc, String modeloGlb, int costo, int desbloqueado) {
        ContentValues values = new ContentValues();
        values.put(COL_PREMIO_NOMBRE, nombre);
        values.put(COL_PREMIO_DESCRIPCION, desc);
        values.put(COL_PREMIO_MODELO_GLB, modeloGlb);
        values.put(COL_PREMIO_COSTO, costo);
        values.put(COL_PREMIO_DESBLOQUEADO, desbloqueado);
        db.insert(TABLE_PREMIOS, null, values);
    }

    // --------------------------------
    // GESTIÓN DE PARQUEPUNTOS
    // --------------------------------

    public int obtenerPuntosUsuario(long usuarioId) {
        if (usuarioId <= 0) return 0;
        Cursor c = null;
        try {
            c = getReadableDatabase().query(
                    TABLE_USUARIOS,
                    new String[]{COL_USUARIO_PUNTOS},
                    COL_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(usuarioId)},
                    null, null, null
            );
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al obtener puntos del usuario", e);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    public boolean actualizarPuntosUsuario(long usuarioId, int nuevosPuntos) {
        if (usuarioId <= 0) return false;
        ContentValues values = new ContentValues();
        values.put(COL_USUARIO_PUNTOS, nuevosPuntos);
        int filas = getWritableDatabase().update(
                TABLE_USUARIOS,
                values,
                COL_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)}
        );
        return filas > 0;
    }

    public boolean sumarPuntosUsuario(long usuarioId, int puntosASumar) {
        int actuales = obtenerPuntosUsuario(usuarioId);
        return actualizarPuntosUsuario(usuarioId, actuales + puntosASumar);
    }

    // --------------------------------
    // MÉTODOS DE FOTOS Y ÁLBUM
    // --------------------------------

    public boolean insertarFotoAlbum(String rutaFoto, String titulo, String descripcion, String origen) {
        ContentValues values = new ContentValues();
        values.put(COL_ALBUM_RUTA, rutaFoto);
        values.put(COL_ALBUM_TITULO, titulo);
        values.put(COL_ALBUM_DESCRIPCION, descripcion);
        values.put(COL_ALBUM_ORIGEN, origen);

        long id = getWritableDatabase().insert(TABLE_ALBUM, null, values);
        return id != -1;
    }

    public Cursor obtenerFotosAlbum() {
        return getReadableDatabase().query(TABLE_ALBUM, null, null, null, null, null, COL_ALBUM_FECHA + " DESC");
    }

    public boolean registrarFoto(long usuarioId, String nombreArchivo, String ruta, String fecha) {
        if (usuarioId <= 0 || nombreArchivo == null || ruta == null || fecha == null) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("nombre_archivo", nombreArchivo);
        values.put("ruta", ruta);
        values.put("fecha", fecha);

        long resultado = getWritableDatabase().insert(TABLE_FOTOS, null, values);
        return resultado != -1;
    }

    public Cursor obtenerFotosUsuario(long usuarioId) {
        try {
            return getReadableDatabase().query(
                    TABLE_FOTOS,
                    new String[]{"id", "nombre_archivo", "ruta", "fecha"},
                    "usuario_id = ?",
                    new String[]{String.valueOf(usuarioId)},
                    null, null,
                    "id DESC"
            );
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al obtener fotos del usuario", e);
            return null;
        }
    }

    public boolean eliminarFoto(long fotoId, long usuarioId) {
        int resultado = getWritableDatabase().delete(
                TABLE_FOTOS,
                "id = ? AND usuario_id = ?",
                new String[]{String.valueOf(fotoId), String.valueOf(usuarioId)}
        );
        return resultado > 0;
    }

    public long obtenerIdFoto(long usuarioId, String ruta) {
        Cursor c = null;
        try {
            c = getReadableDatabase().query(
                    TABLE_FOTOS,
                    new String[]{"id"},
                    "usuario_id = ? AND ruta = ?",
                    new String[]{String.valueOf(usuarioId), ruta},
                    null, null, null
            );

            if (c != null && c.moveToFirst()) {
                return c.getLong(0);
            }
            return -1;
        } catch (Exception e) {
            return -1;
        } finally {
            if (c != null) c.close();
        }
    }

    // --------------------------------
    // MÉTODOS DE AUTENTICACIÓN Y USUARIOS
    // --------------------------------

    public boolean registrarUsuario(String nombre, String email, String password) {
        if (nombre == null || email == null || password == null) return false;

        String emailLimpio = email.trim().toLowerCase();
        if (emailLimpio.isEmpty() || existeEmail(emailLimpio)) return false;

        String salt = generarSalt();
        ContentValues values = new ContentValues();
        values.put(COL_USUARIO_NOMBRE, nombre.trim());
        values.put(COL_USUARIO_EMAIL, emailLimpio);
        values.put(COL_USUARIO_SALT, salt);
        values.put(COL_USUARIO_HASH, hash(password, salt));

        long id = getWritableDatabase().insert(TABLE_USUARIOS, null, values);
        return id != -1;
    }

    public String validarLogin(String email, String password) {
        if (email == null || password == null) return null;

        String emailLimpio = email.trim().toLowerCase();
        Cursor c = null;

        try {
            c = getReadableDatabase().query(TABLE_USUARIOS,
                    new String[]{COL_USUARIO_NOMBRE, COL_USUARIO_SALT, COL_USUARIO_HASH},
                    COL_USUARIO_EMAIL + " = ?", new String[]{emailLimpio},
                    null, null, null);

            if (c != null && c.moveToFirst()) {
                String nombre = c.getString(0);
                String salt = c.getString(1);
                String guardado = c.getString(2);
                if (guardado.equals(hash(password, salt))) {
                    return nombre;
                }
            }
            return null;
        } finally {
            if (c != null) c.close();
        }
    }

    public long obtenerIdUsuario(String email) {
        if (email == null) return -1;

        String emailLimpio = email.trim().toLowerCase();
        Cursor c = null;

        try {
            c = getReadableDatabase().query(
                    TABLE_USUARIOS,
                    new String[]{COL_USUARIO_ID},
                    COL_USUARIO_EMAIL + " = ?",
                    new String[]{emailLimpio},
                    null, null, null
            );

            if (c != null && c.moveToFirst()) {
                return c.getLong(0);
            }
            return -1;
        } finally {
            if (c != null) c.close();
        }
    }

    private boolean existeEmail(String email) {
        if (email == null) return false;

        Cursor c = null;
        boolean existe = false;
        try {
            c = getReadableDatabase().query(TABLE_USUARIOS, new String[]{COL_USUARIO_ID},
                    COL_USUARIO_EMAIL + " = ?", new String[]{email.trim().toLowerCase()}, null, null, null);
            if (c != null) {
                existe = c.getCount() > 0;
            }
        } finally {
            if (c != null) c.close();
        }
        return existe;
    }

    // --------------------------------
    // MÉTODOS DE GESTIÓN DE PREMIOS (RA)
    // --------------------------------

    public boolean registrarPremio(String nombre, String descripcion, String modeloGlb, boolean desbloqueado) {
        ContentValues values = new ContentValues();
        values.put(COL_PREMIO_NOMBRE, nombre);
        values.put(COL_PREMIO_DESCRIPCION, descripcion);
        values.put(COL_PREMIO_MODELO_GLB, modeloGlb);
        values.put(COL_PREMIO_DESBLOQUEADO, desbloqueado ? 1 : 0);

        long id = getWritableDatabase().insert(TABLE_PREMIOS, null, values);
        return id != -1;
    }

    public boolean desbloquearPremio(int premioId) {
        ContentValues values = new ContentValues();
        values.put(COL_PREMIO_DESBLOQUEADO, 1);
        int filasAfectadas = getWritableDatabase().update(TABLE_PREMIOS, values, COL_PREMIO_ID + "=?", new String[]{String.valueOf(premioId)});
        return filasAfectadas > 0;
    }

    public String getRutaModeloGlb(int premioId) {
        SQLiteDatabase db = getReadableDatabase();
        String ruta = "";
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_PREMIOS,
                    new String[]{COL_PREMIO_MODELO_GLB},
                    COL_PREMIO_ID + "=?",
                    new String[]{String.valueOf(premioId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(COL_PREMIO_MODELO_GLB);
                if (index != -1) {
                    ruta = cursor.getString(index);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return ruta;
    }

    public Cursor obtenerTodosLosPremios() {
        return getReadableDatabase().query(TABLE_PREMIOS, null, null, null, null, null, COL_PREMIO_ID + " ASC");
    }

    // --------------------------------
    // SEGURIDAD Y HASH
    // --------------------------------

    private String generarSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar hash de contraseña", e);
        }
    }
}