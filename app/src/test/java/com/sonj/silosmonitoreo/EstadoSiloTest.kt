package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.DatosPrueba
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Silo
import org.junit.Assert.assertEquals
import org.junit.Test

class EstadoSiloTest {

    @Test
    fun testEstadoSiloCritico() {
        val estado = EstadoSilo.desdePorcentaje(10f)
        assertEquals(EstadoSilo.CRITICO, estado)
    }

    @Test
    fun testEstadoSiloAdvertencia() {
        val estado = EstadoSilo.desdePorcentaje(25f)
        assertEquals(EstadoSilo.ADVERTENCIA, estado)
    }

    @Test
    fun testEstadoSiloNormal() {
        val estado = EstadoSilo.desdePorcentaje(80f)
        assertEquals(EstadoSilo.NORMAL, estado)
    }

    @Test
    fun testSiloModelEstadoProperty() {
        val siloCritico = Silo("s1", "Silo Test Crítico", 12)
        assertEquals(EstadoSilo.CRITICO, siloCritico.estado)

        val siloAdvertencia = Silo("s2", "Silo Test Advertencia", 20)
        assertEquals(EstadoSilo.ADVERTENCIA, siloAdvertencia.estado)

        val siloNormal = Silo("s3", "Silo Test Normal", 50)
        assertEquals(EstadoSilo.NORMAL, siloNormal.estado)
    }

    @Test
    fun testDatosPruebaIntegrity() {
        val granjas = DatosPrueba.granjasDePrueba
        assertEquals(3, granjas.size)

        val totalSilos = granjas.flatMap { it.silos }.size
        assertEquals(7, totalSilos)
    }
}
