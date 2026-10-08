package com.sonj.silosmonitoreo.model

data class Granja(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val silos: List<Silo>
)
