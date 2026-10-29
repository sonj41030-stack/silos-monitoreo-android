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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import com.sonj.silosmonitoreo.data.ChatRepository
import com.sonj.silosmonitoreo.data.auth.AuthRepository
import com.sonj.silosmonitoreo.model.MensajeChat
import com.sonj.silosmonitoreo.model.RolUsuario
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatOperativoScreen(
    rol: RolUsuario = RolUsuario.OPERADOR,
    onVolver: () -> Unit
) {
    val usuario = AuthRepository.usuarioAutenticado
    val nombreRemitente = usuario?.nombre ?: when (rol) {
        RolUsuario.OPERADOR -> "Carlos Operador"
        RolUsuario.ADMINISTRADOR -> "Ana Administradora"
        RolUsuario.JEFATURA -> "Roberto Jefe"
    }

    var textoMensaje by remember { mutableStateOf("") }
    var esUrgente by remember { mutableStateOf(false) }

    val plantillasRapidas = listOf(
        "✅ Carga de silo completada",
        "⚠️ Revisar compuerta y sensor",
        "🚚 Camión de granos en báscula",
        "🌾 Nivel bajo: requiere reposición",
        "📋 Turno entregado sin novedades"
    )

    val horaFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Canal Operativo Ariztía", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Operadores ↔ Administración • Melipilla", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Lista de Mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ChatRepository.mensajes) { mensaje ->
                    TarjetaMensajeOperativo(
                        mensaje = mensaje,
                        hora = mensaje.fechaHora.format(horaFormatter),
                        esMio = mensaje.remitenteRol == rol
                    )
                }
            }

            // Chips de Respuestas Rápidas de Terreno
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(plantillasRapidas) { plantilla ->
                    AssistChip(
                        onClick = { textoMensaje = plantilla },
                        label = { Text(plantilla, fontSize = 11.sp) }
                    )
                }
            }

            // Barra inferior para redactar mensaje
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoMensaje,
                    onValueChange = { textoMensaje = it },
                    placeholder = { Text("Escribe un reporte operativo...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botón enviar
                IconButton(
                    onClick = {
                        if (textoMensaje.isNotBlank()) {
                            ChatRepository.enviarMensaje(
                                remitenteNombre = nombreRemitente,
                                remitenteRol = rol,
                                texto = textoMensaje,
                                esUrgente = esUrgente
                            )
                            textoMensaje = ""
                            esUrgente = false
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaMensajeOperativo(
    mensaje: MensajeChat,
    hora: String,
    esMio: Boolean
) {
    val colorRol = when (mensaje.remitenteRol) {
        RolUsuario.OPERADOR -> Color(0xFF2E9E5B)
        RolUsuario.ADMINISTRADOR -> Color(0xFF0288D1)
        RolUsuario.JEFATURA -> Color(0xFF7B1FA2)
    }

    val alineacion = if (esMio) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alineacion
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esMio) 16.dp else 4.dp,
                bottomEnd = if (esMio) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (mensaje.esUrgente) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                } else if (esMio) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorRol)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mensaje.remitenteNombre,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorRol
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${mensaje.remitenteRol.etiqueta})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (mensaje.esUrgente) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("URGENTE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mensaje.mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = hora,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
