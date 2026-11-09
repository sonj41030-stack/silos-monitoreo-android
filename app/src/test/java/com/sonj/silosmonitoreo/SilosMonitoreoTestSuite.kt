package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.DatosPrueba
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.TipoMovimiento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SilosMonitoreoTestSuite {

    @Before
    fun setUp() {
        // Restaura el estado inicial antes de cada test (el Repository es un objeto global compartido)
        SilosRepository.granjas.clear()
        SilosRepository.granjas.addAll(DatosPrueba.granjasDePrueba)
        SilosRepository.movimientos.clear()
        SilosRepository.movimientos.addAll(DatosPrueba.movimientosDePrueba)
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
        assertEquals("Silo A1", silo.nombre)
        assertEquals(80, silo.porcentaje)
        assertEquals(EstadoSilo.NORMAL, silo.estado)
    }
    @Test
    fun test3_RegistroMovimientoEntradaYSalida() {
        val movimientosIniciales = SilosRepository.movimientos.size

        // Registrar una carga (ENTRADA) de 10 toneladas en el silo s2 (sin context: no usa SQLite)
        val exito = SilosRepository.registrarMovimiento(
            context = null,
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
        // Registrar una descarga (SALIDA) en el silo s1 (120 tn de 150)
        val pctAntes = SilosRepository.obtenerSilo("s1")!!.second.porcentaje

        val exito = SilosRepository.registrarMovimiento(
            context = null,
            siloId = "s1",
            tipo = TipoMovimiento.SALIDA,
            cantidadTn = 60,
            responsable = "Operador Test",
            notas = "Despacho masivo"
        )

        val pctDespues = SilosRepository.obtenerSilo("s1")!!.second.porcentaje

        assertTrue("La salida válida debe registrarse", exito)
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

    @Test
    fun test9_SalidaMayorAlStockSeRechaza() {
        // El silo s3 tiene 10 % de 100 tn = 10 tn: sacar 500 tn no debe permitirse
        val movimientosAntes = SilosRepository.movimientos.size
        val pctAntes = SilosRepository.obtenerSilo("s3")!!.second.porcentaje

        val exito = SilosRepository.registrarMovimiento(
            context = null,
            siloId = "s3",
            tipo = TipoMovimiento.SALIDA,
            cantidadTn = 500,
            responsable = "Operador Test",
            notas = "Salida imposible"
        )

        assertFalse("Una salida mayor al stock debe rechazarse", exito)
        assertEquals("No debe agregarse ningún movimiento", movimientosAntes, SilosRepository.movimientos.size)
        assertEquals("El nivel del silo no debe cambiar", pctAntes, SilosRepository.obtenerSilo("s3")!!.second.porcentaje)
    }

    @Test
    fun test10_EntradaMayorALaCapacidadSeRechaza() {
        // El silo s4 está al 90 % de 200 tn (180 tn): agregar 50 tn supera su capacidad
        val exito = SilosRepository.registrarMovimiento(
            context = null,
            siloId = "s4",
            tipo = TipoMovimiento.ENTRADA,
            cantidadTn = 50,
            responsable = "Operador Test",
            notas = "Entrada que desborda"
        )

        assertFalse("Una entrada que supera la capacidad debe rechazarse", exito)
        assertEquals(90, SilosRepository.obtenerSilo("s4")!!.second.porcentaje)
    }
}