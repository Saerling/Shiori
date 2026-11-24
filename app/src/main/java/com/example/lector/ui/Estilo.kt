package com.example.lector.ui

import androidx.compose.ui.geometry.toRect

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.*
import androidx.compose.ui.composed
import androidx.compose.ui.unit.isSpecified
import androidx.compose.runtime.remember

import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.Spring
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/*
 * Kit de diseño inspirado en la app : pulsación con rebote, inclinación 3D, resalte suave de filas,
 * esquinas continuas, esqueletos con ola, avisos que caen desde arriba y números que ruedan.
 * Todo está escrito para Lector (Compose 1.7), sin depender de nada más que de Compose.
 */

/** Curva "enfática": arranca rápido y se asienta despacio. La usan las entradas y los cambios de pantalla. */
val EnfasisEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

// ───────────────────────── Formas ─────────────────────────

/**
 * Rectángulo con esquinas continuas (G2, como ColorOS 17 e iOS): la curva entra en el borde sin el quiebre de un
 * cuarto de círculo. Cada esquina ocupa 1,28 × el radio y se dibuja con dos cúbicas.
 */
@Immutable
data class SuaveShape(val radio: Dp, val exponente: Float = 4f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r0 = with(density) { radio.toPx() }
        val r = minOf(r0, size.minDimension / 2f)
        if (r <= 0f) return Outline.Rectangle(size.toRect())
        // Cápsulas: si el radio llega a la mitad del lado corto, una píldora normal (los extremos ya son semicírculos).
        if (r0 >= size.minDimension / 2f) return Outline.Rounded(androidx.compose.ui.geometry.RoundRect(size.toRect(), androidx.compose.ui.geometry.CornerRadius(r)))
        val e = minOf(r * 1.28f, size.minDimension / 2f)
        val w = size.width; val h = size.height
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(e, 0f); lineTo(w - e, 0f)
            cubicTo(w - e * 0.38f, 0f, w, e * 0.38f, w, e)
            lineTo(w, h - e)
            cubicTo(w, h - e * 0.38f, w - e * 0.38f, h, w - e, h)
            lineTo(e, h)
            cubicTo(e * 0.38f, h, 0f, h - e * 0.38f, 0f, h - e)
            lineTo(0f, e)
            cubicTo(0f, e * 0.38f, e * 0.38f, 0f, e, 0f)
            close()
        }
        return Outline.Generic(p)
    }
}

// ───────────────────────── Pulsación ─────────────────────────

/** Botón o tarjeta tocable: se encoge un poco bajo el dedo (sin ondas). Con [onLongClick], también mantener. */
fun Modifier.pressable(onClick: () -> Unit): Modifier = pressableCon(0.96f, null, onClick)
fun Modifier.pressable(onLongClick: (() -> Unit)?, onClick: () -> Unit): Modifier = pressableCon(0.96f, onLongClick, onClick)
fun Modifier.pressableCon(escalaPulsada: Float, onClick: () -> Unit): Modifier = pressableCon(escalaPulsada, null, onClick)

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pressableCon(escalaPulsada: Float, onLongClick: (() -> Unit)?, onClick: () -> Unit): Modifier = composed {
    val fuente = remember { MutableInteractionSource() }
    val base = this.rebote(fuente, escalaPulsada)
    if (onLongClick == null) base.clickable(interactionSource = fuente, indication = null, onClick = onClick)
    else base.combinedClickable(interactionSource = fuente, indication = null, onLongClick = onLongClick, onClick = onClick)
}

/** Fila tocable: al pulsar se ve un velo suave detrás (sin onda). */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.resaltable(radio: Dp = 0.dp, margenX: Dp = 0.dp, onLongClick: (() -> Unit)? = null, onClick: () -> Unit): Modifier = composed {
    val fuente = remember { MutableInteractionSource() }
    val pulsado by fuente.collectIsPressedAsState()
    val velo = LectorColors.Resalte
    val base = this.drawBehind { if (pulsado) drawRect(velo) }
    if (onLongClick == null) base.clickable(interactionSource = fuente, indication = null, onClick = onClick)
    else base.combinedClickable(interactionSource = fuente, indication = null, onLongClick = onLongClick, onClick = onClick)
}

/** Portadas: se encogen un poco al pulsar (no consume el toque). */
fun Modifier.press3D(inclinacionMax: Float = 0f, escalaPulsada: Float = 0.97f): Modifier = this

// ───────────────────────── Esqueletos de carga ─────────────────────────

private val ColorHueso: Color get() = LectorColors.CardHigh

/** Contenedor de esqueletos: late despacio (quieto con «quitar animaciones»). */
@Composable
fun BonesHost(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val a = if (MovimientoReducido.activo) 0.7f else {
        val t = rememberInfiniteTransition(label = "huesos")
        t.animateFloat(0.45f, 0.9f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a").value
    }
    Column(modifier.graphicsLayer { alpha = a }, content = content)
}

@Composable
fun Bone(ancho: Dp, alto: Dp, modifier: Modifier = Modifier, forma: Shape = SuaveShape(8.dp)) {
    Box(modifier.size(ancho, alto).clip(forma).background(ColorHueso))
}

/** Esqueleto de la lista de capítulos (mismas medidas que las filas reales). */
@Composable
fun BonesCapitulos(filas: Int = 7) {
    BonesHost(Modifier.fillMaxWidth()) {
        repeat(filas) {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Bone(64.dp, 64.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Bone(170.dp, 16.dp, forma = SuaveShape(8.dp))
                    Spacer(Modifier.height(8.dp))
                    Bone(110.dp, 12.dp, forma = SuaveShape(6.dp))
                }
            }
        }
    }
}

