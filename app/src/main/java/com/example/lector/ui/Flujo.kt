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

/** Superficie flotante (barra, mini lector): tinte casi opaco según «Intensidad del vidrio», línea finísima y sombra suave. */
fun Modifier.vidrioFlotante(forma: Shape, elevacion: Dp = 16.dp): Modifier = composed {
    val a = 0.97f - 0.27f * Aspecto.vidrio
    val linea = if (TemaActual.oscuro) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.08f)
    (if (elevacion > 0.dp) this.shadow(elevacion, forma, clip = false, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.25f)) else this)
        .clip(forma)
        .background(LectorColors.Cristal.copy(alpha = a))
        .then(if (Aspecto.luminoso) Modifier.bordeLuminoso(forma) else Modifier.border(0.5.dp, linea, forma))
}

/**
 * Esfera / cápsula minimalista (como Mochy): relleno plano, sin degradado ni sombra. Solo con «Efecto luminoso»
 * activado lleva además un borde de luz muy fino.
 */
fun Modifier.esfera(forma: Shape = CircleShape, relleno: Color, sombra: Boolean = true): Modifier = composed {
    this.clip(forma).background(relleno).then(if (Aspecto.luminoso) Modifier.bordeLuminoso(forma, 0.6f) else Modifier)
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

/** Borde solo si «Efecto luminoso» está activado; si no, nada (superficies planas como Mochy). */
fun Modifier.bordeSuave(forma: Shape): Modifier = composed { if (Aspecto.luminoso) this.bordeLuminoso(forma, 0.5f) else this }
