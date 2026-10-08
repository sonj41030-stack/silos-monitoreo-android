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
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.ui.theme.EstadoAdvertencia
import com.sonj.silosmonitoreo.ui.theme.EstadoCritico
import com.sonj.silosmonitoreo.ui.theme.EstadoNormal

fun colorDeEstado(estado: EstadoSilo): Color = when (estado) {
    EstadoSilo.NORMAL -> EstadoNormal
    EstadoSilo.ADVERTENCIA -> EstadoAdvertencia
    EstadoSilo.CRITICO -> EstadoCritico
}

@Composable
fun TarjetaSilo(
    silo: Silo,
    rol: RolUsuario = RolUsuario.OPERADOR,
    modifier: Modifier = Modifier
) {
    val estado = silo.estado
    val color = colorDeEstado(estado)
    val fraccion = (silo.porcentaje / 100f).coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = silo.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Grano: ${silo.tipoGrano}" + if (rol == RolUsuario.ADMINISTRADOR) " • Capacidad: ${silo.capacidadToneladas} Tn" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = estado.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = color
                    )
                }
            }

            // Exclusivo para ADMINISTRADOR: detalle del porcentaje numérico y la barra de llenado
            if (rol == RolUsuario.ADMINISTRADOR) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${silo.porcentaje}%",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${(silo.porcentaje * silo.capacidadToneladas / 100)} / ${silo.capacidadToneladas} Tn",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
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
                            .background(color)
                    )
                }
            }
        }
    }
}
