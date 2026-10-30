package com.sonj.silosmonitoreo.ui.viewmodel

/**
 * Reglas de validación del formulario "Registrar Movimiento".
 * Cada función devuelve el mensaje de error del campo, o null si el valor es válido.
 * Se mantienen separadas del ViewModel para que sean fáciles de leer, probar y modificar.
 */
object ValidadorMovimiento {

    const val CANTIDAD_MAXIMA_TN = 999
    const val NOTAS_MAX_CARACTERES = 100
    const val RESPONSABLE_MIN_CARACTERES = 3

    fun errorCantidad(valor: String): String? {
        if (valor.isBlank()) return "Ingresa la cantidad en toneladas."
        val cantidad = valor.trim().toIntOrNull()
            ?: return "Ingresa solo números enteros (sin letras ni decimales)."
        return when {
            cantidad <= 0 -> "La cantidad debe ser mayor a 0."
            cantidad > CANTIDAD_MAXIMA_TN ->
                "La cantidad máxima permitida es $CANTIDAD_MAXIMA_TN toneladas."
            else -> null
        }
    }

    fun errorResponsable(valor: String): String? = when {
        valor.isBlank() -> "Ingresa el nombre del responsable."
        valor.trim().length < RESPONSABLE_MIN_CARACTERES ->
            "El nombre debe tener al menos $RESPONSABLE_MIN_CARACTERES caracteres."
        else -> null
    }

    fun errorNotas(valor: String): String? =
        if (valor.length > NOTAS_MAX_CARACTERES) {
            "Máximo $NOTAS_MAX_CARACTERES caracteres (actual: ${valor.length})."
        } else null
}
