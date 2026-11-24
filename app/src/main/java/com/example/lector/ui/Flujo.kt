package com.example.lector.ui

// «Flujo»: la capa táctil y luminosa de Silencio. Idea de ColorOS 17 (Living Flow: efecto luminoso, movimiento fluido,
// respuesta flexible) con el vidrio de iOS 27 (intensidad ajustable) y las cápsulas de MagicOS 11. Todo a mano:
// sin desenfoques costosos; el vidrio es translucidez + borde de luz + sombra suave.

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Borde de luz: más claro arriba que abajo, como el canto de un cristal iluminado desde arriba. */
fun Modifier.bordeLuminoso(forma: Shape, fuerza: Float = 1f): Modifier = composed {
    val o = TemaActual.oscuro
    val b = if (!Aspecto.luminoso) Brush.verticalGradient(listOf(LectorColors.Linea, LectorColors.Linea))
    else if (o) Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.24f * fuerza), Color.White.copy(alpha = 0.05f * fuerza)))
    else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f * fuerza), Color.Black.copy(alpha = 0.07f * fuerza)))
    this.border(0.8.dp, b, forma)
}

/** Superficie flotante de vidrio: sombra suave, tinte translúcido según «Intensidad del vidrio» y borde de luz. */
fun Modifier.vidrioFlotante(forma: Shape, elevacion: Dp = 16.dp): Modifier = composed {
    val a = 0.97f - 0.27f * Aspecto.vidrio
    this.shadow(elevacion, forma, clip = false, ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.35f))
        .clip(forma)
        .background(LectorColors.Cristal.copy(alpha = a))
        .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = if (TemaActual.oscuro) 0.05f else 0.25f), Color.Transparent)))
        .bordeLuminoso(forma)
}

/**
 * Esfera: un círculo (o cápsula) con volumen. Relleno con un degradado muy suave (más claro arriba), borde de luz y una
 * sombra corta. [activo] la vuelve sólida del color del texto.
 */
fun Modifier.esfera(forma: Shape = CircleShape, relleno: Color, sombra: Boolean = true): Modifier = composed {
    val luz = if (TemaActual.oscuro) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.55f)
    (if (sombra) this.shadow(4.dp, forma, clip = false, ambientColor = Color.Black.copy(alpha = 0.18f), spotColor = Color.Black.copy(alpha = 0.22f)) else this)
        .clip(forma)
        .background(relleno)
        .background(Brush.verticalGradient(listOf(luz, Color.Transparent)))
        .bordeLuminoso(forma, 0.8f)
}

/** Respuesta flexible: al pulsar se encoge con un muelle y se ilumina un poco. */
fun Modifier.respuestaFlexible(fuente: InteractionSource, escala: Float = 0.9f): Modifier = composed {
    val pulsado by fuente.collectIsPressedAsState()
    val e by animateFloatAsState(if (pulsado) escala else 1f, spring(dampingRatio = 0.55f, stiffness = 700f), label = "flex")
    val luz by animateFloatAsState(if (pulsado) 1f else 0f, spring(stiffness = 900f), label = "flexLuz")
    val velo = LectorColors.Text
    this.graphicsLayer { scaleX = e; scaleY = e }.drawBehind { if (luz > 0f) drawRect(velo.copy(alpha = 0.08f * luz)) }
}

/**
 * Barra de progreso luminosa (como la del reproductor de ColorOS 17): pista fina y avance del acento con un halo
 * suave en la punta.
 */
@Composable
fun BarraLuminosa(progreso: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, alto: Dp = 4.dp) {
    val p = progreso.coerceIn(0f, 1f)
    val pista = LectorColors.PillFill
    val brillo = Aspecto.luminoso
    Box(modifier.fillMaxWidth().height(alto + 8.dp).drawBehind {
        val y = size.height / 2f
        val h = alto.toPx()
        drawRoundRect(pista, Offset(0f, y - h / 2), androidx.compose.ui.geometry.Size(size.width, h), androidx.compose.ui.geometry.CornerRadius(h / 2))
        if (p > 0f) {
            val x = size.width * p
            drawRoundRect(color, Offset(0f, y - h / 2), androidx.compose.ui.geometry.Size(x, h), androidx.compose.ui.geometry.CornerRadius(h / 2))
            if (brillo) drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), Offset(x, y), h * 3.2f), h * 3.2f, Offset(x, y))
        }
    })
}
