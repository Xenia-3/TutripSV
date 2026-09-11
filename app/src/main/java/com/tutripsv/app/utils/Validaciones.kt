package com.tutripsv.app.utils

import android.util.Patterns

object Validaciones {

    fun esCorreoValido(correo: String): Boolean {
        return correo.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }

    fun esPasswordValida(password: String): Boolean {
        return password.length >= 6
    }

    fun esCampoVacio(campo: String): Boolean {
        return campo.trim().isEmpty()
    }

    fun esDescripcionValida(descripcion: String): Boolean {
        return descripcion.trim().length >= 20
    }

    fun esPrecioValido(precio: String): Boolean {
        val numero = precio.toDoubleOrNull() ?: return false
        return numero > 0
    }

    fun esPaisValido(pais: String): Boolean {
        return pais.isNotEmpty() && pais != "Selecciona un país"
    }
}