package com.tutripsv.app.utils

import android.content.Context
import android.content.SharedPreferences

class SesionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("TutripSVPrefs", Context.MODE_PRIVATE)

    fun guardarSesion(usuarioId: String, correo: String, nombre: String) {
        prefs.edit()
            .putString("usuarioId", usuarioId)
            .putString("correo", correo)
            .putString("nombre", nombre)
            .putBoolean("logueado", true)
            .apply()
    }

    fun obtenerUsuarioId(): String = prefs.getString("usuarioId", "") ?: ""
    fun obtenerCorreo(): String = prefs.getString("correo", "") ?: ""
    fun obtenerNombre(): String = prefs.getString("nombre", "") ?: ""
    fun estaLogueado(): Boolean = prefs.getBoolean("logueado", false)

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}