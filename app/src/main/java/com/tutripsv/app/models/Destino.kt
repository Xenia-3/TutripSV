package com.tutripsv.app.models

import com.google.firebase.firestore.DocumentId

data class Destino(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val pais: String = "",
    val precio: Double = 0.0,
    val descripcion: String = "",
    val imagenRuta: String = "",
    val usuarioId: String = ""
)
