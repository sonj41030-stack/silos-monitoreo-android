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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonj.silosmonitoreo.data.ChatRepository
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.ui.components.TarjetaSilo
import com.sonj.silosmonitoreo.ui.viewmodel.ListaSilosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSilosScreen(
    rol: RolUsuario = RolUsuario.OPERADOR,
    onSiloClick: (String) -> Unit = {},
    onRegistrarMovimientoClick: () -> Unit = {},
    onVerHistorialClick: () -> Unit = {},
    onVerAlertasClick: () -> Unit = {},
    onVerDashboardJefaturaClick: () -> Unit = {},
    onVerPerfilClick: () -> Unit = {},
    onVerConfiguracionClick: () -> Unit = {},
    onVerChatClick: () -> Unit = {},
    onCambiarRol: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: ListaSilosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val granjas = viewModel.granjas
    val alertasCantidad = viewModel.alertas.size
    val mensajesChatCantidad = ChatRepository.mensajes.size

    // Sincronizar el rol con el ViewModel al abrir la pantalla
    LaunchedEffect(rol) { viewModel.inicializarRol(rol) }

    val filtroGranjaSeleccionada = uiState.filtroGranja
    val filtroEstadoSeleccionado = uiState.filtroEstado

    val granjasFiltradas = uiState.granjasFiltradas(granjas)

    // Métricas para el resumen ejecutivo de Jefatura — delegadas al ViewModel
    val totalSilos = uiState.totalSilos(granjas)
    val totalCapacidad = uiState.totalCapacidad(granjas)
    val totalLleno = uiState.totalStock(granjas)
    val promedioOcupacion = uiState.promedioOcupacion(granjas)
    val totalCriticos = uiState.totalCriticos(granjas)

    // Estado para el menú desplegable de más opciones (3 puntos)
    var menuMasOpcionesAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Monitoreo de Silos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Ariztía • Vista: ${rol.etiqueta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // Chat Operativo (Terreno ↔ Administración)
                    IconButton(onClick = onVerChatClick) {
                        BadgedBox(
                            badge = {
                                if (mensajesChatCantidad > 0) {
                                    Badge { Text("$mensajesChatCantidad") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Canal Operativo"
                            )
                        }
                    }

                    // Botón de Alertas con Badge contador
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

                    // Únicamente Jefatura ve el acceso directo al Dashboard Ejecutivo
                    if (rol == RolUsuario.JEFATURA) {
                        IconButton(onClick = onVerDashboardJefaturaClick) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Dashboard Jefatura"
                            )
                        }
                    }

                    // Menú de 3 puntos (Más Opciones) para mantener la barra limpia y horizontal
                    Box {
                        IconButton(onClick = { menuMasOpcionesAbierto = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones"
                            )
                        }

                        DropdownMenu(
                            expanded = menuMasOpcionesAbierto,
                            onDismissRequest = { menuMasOpcionesAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mi Perfil") },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                                onClick = {
                                    menuMasOpcionesAbierto = false
                                    onVerPerfilClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración de la App") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    menuMasOpcionesAbierto = false
                                    onVerConfiguracionClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Historial de Movimientos") },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                                onClick = {
                                    menuMasOpcionesAbierto = false
                                    onVerHistorialClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cambiar Rol") },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                                onClick = {
                                    menuMasOpcionesAbierto = false
                                    onCambiarRol()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuMasOpcionesAbierto = false
                                    onCerrarSesion()
                                }
                            )
                        }
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
                            onClick = { viewModel.onFiltroGranjaChanged("Todas") },
                            label = { Text("Todas las Granjas") }
                        )
                    }
                    items(granjas) { granja ->
                        FilterChip(
                            selected = filtroGranjaSeleccionada == granja.nombre,
                            onClick = { viewModel.onFiltroGranjaChanged(granja.nombre) },
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
                            onClick = { viewModel.onFiltroEstadoChanged(null) },
                            label = { Text("Todos los Estados") }
                        )
                    }
                    items(EstadoSilo.entries.toTypedArray()) { estado ->
                        FilterChip(
                            selected = filtroEstadoSeleccionado == estado,
                            onClick = {
                                viewModel.onFiltroEstadoChanged(
                                    if (filtroEstadoSeleccionado == estado) null else estado
                                )
                            },
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