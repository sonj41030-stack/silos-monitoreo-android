package com.sonj.silosmonitoreo.data

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.sonj.silosmonitoreo.model.EstiloTipografia
import com.sonj.silosmonitoreo.model.MensajeChat
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.TamanoFuente
import java.time.LocalDateTime
import java.util.UUID

object PreferenciasManager {
    // null = Automático / Sistema, true = Oscuro, false = Claro
    var modoOscuroManual by mutableStateOf<Boolean?>(null)
    var tamanoFuente by mutableStateOf(TamanoFuente.NORMAL)
    var estiloTipografia by mutableStateOf(EstiloTipografia.MODERNA)
    var alertasSonoras by mutableStateOf(true)
    var vibracionActiva by mutableStateOf(true)

    // Foto de perfil del usuario (capturada con cámara o elegida de galería)
    var fotoPerfilBitmap by mutableStateOf<Bitmap?>(null)

    // ── Configuraciones EXCLUSIVAS de JEFATURA ──
    // Color personalizado de alerta crítica (por defecto rojo industrial 0xFFD64545)
    var colorCriticoPersonalizado by mutableStateOf(Color(0xFFD64545))
    // Color personalizado de advertencia (por defecto amarillo/ámbar 0xFFE5A100)
    var colorAdvertenciaPersonalizado by mutableStateOf(Color(0xFFE5A100))
    // Umbral de stock crítico en porcentaje (por defecto 15%)
    var umbralCriticoPorcentaje by mutableFloatStateOf(15f)
}

object ChatRepository {
    val mensajes = mutableStateListOf<MensajeChat>().apply {
        add(
            MensajeChat(
                id = "c1",
                remitenteNombre = "Carlos Operador",
                remitenteRol = RolUsuario.OPERADOR,
                mensaje = "Recepción de 30 Tn de maíz en Planta Tarapacá completada sin incidentes.",
                fechaHora = LocalDateTime.now().minusMinutes(45),
                esUrgente = false
            )
        )
        add(
            MensajeChat(
                id = "c2",
                remitenteNombre = "Ana Administradora",
                remitenteRol = RolUsuario.ADMINISTRADOR,
                mensaje = "Enterada Carlos. Ya autoricé el despacho para la molienda de alimento broiler.",
                fechaHora = LocalDateTime.now().minusMinutes(30),
                esUrgente = false
            )
        )
        add(
            MensajeChat(
                id = "c3",
                remitenteNombre = "Roberto Jefe",
                remitenteRol = RolUsuario.JEFATURA,
                mensaje = "Atención todos los turnos: monitorear de cerca los silos de soya en Puangue debido a la humedad ambiente.",
                fechaHora = LocalDateTime.now().minusMinutes(12),
                esUrgente = true
            )
        )
    }

    fun enviarMensaje(remitenteNombre: String, remitenteRol: RolUsuario, texto: String, esUrgente: Boolean = false) {
        if (texto.isBlank()) return
        mensajes.add(
            MensajeChat(
                id = UUID.randomUUID().toString(),
                remitenteNombre = remitenteNombre,
                remitenteRol = remitenteRol,
                mensaje = texto.trim(),
                fechaHora = LocalDateTime.now(),
                esUrgente = esUrgente
            )
        )
    }
}
