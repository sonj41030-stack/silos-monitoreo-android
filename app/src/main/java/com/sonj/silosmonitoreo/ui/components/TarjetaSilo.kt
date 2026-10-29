package com.sonj.silosmonitoreo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.data.PreferenciasManager
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.ui.theme.ColorCebada
import com.sonj.silosmonitoreo.ui.theme.ColorGirasol
import com.sonj.silosmonitoreo.ui.theme.ColorGranoDefault
import com.sonj.silosmonitoreo.ui.theme.ColorMaiz
import com.sonj.silosmonitoreo.ui.theme.ColorMaizBlanco
import com.sonj.silosmonitoreo.ui.theme.ColorSorgo
import com.sonj.silosmonitoreo.ui.theme.ColorSoya
import com.sonj.silosmonitoreo.ui.theme.ColorTrigo
import com.sonj.silosmonitoreo.ui.theme.EstadoAdvertencia
import com.sonj.silosmonitoreo.ui.theme.EstadoCritico
import com.sonj.silosmonitoreo.ui.theme.EstadoNormal

fun colorDeEstado(estado: EstadoSilo): Color = when (estado) {
    EstadoSilo.NORMAL -> EstadoNormal
    EstadoSilo.ADVERTENCIA -> PreferenciasManager.colorAdvertenciaPersonalizado
    EstadoSilo.CRITICO -> PreferenciasManager.colorCriticoPersonalizado
}

fun colorDeGrano(tipoGrano: String): Color = when (tipoGrano.lowercase()) {
    "maíz", "maiz" -> ColorMaiz
    "soya", "soja" -> ColorSoya
    "trigo" -> ColorTrigo
    "girasol" -> ColorGirasol
    "cebada" -> ColorCebada
    "sorgo" -> ColorSorgo
    "maíz blanco", "maiz blanco" -> ColorMaizBlanco
    else -> ColorGranoDefault
}

@Composable
fun TarjetaSilo(
    silo: Silo,
    rol: RolUsuario = RolUsuario.OPERADOR,
    modifier: Modifier = Modifier
) {
    val estado = silo.estado
    val colorEstado = colorDeEstado(estado)
    val colorGrano = colorDeGrano(silo.tipoGrano)
    val fraccion = (silo.porcentaje / 100f).coerceIn(0f, 1f)
    val stockTn = (silo.porcentaje * silo.capacidadToneladas) / 100

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Nombre y Estado del Silo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = silo.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Destino: ${silo.destinoUso}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge de Estado (Normal / Advertencia / Crítico)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorEstado.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colorEstado)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = estado.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorEstado
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila de Tipo de Grano con Chip visual específico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de Grano (Materia Prima de Ariztía)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorGrano.copy(alpha = 0.20f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = silo.iconoGrano,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = silo.tipoGrano,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Capacidad y Stock visible según rol
                if (rol != RolUsuario.OPERADOR) {
                    Text(
                        text = "$stockTn / ${silo.capacidadToneladas} Tn (${silo.porcentaje}%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Nivel: ${silo.porcentaje}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorEstado
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra horizontal de nivel disponible para todos los roles (Retroalimentación visual)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccion)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorEstado)
                )
            }
        }
    }
}

