package com.sonj.silosmonitoreo.model

data class Silo(
    val id: String,
    val nombre: String,
    val porcentaje: Int,
    val tipoGrano: String = "Maíz",
    val capacidadToneladas: Int = 100,
    val destinoUso: String = "Nutrición Avícola (Pollos Ariztía)"
) {
    val estado: EstadoSilo
        get() = EstadoSilo.desdePorcentaje(porcentaje.toFloat())

    val iconoGrano: String
        get() = when (tipoGrano.lowercase()) {
            "maíz", "maiz" -> "🌽"
            "soya", "soja" -> "🌱"
            "trigo" -> "🌾"
            "girasol" -> "🌻"
            "cebada" -> "🌾"
            "sorgo" -> "🌾"
            "maíz blanco", "maiz blanco" -> "🌽"
            else -> "📦"
        }
}

