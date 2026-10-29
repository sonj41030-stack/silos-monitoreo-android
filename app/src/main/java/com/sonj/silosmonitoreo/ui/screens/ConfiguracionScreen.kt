package com.sonj.silosmonitoreo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonj.silosmonitoreo.data.PreferenciasManager
import com.sonj.silosmonitoreo.model.EstiloTipografia
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.TamanoFuente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    rol: RolUsuario = RolUsuario.OPERADOR,
    onVolver: () -> Unit
) {
    val esJefatura = (rol == RolUsuario.JEFATURA)

    // Paletas disponibles para personalización por Jefatura
    val coloresCriticosDisponibles = listOf(
        Pair("Rojo Industrial", Color(0xFFD64545)),
        Pair("Carmesí Ariztía", Color(0xFFC2185B)),
        Pair("Rojo Fuego", Color(0xFFE53935))
    )

    val coloresAdvertenciaDisponibles = listOf(
        Pair("Ámbar", Color(0xFFE5A100)),
        Pair("Naranja Melipilla", Color(0xFFF57C00)),
        Pair("Dorado Intenso", Color(0xFFFFA000))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de la App") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 1. Tema de la Aplicación ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tema y Apariencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Selecciona el modo visual:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = PreferenciasManager.modoOscuroManual == null,
                                onClick = { PreferenciasManager.modoOscuroManual = null },
                                label = { Text("Automático (Sistema)") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = PreferenciasManager.modoOscuroManual == false,
                                onClick = { PreferenciasManager.modoOscuroManual = false },
                                leadingIcon = {
                                    Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                label = { Text("Claro ☀️") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = PreferenciasManager.modoOscuroManual == true,
                                onClick = { PreferenciasManager.modoOscuroManual = true },
                                leadingIcon = {
                                    Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                label = { Text("Oscuro 🌙") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 2. Tipografía y Tamaño del Texto ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Texto y Tipografía",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tamaño del texto: ${PreferenciasManager.tamanoFuente.etiqueta}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TamanoFuente.entries.forEach { tam ->
                            FilterChip(
                                selected = PreferenciasManager.tamanoFuente == tam,
                                onClick = { PreferenciasManager.tamanoFuente = tam },
                                label = { Text(tam.etiqueta, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vista previa del texto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Vista previa: Silo A1 (Maíz) • Ariztía Melipilla",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Estilo de fuente:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(EstiloTipografia.entries) { estilo ->
                            FilterChip(
                                selected = PreferenciasManager.estiloTipografia == estilo,
                                onClick = { PreferenciasManager.estiloTipografia = estilo },
                                label = { Text(estilo.etiqueta) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 3. Parámetros del Monitoreo y Colores (CONTROL POR ROL) ──
            if (esJefatura) {
                // Jefatura tiene acceso completo a modificar colores y umbrales telemétricos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personalización de Silos (Jefatura)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Color para Alerta Crítica
                        Text(
                            text = "Color para Estado Crítico:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            coloresCriticosDisponibles.forEach { (nombre, c) ->
                                val seleccionado = PreferenciasManager.colorCriticoPersonalizado == c
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(c)
                                        .border(
                                            width = if (seleccionado) 3.dp else 1.dp,
                                            color = if (seleccionado) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { PreferenciasManager.colorCriticoPersonalizado = c },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (seleccionado) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Color para Estado Advertencia
                        Text(
                            text = "Color para Estado de Advertencia:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            coloresAdvertenciaDisponibles.forEach { (nombre, c) ->
                                val seleccionado = PreferenciasManager.colorAdvertenciaPersonalizado == c
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(c)
                                        .border(
                                            width = if (seleccionado) 3.dp else 1.dp,
                                            color = if (seleccionado) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { PreferenciasManager.colorAdvertenciaPersonalizado = c },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (seleccionado) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Umbral Crítico
                        Text(
                            text = "Umbral Crítico: < ${PreferenciasManager.umbralCriticoPorcentaje.toInt()}% de capacidad",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = PreferenciasManager.umbralCriticoPorcentaje,
                            onValueChange = { PreferenciasManager.umbralCriticoPorcentaje = it },
                            valueRange = 10f..25f,
                            steps = 2
                        )
                    }
                }
            } else {
                // Operador o Admin: Tarjeta informativa con candado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Personalización de Silos (Restringido)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "La modificación de colores y umbrales telemétricos está reservada para Jefatura. Tu rol (${rol.etiqueta}) tiene permisos de operación.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 4. Notificaciones y Alertas Sonoras ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Alertas Telemétricas de Silos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Avisos sonoros (< 15%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Sonido de alarma en silos críticos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = PreferenciasManager.alertasSonoras,
                            onCheckedChange = { PreferenciasManager.alertasSonoras = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Vibración táctil", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Alerta háptica en galpones ruidosos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = PreferenciasManager.vibracionActiva,
                            onCheckedChange = { PreferenciasManager.vibracionActiva = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Restaurar Configuración Predeterminada
            OutlinedButton(
                onClick = {
                    PreferenciasManager.modoOscuroManual = null
                    PreferenciasManager.tamanoFuente = TamanoFuente.NORMAL
                    PreferenciasManager.estiloTipografia = EstiloTipografia.MODERNA
                    PreferenciasManager.alertasSonoras = true
                    PreferenciasManager.vibracionActiva = true
                    PreferenciasManager.colorCriticoPersonalizado = Color(0xFFD64545)
                    PreferenciasManager.colorAdvertenciaPersonalizado = Color(0xFFE5A100)
                    PreferenciasManager.umbralCriticoPorcentaje = 15f
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restaurar Valores Predeterminados")
            }
        }
    }
}
