package com.sonj.silosmonitoreo.ui.viewmodel

/** Reglas de validacion del formulario de silo. Cada funcion devuelve el error del campo o null. */
object ValidadorSilo {

    const val CAPACIDAD_MAXIMA_TN = 10000
    const val NOMBRE_MIN_CARACTERES = 3

    fun errorNombre(valor: String): String? = when {
        valor.isBlank() -> "Ingresa el nombre del silo."
        valor.trim().length < NOMBRE_MIN_CARACTERES ->
            "El nombre debe tener al menos $NOMBRE_MIN_CARACTERES caracteres."
        else -> null
    }

    fun errorProducto(valor: String): String? =
        if (valor.isBlank()) "Ingresa el producto (ej. Maiz, Trigo)." else null

    fun errorCapacidad(valor: String): String? {
        if (valor.isBlank()) return "Ingresa la capacidad en toneladas."
        val cap = valor.trim().toIntOrNull() ?: return "Ingresa solo numeros enteros."
        return when {
            cap <= 0 -> "La capacidad debe ser mayor a 0."
            cap > CAPACIDAD_MAXIMA_TN -> "La capacidad maxima es $CAPACIDAD_MAXIMA_TN toneladas."
            else -> null
        }
    }

    fun errorToneladas(valor: String, capacidad: String): String? {
        if (valor.isBlank()) return "Ingresa las toneladas actuales."
        val tn = valor.trim().toIntOrNull() ?: return "Ingresa solo numeros enteros."
        if (tn < 0) return "Las toneladas no pueden ser negativas."
        val cap = capacidad.trim().toIntOrNull()
        return if (cap != null && tn > cap) "No puede superar la capacidad ($cap Tn)." else null
    }
}