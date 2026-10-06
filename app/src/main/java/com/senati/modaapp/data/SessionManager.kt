package com.senati.modaapp.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("modaapp_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun guardarSesion(usuario: String, rol: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, usuario)
            .putString(KEY_USER_ROLE, rol)
            .apply()
    }

    fun estaLogueado(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun obtenerUsuario(): String = prefs.getString(KEY_USER_NAME, "admin") ?: "admin"

    fun obtenerRol(): String = prefs.getString(KEY_USER_ROLE, "ADMIN") ?: "ADMIN"

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
