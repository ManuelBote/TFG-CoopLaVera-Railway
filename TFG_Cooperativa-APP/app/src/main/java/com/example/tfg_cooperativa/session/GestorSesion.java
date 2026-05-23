package com.example.tfg_cooperativa.session;

import android.content.Context;
import android.content.SharedPreferences;

public class GestorSesion {

    private static final String PREFS_NAME = "coop_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_DNI = "dni";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_CITY = "city";
    private static final String KEY_POSTAL = "postal_code";

    private static GestorSesion instance;
    private final SharedPreferences prefs;

    private GestorSesion(Context appContext) {
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized GestorSesion get(Context context) {
        if (instance == null) {
            instance = new GestorSesion(context.getApplicationContext());
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // Guarda la sesión completa devuelta por el back tras login/registro.
    public void login(String token, int userId, String name, String email,
                      String phone, String dni, boolean admin) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putBoolean(KEY_IS_ADMIN, admin)
                .putString(KEY_TOKEN, token == null ? "" : token)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_NAME, name == null ? "" : name)
                .putString(KEY_EMAIL, email == null ? "" : email)
                .putString(KEY_PHONE, phone == null ? "" : phone)
                .putString(KEY_DNI, dni == null ? "" : dni)
                .apply();
    }

    // Actualiza los datos del usuario sin tocar el token ni el flag de sesión.
    public void updateProfile(String name, String email, String phone, String dni) {
        prefs.edit()
                .putString(KEY_NAME, name == null ? "" : name)
                .putString(KEY_EMAIL, email == null ? "" : email)
                .putString(KEY_PHONE, phone == null ? "" : phone)
                .putString(KEY_DNI, dni == null ? "" : dni)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public int getUserId() { return prefs.getInt(KEY_USER_ID, -1); }
    public String getName() { return prefs.getString(KEY_NAME, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getPhone() { return prefs.getString(KEY_PHONE, ""); }
    public String getDni() { return prefs.getString(KEY_DNI, ""); }
    public String getAddress() { return prefs.getString(KEY_ADDRESS, ""); }
    public String getCity() { return prefs.getString(KEY_CITY, ""); }
    public String getPostalCode() { return prefs.getString(KEY_POSTAL, ""); }

    public void updateAddress(String address, String city, String postalCode) {
        prefs.edit()
                .putString(KEY_ADDRESS, address == null ? "" : address)
                .putString(KEY_CITY, city == null ? "" : city)
                .putString(KEY_POSTAL, postalCode == null ? "" : postalCode)
                .apply();
    }
}
