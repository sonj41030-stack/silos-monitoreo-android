package com.sonj.silosmonitoreo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sonj.silosmonitoreo.data.PreferenciasManager
import com.sonj.silosmonitoreo.model.EstiloTipografia

private val EsquemaClaro = lightColorScheme(
    primary = Texto,
    onPrimary = Superficie,
    secondary = TextoSecundario,
    tertiary = Acento,
    background = Fondo,
    surface = Superficie,
    surfaceVariant = Pista,
    onBackground = Texto,
    onSurface = Texto,
    onSurfaceVariant = TextoSecundario
)

private val EsquemaOscuro = darkColorScheme(
    primary = TextoOscuro,
    onPrimary = FondoOscuro,
    secondary = TextoSecundarioOscuro,
    tertiary = Acento,
    background = FondoOscuro,
    surface = SuperficieOscura,
    surfaceVariant = PistaOscura,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    onSurfaceVariant = TextoSecundarioOscuro
)

@Composable
fun SilosMonitoreoTheme(
    content: @Composable () -> Unit
) {
    // Si el usuario configuró manualmente Modo Oscuro o Claro en Perfil, se respeta; sino usa el sistema
    val esOscuro = PreferenciasManager.modoOscuroManual ?: isSystemInDarkTheme()

    // Familia tipográfica según personalización
    val familia = when (PreferenciasManager.estiloTipografia) {
        EstiloTipografia.MODERNA -> FontFamily.Default
        EstiloTipografia.SERIF -> FontFamily.Serif
        EstiloTipografia.INDUSTRIAL -> FontFamily.Monospace
    }

    val escala = PreferenciasManager.tamanoFuente.factorEscala

    val tipografiaPersonalizada = Typography(
        headlineLarge = TextStyle(fontFamily = familia, fontWeight = FontWeight.Bold, fontSize = (32 * escala).sp),
        headlineMedium = TextStyle(fontFamily = familia, fontWeight = FontWeight.Bold, fontSize = (24 * escala).sp),
        headlineSmall = TextStyle(fontFamily = familia, fontWeight = FontWeight.Bold, fontSize = (20 * escala).sp),
        titleLarge = TextStyle(fontFamily = familia, fontWeight = FontWeight.SemiBold, fontSize = (19 * escala).sp),
        titleMedium = TextStyle(fontFamily = familia, fontWeight = FontWeight.Medium, fontSize = (16 * escala).sp),
        bodyLarge = TextStyle(fontFamily = familia, fontWeight = FontWeight.Normal, fontSize = (16 * escala).sp),
        bodyMedium = TextStyle(fontFamily = familia, fontWeight = FontWeight.Normal, fontSize = (14 * escala).sp),
        bodySmall = TextStyle(fontFamily = familia, fontWeight = FontWeight.Normal, fontSize = (12 * escala).sp),
        labelLarge = TextStyle(fontFamily = familia, fontWeight = FontWeight.Medium, fontSize = (14 * escala).sp),
        labelMedium = TextStyle(fontFamily = familia, fontWeight = FontWeight.Medium, fontSize = (12 * escala).sp),
        labelSmall = TextStyle(fontFamily = familia, fontWeight = FontWeight.Normal, fontSize = (11 * escala).sp)
    )

    MaterialTheme(
        colorScheme = if (esOscuro) EsquemaOscuro else EsquemaClaro,
        typography = tipografiaPersonalizada,
        content = content
    )
}
