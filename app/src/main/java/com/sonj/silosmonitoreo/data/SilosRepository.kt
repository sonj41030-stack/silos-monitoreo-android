package com.sonj.silosmonitoreo.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.sonj.silosmonitoreo.data.api.AjusteSiloRequest
import com.sonj.silosmonitoreo.data.api.ApiClient
import com.sonj.silosmonitoreo.data.api.MovimientoApiRequest
import com.sonj.silosmonitoreo.data.api.MovimientosApi
import com.sonj.silosmonitoreo.data.api.Resultado
import com.sonj.silosmonitoreo.data.api.SilosApi
import com.sonj.silosmonitoreo.data.api.llamarApi
import com.sonj.silosmonitoreo.data.api.toModel
import com.sonj.silosmonitoreo.model.Alerta
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.Movimiento
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import java.util.UUID

object SilosRepository {

    // Inyectables: en los tests se reemplazan por mocks.
    var silosApi: SilosApi = ApiClient.silosApi
    var movimientosApi: MovimientosApi = ApiClient.movimientosApi

    val granjas = mutableStateListOf<Granja>()
    val movimientos = mutableStateListOf<Movimiento>()
    val alertas = mutableStateListOf<Alerta>()

    @Deprecated("Ya no se usa SQLite: los datos vienen de los microservicios. Quita esta llamada.")
    fun cargarMovimientosDesdeDB(context: Context) { /* sin efecto */ }

    fun limpiar() {
        granjas.clear()
        movimientos.clear()
        alertas.clear()
    }

    /** GET ms-silos /api/granjas (incluye los silos de cada granja). */
    suspend fun cargarGranjas(): Resultado<Unit> =
        when (val r = llamarApi { silosApi.granjas() }) {
            is Resultado.Ok -> {
                granjas.clear()
                granjas.addAll(r.data.map { it.toModel() })
                recalcularAlertas()
                Resultado.Ok(Unit)
            }
            is Resultado.Error -> r
        }

    /** GET ms-movimientos /api/movimientos. Llamar después de cargarGranjas() para tener nombres. */
    suspend fun cargarMovimientos(siloId: String? = null): Resultado<Unit> =
        when (val r = llamarApi { movimientosApi.listar(siloId) }) {
            is Resultado.Ok -> {
                movimientos.clear()
                movimientos.addAll(r.data.map { m ->
                    val encontrado = obtenerSilo(m.siloId)
                    m.toModel(
                        siloNombre = encontrado?.second?.nombre ?: "Silo desconocido",
                        granjaNombre = encontrado?.first?.nombre ?: "-"
                    )
                })
                Resultado.Ok(Unit)
            }
            is Resultado.Error -> r
        }

    fun obtenerSilo(siloId: String): Pair<Granja, Silo>? {
        for (g in granjas) {
            val s = g.silos.firstOrNull { it.id == siloId }
            if (s != null) return Pair(g, s)
        }
        return null
    }

    /**
     * Un movimiento requiere DOS microservicios (no se llaman entre sí):
     * 1) ms-silos actualiza el stock y valida capacidad/disponibilidad.
     * 2) ms-movimientos guarda el historial.
     * Si el paso 2 falla, se revierte el paso 1 con un ajuste inverso (compensación).
     */
    suspend fun registrarMovimiento(
        siloId: String,
        tipo: TipoMovimiento,
        cantidadTn: Int,
        notas: String
    ): Resultado<Unit> {
        val kg = cantidadTn * 1000.0

        val ajuste = llamarApi { silosApi.ajustar(siloId, AjusteSiloRequest(tipo.name, kg)) }
        if (ajuste is Resultado.Error) return ajuste

        val historial = llamarApi {
            movimientosApi.registrar(MovimientoApiRequest(siloId, tipo.name, kg, notas.trim().ifBlank { null }))
        }
        if (historial is Resultado.Error) {
            val inverso = if (tipo == TipoMovimiento.ENTRADA) TipoMovimiento.SALIDA else TipoMovimiento.ENTRADA
            llamarApi { silosApi.ajustar(siloId, AjusteSiloRequest(inverso.name, kg)) }
            return Resultado.Error("No se pudo guardar el historial; el stock se revirtió. ${historial.mensaje}")
        }

        cargarGranjas()
        cargarMovimientos()
        return Resultado.Ok(Unit)
    }

    fun recalcularAlertas() {
        alertas.clear()
        for (granja in granjas) {
            for (silo in granja.silos) {
                if (silo.estado == EstadoSilo.CRITICO) {
                    alertas.add(
                        Alerta(
                            id = UUID.randomUUID().toString(),
                            siloId = silo.id,
                            siloNombre = silo.nombre,
                            granjaNombre = granja.nombre,
                            estado = EstadoSilo.CRITICO,
                            porcentajeActual = silo.porcentaje,
                            mensaje = "Nivel crítico en ${silo.nombre} (${granja.nombre}): ${silo.porcentaje}% de capacidad restante."
                        )
                    )
                } else if (silo.estado == EstadoSilo.ADVERTENCIA) {
                    alertas.add(
                        Alerta(
                            id = UUID.randomUUID().toString(),
                            siloId = silo.id,
                            siloNombre = silo.nombre,
                            granjaNombre = granja.nombre,
                            estado = EstadoSilo.ADVERTENCIA,
                            porcentajeActual = silo.porcentaje,
                            mensaje = "Nivel de advertencia en ${silo.nombre} (${granja.nombre}): ${silo.porcentaje}% de capacidad."
                        )
                    )
                }
            }
        }
    }
}
