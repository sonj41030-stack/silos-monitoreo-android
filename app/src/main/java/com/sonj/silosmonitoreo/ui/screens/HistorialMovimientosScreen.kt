package com.sonj.silosmonitoreo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.model.Movimiento
import com.sonj.silosmonitoreo.model.TipoMovimiento
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialMovimientosScreen(
    siloIdFiltro: String? = null,
    onVolver: () -> Unit
) {
    var tipoFiltro by remember { mutableStateOf<TipoMovimiento?>(null) }

    val movimientos = SilosRepository.movimientos.filter { mov ->
        (siloIdFiltro == null || mov.siloId == siloIdFiltro) &&
                (tipoFiltro == null || mov.tipo == tipoFiltro)
    }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Movimientos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = (tipoFiltro == null),
                            onClick = { tipoFiltro = null },
                            label = { Text("Todos los Movimientos") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = (tipoFiltro == TipoMovimiento.ENTRADA),
                            onClick = { tipoFiltro = TipoMovimiento.ENTRADA },
                            label = { Text("Entradas (Cargas)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = (tipoFiltro == TipoMovimiento.SALIDA),
                            onClick = { tipoFiltro = TipoMovimiento.SALIDA },
                            label = { Text("Salidas (Descargas)") }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (movimientos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay registros de movimientos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(movimientos) { mov ->
                        TarjetaMovimientoItem(movimiento = mov, formatter = formatter)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TarjetaMovimientoItem(movimiento: Movimiento, formatter: DateTimeFormatter) {
    val esEntrada = movimiento.tipo == TipoMovimiento.ENTRADA
    val colorBadge = if (esEntrada) Color(0xFF2E9E5B) else Color(0xFFD64545)
    val icono = if (esEntrada) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val signo = if (esEntrada) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colorBadge.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorBadge,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${movimiento.granjaNombre} - ${movimiento.siloNombre}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Responsable: ${movimiento.responsable}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (movimiento.notas.isNotBlank()) {
                    Text(
                        text = "Notas: ${movimiento.notas}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = movimiento.fechaHora.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$signo${movimiento.cantidadToneladas} Tn",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorBadge
            )
        }
    }
}
