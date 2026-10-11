package com.joro.parquevivo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "parquevivo.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "usuarios";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "email TEXT NOT NULL UNIQUE, "
                + "salt TEXT NOT NULL, "
                + "password_hash TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /** Devuelve true si el usuario se creó; false si el correo ya existe. */
    public boolean registrarUsuario(String nombre, String email, String password) {
        if (existeEmail(email)) return false;

        String salt = generarSalt();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("email", email.toLowerCase());
        values.put("salt", salt);
        values.put("password_hash", hash(password, salt));

        long id = getWritableDatabase().insert(TABLE, null, values);
        return id != -1;
    }

    /** Devuelve el nombre del usuario si las credenciales son correctas; null si no. */
    public String validarLogin(String email, String password) {
        Cursor c = getReadableDatabase().query(TABLE,
                new String[]{"nombre", "salt", "password_hash"},
                "email = ?", new String[]{email.toLowerCase()},
                null, null, null);
        try {
            if (c.moveToFirst()) {
                String nombre = c.getString(0);
                String salt = c.getString(1);
                String guardado = c.getString(2);
                if (guardado.equals(hash(password, salt))) {
                    return nombre;
                }
            }
            return null;
        } finally {
            c.close();
        }
    }

    private boolean existeEmail(String email) {
        Cursor c = getReadableDatabase().query(TABLE, new String[]{"id"},
                "email = ?", new String[]{email.toLowerCase()}, null, null, null);
        boolean existe = c.getCount() > 0;
        c.close();
        return existe;
    }

    private String generarSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes("UTF-8"));
            byte[] digest = md.digest(password.getBytes("UTF-8"));
            return Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}