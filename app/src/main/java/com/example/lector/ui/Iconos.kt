package com.example.lector.ui

import com.example.lector.fx.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Iconos que NO están en el set "core" de Material (el proyecto no usa material-icons-extended).
 * Se dibujan a partir de los mismos trazados SVG de Material Icons.
 */
object Iconos {
    private fun vector(nombre: String, trazado: String): ImageVector =
        ImageVector.Builder(
            name = nombre,
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).addPath(addPathNodes(trazado), fill = SolidColor(Color.Black)).build()

    /** Flecha hacia abajo (descargar). */
    val Descargar: ImageVector by lazy {
        vector("Descargar", "M20 12l-1.41-1.41L13 16.17V4h-2v12.17l-5.58-5.59L4 12l8 8 8-8z")
    }

    /** Globo terráqueo (web). */
    val Globo: ImageVector by lazy {
        vector("Globo", "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm6.93 6h-2.95c-.32-1.25-.78-2.45-1.38-3.56 1.84.63 3.37 1.91 4.33 3.56zM12 4.04c.83 1.2 1.48 2.53 1.91 3.96h-3.82c.43-1.43 1.08-2.76 1.91-3.96zM4.26 14C4.1 13.36 4 12.69 4 12s.1-1.36.26-2h3.38c-.08.66-.14 1.32-.14 2 0 .68.06 1.34.14 2H4.26zm.82 2h2.95c.32 1.25.78 2.45 1.38 3.56-1.84-.63-3.37-1.9-4.33-3.56zm2.95-8H5.08c.96-1.66 2.49-2.93 4.33-3.56C8.81 5.55 8.35 6.75 8.03 8zM12 19.96c-.83-1.2-1.48-2.53-1.91-3.96h3.82c-.43 1.43-1.08 2.76-1.91 3.96zM14.34 14H9.66c-.09-.66-.16-1.32-.16-2 0-.68.07-1.35.16-2h4.68c.09.65.16 1.32.16 2 0 .68-.07 1.34-.16 2zm.25 5.56c.6-1.11 1.06-2.31 1.38-3.56h2.95c-.96 1.65-2.49 2.93-4.33 3.56zM16.36 14c.08-.66.14-1.32.14-2 0-.68-.06-1.34-.14-2h3.38c.16.64.26 1.31.26 2s-.1 1.36-.26 2h-3.38z")
    }

    /** Cuadrado (detener / cancelar). */
    val Detener: ImageVector by lazy { vector("Detener", "M7 7h10v10H7z") }

    /** Ojo abierto (categoría visible). */
    val Ojo: ImageVector by lazy {
        vector(
            "Ojo",
            "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z",
        )
    }

    /** Ojo tachado (categoría oculta). */
    val OjoTachado: ImageVector by lazy {
        vector(
            "OjoTachado",
            "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z",
        )
    }

    /** Marcador relleno (capítulo marcado). */
    val Marca: ImageVector by lazy {
        vector("Marca", "M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z")
    }

    /** Marcador vacío (quitar la marca). */
    val MarcaBorde: ImageVector by lazy {
        vector("MarcaBorde", "M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2zm0 15l-5-2.18L7 18V5h10v13z")
    }

    /** Dos rayas (asa para arrastrar y reordenar). */
    val Arrastrar: ImageVector by lazy {
        vector("Arrastrar", "M20 9H4v2h16V9zM4 15h16v-2H4v2z")
    }
}

/** Estado de la descarga de un capítulo. */
sealed interface EstadoDescarga {
    /** Todavía no descargado. */
    object Libre : EstadoDescarga

    /** Descargando; [fraccion] va de 0f a 1f (0f = preparando). */
    data class Descargando(val fraccion: Float) : EstadoDescarga

    /** Ya está en la biblioteca como .cbz. */
    object Listo : EstadoDescarga
}

/**
 * Botón de descarga estilo Komikku:
 *  - Libre: círculo con flecha hacia abajo.
 *  - Descargando: anillo de progreso con un cuadradito en el centro. Al volver a tocarlo se CANCELA.
 *  - Listo: círculo relleno con ✓.
 */
@Composable
fun BotonDescarga(
    estado: EstadoDescarga,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val acento = MaterialTheme.colorScheme.primary
    val apagado = LectorColors.Muted
    val fraccion by animateFloatAsState(
        (estado as? EstadoDescarga.Descargando)?.fraccion ?: 0f,
        tween(250), label = "fraccionDescarga",
    )
    // Mientras todavía no hay progreso (preparando), el arco gira.
    val giro by rememberInfiniteTransition(label = "giroDescarga").animateFloat(
        0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "giro",
    )

    if (fxOn("download")) { BotonDescargaFx(estado, onClick, modifier); return }
    Box(modifier.size(44.dp).pressable(onClick), Alignment.Center) {
        when (estado) {
            is EstadoDescarga.Libre -> {
                Canvas(Modifier.size(30.dp)) {
                    drawCircle(apagado, style = Stroke(width = 2.dp.toPx()))
                }
                Icon(Iconos.Descargar, "Descargar capítulo", tint = apagado, modifier = Modifier.size(18.dp))
            }
            is EstadoDescarga.Descargando -> {
                Canvas(Modifier.size(30.dp)) {
                    val trazo = 2.5.dp.toPx()
                    val margen = trazo / 2
                    val tam = Size(size.width - trazo, size.height - trazo)
                    drawCircle(acento.copy(alpha = 0.25f), style = Stroke(width = trazo))
                    if (estado.fraccion <= 0f) {
                        drawArc(
                            acento, startAngle = giro - 90f, sweepAngle = 90f, useCenter = false,
                            topLeft = Offset(margen, margen), size = tam,
                            style = Stroke(width = trazo, cap = StrokeCap.Round),
                        )
                    } else {
                        drawArc(
                            acento, startAngle = -90f, sweepAngle = 360f * fraccion, useCenter = false,
                            topLeft = Offset(margen, margen), size = tam,
                            style = Stroke(width = trazo, cap = StrokeCap.Round),
                        )
                    }
                }
                Icon(Iconos.Detener, "Cancelar descarga", tint = acento, modifier = Modifier.size(22.dp))
            }
            is EstadoDescarga.Listo -> {
                Box(Modifier.size(30.dp).clip(CircleShape).background(acento), Alignment.Center) {
                    Icon(
                        Icons.Rounded.Check, "Descargado",
                        tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
