package com.sonj.silosmonitoreo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.ui.components.TarjetaSilo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSilosScreen(
    rol: RolUsuario = RolUsuario.OPERADOR,
    onSiloClick: (String) -> Unit = {},
    onRegistrarMovimientoClick: () -> Unit = {},
    onVerHistorialClick: () -> Unit = {},
    onVerAlertasClick: () -> Unit = {},
    onVerDashboardJefaturaClick: () -> Unit = {},
    onCambiarRol: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    var filtroGranjaSeleccionada by remember { mutableStateOf("Todas") }
    var filtroEstadoSeleccionado by remember { mutableStateOf<EstadoSilo?>(null) }

    val granjas = SilosRepository.granjas
    val alertasCantidad = SilosRepository.alertas.size

    val granjasFiltradas = granjas.mapNotNull { granja ->
        if (filtroGranjaSeleccionada != "Todas" && granja.nombre != filtroGranjaSeleccionada) {
            null
        } else {
            val silosCoincidentes = granja.silos.filter { silo ->
                filtroEstadoSeleccionado == null || silo.estado == filtroEstadoSeleccionado
            }
            if (silosCoincidentes.isNotEmpty()) {
                granja.copy(silos = silosCoincidentes)
            } else {
                null
            }
        }
    }

    // Métricas para el resumen ejecutivo de Jefatura
    val todosLosSilos = granjas.flatMap { it.silos }
    val totalSilos = todosLosSilos.size
    val totalCapacidad = todosLosSilos.sumOf { it.capacidadToneladas }
    val totalLleno = todosLosSilos.sumOf { (it.porcentaje * it.capacidadToneladas) / 100 }
    val promedioOcupacion = if (totalCapacidad > 0) (totalLleno * 100) / totalCapacidad else 0
    val totalCriticos = todosLosSilos.count { it.estado == EstadoSilo.CRITICO }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Monitoreo de Silos",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Vista: ${rol.etiqueta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Únicamente Jefatura ve el acceso directo al Dashboard Ejecutivo
                    if (rol == RolUsuario.JEFATURA) {
                        IconButton(onClick = onVerDashboardJefaturaClick) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Dashboard Jefatura"
                            )
                        }
                    }
                    // Botón de Alertas con Badge contador (accesible para todos los roles)
                    IconButton(onClick = onVerAlertasClick) {
                        BadgedBox(
                            badge = {
                                if (alertasCantidad > 0) {
                                    Badge { Text("$alertasCantidad") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alertas"
                            )
                        }
                    }
                    // Botón de Historial de Movimientos
                    IconButton(onClick = onVerHistorialClick) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial de Movimientos"
                        )
                    }
                    // Cambiar Rol
                    IconButton(onClick = onCambiarRol) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Cambiar rol"
                        )
                    }
                    // Cerrar Sesión
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // ÚNICAMENTE Operador y Administrador pueden registrar movimientos en el campo
            if (rol != RolUsuario.JEFATURA) {
                FloatingActionButton(
                    onClick = onRegistrarMovimientoClick,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar Movimiento")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tarjeta de Resumen Ejecutivo: ÚNICAMENTE visible para JEFATURA
            if (rol == RolUsuario.JEFATURA) {
                ResumenEjecutivoCard(
                    totalSilos = totalSilos,
                    promedioOcupacion = promedioOcupacion,
                    totalLleno = totalLleno,
                    totalCapacidad = totalCapacidad,
                    totalCriticos = totalCriticos,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onVerDashboardJefaturaClick() }
                )
            }

            // Filtros de Granja y Estado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Filtrar por granja:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filtroGranjaSeleccionada == "Todas",
                            onClick = { filtroGranjaSeleccionada = "Todas" },
                            label = { Text("Todas las Granjas") }
                        )
                    }
                    items(granjas) { granja ->
                        FilterChip(
                            selected = filtroGranjaSeleccionada == granja.nombre,
                            onClick = { filtroGranjaSeleccionada = granja.nombre },
                            label = { Text(granja.nombre) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Filtrar por estado:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filtroEstadoSeleccionado == null,
                            onClick = { filtroEstadoSeleccionado = null },
                            label = { Text("Todos los Estados") }
                        )
                    }
                    items(EstadoSilo.entries.toTypedArray()) { estado ->
                        FilterChip(
                            selected = filtroEstadoSeleccionado == estado,
                            onClick = { filtroEstadoSeleccionado = if (filtroEstadoSeleccionado == estado) null else estado },
                            label = { Text(estado.etiqueta) }
                        )
                    }
                }
            }

            // Lista de Granjas y Silos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (granjasFiltradas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron silos con los filtros aplicados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    granjasFiltradas.forEach { granja ->
                        item {
                            TarjetaEncabezadoGranja(granja = granja, rol = rol)
                        }
                        items(granja.silos) { silo ->
                            TarjetaSilo(
                                silo = silo,
                                rol = rol,
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .clickable { onSiloClick(silo.id) }
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ResumenEjecutivoCard(
    totalSilos: Int,
    promedioOcupacion: Int,
    totalLleno: Int,
    totalCapacidad: Int,
    totalCriticos: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resumen Ejecutivo (Jefatura)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricaItem(titulo = "Ocupación Prom.", valor = "$promedioOcupacion%")
                MetricaItem(titulo = "Silos Activos", valor = "$totalSilos")
                MetricaItem(titulo = "Total Stock", valor = "$totalLleno / $totalCapacidad Tn")
                MetricaItem(titulo = "Alertas Críticas", valor = "$totalCriticos")
            }
        }
    }
}

@Composable
private fun MetricaItem(titulo: String, valor: String) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun TarjetaEncabezadoGranja(granja: Granja, rol: RolUsuario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = granja.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                val extraInfo = when (rol) {
                    RolUsuario.JEFATURA -> {
                        val totalCap = granja.silos.sumOf { it.capacidadToneladas }
                        val totalStock = granja.silos.sumOf { (it.porcentaje * it.capacidadToneladas) / 100 }
                        " • Stock: $totalStock / $totalCap Tn"
                    }
                    RolUsuario.ADMINISTRADOR -> {
                        val totalCap = granja.silos.sumOf { it.capacidadToneladas }
                        " • Capacidad: $totalCap Tn"
                    }
                    else -> ""
                }
                Text(
                    text = "${granja.ubicacion} • ${granja.silos.size} silos$extraInfo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
