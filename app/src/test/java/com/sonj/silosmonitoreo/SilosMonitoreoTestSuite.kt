package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SilosMonitoreoTestSuite {

    @Before
    fun setUp() {
        // Recalcular alertas y verificar estado inicial
        SilosRepository.recalcularAlertas()
    }

    @Test
    fun test1_ListadoGranjasYSilos() {
        val granjas = SilosRepository.granjas
        assertTrue("Debe haber granjas cargadas", granjas.isNotEmpty())

        val totalSilos = granjas.flatMap { it.silos }
        assertTrue("Debe haber silos en las granjas", totalSilos.size >= 7)
    }

    @Test
    fun test2_DetalleSiloNivelYEstado() {
        val granjaYSilo = SilosRepository.obtenerSilo("s1")
        assertNotNull("El silo s1 debe existir", granjaYSilo)

        val silo = granjaYSilo!!.second
        assertEquals("Silo 1", silo.nombre)
        assertEquals(80, silo.porcentaje)
        assertEquals(EstadoSilo.NORMAL, silo.estado)
    }

    @Test
    fun test3_RegistroMovimientoEntradaYSalida() {
        val movimientosIniciales = SilosRepository.movimientos.size

        // Registrar una carga (ENTRADA) de 10 toneladas en Silo 2 (Granja Norte - s2)
        val exito = SilosRepository.registrarMovimiento(
            siloId = "s2",
            tipo = TipoMovimiento.ENTRADA,
            cantidadTn = 10,
            responsable = "Operador Test",
            notas = "Carga de prueba unitaria"
        )

        assertTrue("El movimiento debe registrarse correctamente", exito)
        assertEquals("El total de movimientos debe incrementarse", movimientosIniciales + 1, SilosRepository.movimientos.size)

        val ultimoMovimiento = SilosRepository.movimientos.first()
        assertEquals("s2", ultimoMovimiento.siloId)
        assertEquals(TipoMovimiento.ENTRADA, ultimoMovimiento.tipo)
        assertEquals(10, ultimoMovimiento.cantidadToneladas)
    }

    @Test
    fun test4_RegistroMovimientoSalidaYBajaNivel() {
        // Registrar una descarga (SALIDA) en Silo 1 (s1)
        val granjaYSiloAntes = SilosRepository.obtenerSilo("s1")!!
        val pctAntes = granjaYSiloAntes.second.porcentaje

        SilosRepository.registrarMovimiento(
            siloId = "s1",
            tipo = TipoMovimiento.SALIDA,
            cantidadTn = 60,
            responsable = "Operador Test",
            notas = "Despacho masivo"
        )

        val granjaYSiloDespues = SilosRepository.obtenerSilo("s1")!!
        val pctDespues = granjaYSiloDespues.second.porcentaje

        assertTrue("El porcentaje debe haber disminuido", pctDespues < pctAntes)
    }

    @Test
    fun test5_AlertasYNotificaciones() {
        SilosRepository.recalcularAlertas()
        val alertas = SilosRepository.alertas
        assertTrue("Debe haber alertas generadas para los silos en nivel crítico o advertencia", alertas.isNotEmpty())

        val tieneAlertasCriticas = alertas.any { it.estado == EstadoSilo.CRITICO }
        assertTrue("Debe haber al menos una alerta crítica", tieneAlertasCriticas)
    }

    @Test
    fun test6_HistorialMovimientosCronologico() {
        val historial = SilosRepository.movimientos
        assertTrue("El historial debe contener registros de prueba", historial.size >= 3)
    }

    @Test
    fun test7_DashboardResumenJefaturaCalculos() {
        val todosLosSilos = SilosRepository.granjas.flatMap { it.silos }
        val totalCapacidad = todosLosSilos.sumOf { it.capacidadToneladas }
        val totalLleno = todosLosSilos.sumOf { (it.porcentaje * it.capacidadToneladas) / 100 }
        val promedioOcupacion = if (totalCapacidad > 0) (totalLleno * 100) / totalCapacidad else 0

        assertTrue("La capacidad total debe ser mayor a 0", totalCapacidad > 0)
        assertTrue("El promedio de ocupación debe ser un porcentaje válido", promedioOcupacion in 0..100)
    }

    @Test
    fun test8_PrivilegiosRoles() {
        assertEquals("Operador", RolUsuario.OPERADOR.etiqueta)
        assertEquals("Administrador", RolUsuario.ADMINISTRADOR.etiqueta)
        assertEquals("Jefatura", RolUsuario.JEFATURA.etiqueta)
    }
}
