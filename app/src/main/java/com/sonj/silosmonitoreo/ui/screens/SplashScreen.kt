package com.sonj.silosmonitoreo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Trazos de las letras "M" y "s" (tipografia con serifa), dibujados en una cuadricula de 200 x 200
private const val TRAZO_M =
    "M104.4 63.1 L78.1 122.0H76.7L50.9 65.5V106.9Q50.9 114.7 52.8 117.2Q54.6 119.7 60.6 120.0V122.0H36.4V120.0Q42.8 119.6 44.6 117.2Q46.4 114.8 46.4 106.9V65.2Q46.4 59.5 44.7 58.0Q43.0 56.4 36.6 56.0V54.0H57.0L80.7 105.9L103.4 54.0H123.9V56.0Q118.1 56.5 116.5 58.1Q114.9 59.8 114.9 65.2V110.8Q114.9 116.2 116.6 117.9Q118.2 119.6 123.9 120.0V122.0H95.1V120.0Q101.2 119.6 102.8 117.8Q104.4 116.0 104.4 109.7Z"

private const val TRAZO_S =
    "M154.9 76.8Q155.0 76.8 156.6 75.8H157.7L158.1 89.7H156.6Q154.8 82.9 152.3 79.9Q149.7 77.0 145.3 77.0Q141.8 77.0 139.6 79.0Q137.4 81.0 137.4 84.1Q137.4 88.5 141.8 91.1L152.9 97.7Q157.5 100.4 159.5 103.3Q161.5 106.1 161.5 109.9Q161.5 115.2 157.2 119.1Q153.0 123.0 147.0 123.0Q144.5 123.0 140.1 122.1Q135.8 121.2 134.8 121.2Q133.3 121.2 132.4 122.4H131.1V106.4H132.7Q134.4 113.6 136.6 116.6Q139.9 120.8 145.8 120.8Q149.7 120.8 152.0 118.8Q154.3 116.8 154.3 113.4Q154.3 108.7 149.0 105.7L143.0 102.3Q136.4 98.6 133.7 95.2Q131.0 91.8 131.0 87.5Q131.0 81.9 134.9 78.3Q138.9 74.7 145.0 74.7Q148.4 74.7 151.3 75.8Q154.3 76.8 154.9 76.8Z"

@Composable
fun SplashScreen(onFinalizado: () -> Unit) {
    // progreso: el logo aparece, se expande y se aclara (0 = inicio, 1 = final)
    val progreso = remember { Animatable(0f) }
    // ornamento: las lineas y el rombo se abren despues del logo
    val ornamento = remember { Animatable(0f) }

    // Duracion total: 1400 + 600 + 600 = 2,6 segundos
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, tween(durationMillis = 1400, easing = FastOutSlowInEasing))
        ornamento.animateTo(1f, tween(durationMillis = 600, easing = FastOutSlowInEasing))
        delay(600)
        onFinalizado()
    }

    val trazoM = remember { PathParser().parsePathString(TRAZO_M).toPath() }
    val trazoS = remember { PathParser().parsePathString(TRAZO_S).toPath() }
    val rombo = remember {
        Path().apply {
            moveTo(100f, 137f)
            lineTo(105f, 143f)
            lineTo(100f, 149f)
            lineTo(95f, 143f)
            close()
        }
    }

    val colorTinta = MaterialTheme.colorScheme.onBackground
    val colorAcento = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(
                modifier = Modifier
                    .size(240.dp)
                    // difuminado: empieza borroso y se aclara
                    .blur((20f * (1f - progreso.value)).dp)
                    .graphicsLayer {
                        // expansion: empieza pequeno y crece hacia adelante
                        val escala = 0.6f + 0.4f * progreso.value
                        scaleX = escala
                        scaleY = escala
                        alpha = progreso.value
                    }
            ) {
                val k = size.width / 200f
                // desfase: la M empieza mas arriba y la s mas abajo, y se encuentran
                val desfase = 16f * (1f - progreso.value)

                scale(scale = k, pivot = Offset.Zero) {
                    translate(top = -desfase) {
                        drawPath(path = trazoM, color = colorTinta)
                    }
                    translate(top = desfase) {
                        drawPath(path = trazoS, color = colorAcento)
                    }

                    // ornamento: dos lineas que se abren desde el centro y un rombo que crece
                    val largo = 30f * ornamento.value
                    drawLine(
                        color = colorAcento,
                        start = Offset(92f - largo, 143f),
                        end = Offset(92f, 143f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = colorAcento,
                        start = Offset(108f, 143f),
                        end = Offset(108f + largo, 143f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                    scale(scale = ornamento.value, pivot = Offset(100f, 143f)) {
                        drawPath(path = rombo, color = colorAcento)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "MONITOREO DE SILOS",
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { alpha = ornamento.value }
            )
        }
    }
}