/** Esqueleto de una fila horizontal de portadas. */
@Composable
fun BonesPortadas(cantidad: Int = 3) {
    BonesHost(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 24.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            repeat(cantidad) {
                Column {
                    Bone(124.dp, 186.dp, forma = SuaveShape(12.dp))
                    Spacer(Modifier.height(8.dp))
                    Bone(90.dp, 12.dp, forma = SuaveShape(6.dp))
                }
            }
        }
    }
}

/** Esqueleto de una cuadrícula de portadas (catálogo de una fuente mientras llega la primera página). */
@Composable
fun BonesRejilla(filas: Int = 3, columnas: Int = 3) {
    BonesHost(Modifier.fillMaxWidth()) {
        repeat(filas) {
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                repeat(columnas) {
                    Column(Modifier.weight(1f)) {
                        Box(Modifier.fillMaxWidth().then(Modifier.aspectRatio(2f / 3f)).background(ColorHueso, SuaveShape(14.dp)))
                        Spacer(Modifier.height(8.dp))
                        Bone(80.dp, 12.dp, forma = SuaveShape(6.dp))
                    }
                }
            }
        }
    }
}

// ───────────────────────── Avisos (en lugar de Toast) ─────────────────────────

@Immutable
data class Aviso(val id: Long, val texto: String, val accion: String? = null, val alAccion: (() -> Unit)? = null)

/** Avisos: una píldora que cae desde arriba. Se pueden lanzar desde cualquier hilo; uno nuevo sustituye al anterior. */
object Avisos {
    private val _actual = MutableStateFlow<Aviso?>(null)
    val actual: StateFlow<Aviso?> = _actual.asStateFlow()
    private val contador = AtomicLong(0)

    fun mostrar(texto: String) {
        if (esError(texto)) Tacto.rechazar()
        _actual.value = Aviso(contador.incrementAndGet(), texto)
    }

    /** Aviso con un botón (p. ej. «Buscar en todas»): tocarlo lo ejecuta y cierra el aviso. */
    fun mostrar(texto: String, accion: String, alAccion: () -> Unit) {
        _actual.value = Aviso(contador.incrementAndGet(), texto, accion, alAccion)
    }

    fun ocultar(id: Long) { _actual.update { if (it?.id == id) null else it } }
}

/** Un aviso «de error» (lleva un punto rojo). */
private fun esError(t: String): Boolean {
    val x = t.lowercase()
    return x.startsWith("no se pudo") || x.startsWith("error") || x.contains("falló") || x.startsWith("no hay")
}

/** Avisos encima de toda la app. */
@Composable
fun AvisosHost(modifier: Modifier = Modifier) {
    val aviso by Avisos.actual.collectAsState()
    LaunchedEffect(aviso?.id) { val a = aviso ?: return@LaunchedEffect; kotlinx.coroutines.delay(if (a.accion != null) 5000 else 2600); Avisos.ocultar(a.id) }
    Box(modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp), contentAlignment = Alignment.TopCenter) {
        androidx.compose.animation.AnimatedContent(
            aviso, label = "aviso",
            transitionSpec = {
                (androidx.compose.animation.slideInVertically(tween(300, easing = EnfasisEasing)) { -it } + androidx.compose.animation.fadeIn(tween(200))) togetherWith
                    (androidx.compose.animation.slideOutVertically(tween(220)) { -it } + androidx.compose.animation.fadeOut(tween(160)))
            },
        ) { a ->
            if (a == null) { Spacer(Modifier.height(1.dp)); return@AnimatedContent }
            Row(
                Modifier.padding(horizontal = 24.dp).widthIn(max = 520.dp)
                    .shadow(12.dp, CircleShape, clip = false).clip(CircleShape).background(LectorColors.Aviso)
                    .clickable { Avisos.ocultar(a.id) }.padding(start = 16.dp, end = if (a.accion != null) 6.dp else 18.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (esError(a.texto)) { Box(Modifier.size(7.dp).clip(CircleShape).background(LectorColors.Error)); Spacer(Modifier.width(10.dp)) }
                LText(a.texto, LectorType.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), Modifier.weight(1f, fill = false), color = Color.White, maxLines = 2)
                if (a.accion != null) {
                    Spacer(Modifier.width(10.dp))
                    LText(a.accion, LectorType.Label.copy(fontWeight = FontWeight.Bold), Modifier.clip(CircleShape).background(Color.White).clickable { a.alAccion?.invoke(); Avisos.ocultar(a.id) }.padding(horizontal = 12.dp, vertical = 7.dp), color = Color.Black)
                }
            }
        }
    }
}

// ───────────────────────── Números que ruedan ─────────────────────────

/** Texto (normalmente un número) que se desliza al cambiar. */
@Composable
fun Odometro(texto: String, estilo: TextStyle, modifier: Modifier = Modifier, color: Color = LectorColors.Text) {
    androidx.compose.animation.AnimatedContent(
        texto, modifier, label = "odometro",
        transitionSpec = {
            (androidx.compose.animation.slideInVertically(tween(260)) { it / 2 } + androidx.compose.animation.fadeIn(tween(200))) togetherWith
                (androidx.compose.animation.slideOutVertically(tween(200)) { -it / 2 } + androidx.compose.animation.fadeOut(tween(150)))
        },
    ) { t -> Text(t, style = estilo.copy(fontWeight = estilo.fontWeight ?: FontWeight.Bold), color = color, maxLines = 1) }
}
