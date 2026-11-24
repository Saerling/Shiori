// Copiado de Mochy Music (github.com/realidkroo/Mochy-Music, GPL-3.0): ui/screens/playlist (fila de canción) y
// ui/component/SongSwipeBox.kt. Shiori: la fila de canción es la de capítulo y las acciones de deslizar, las de capítulo.

package com.example.lector.ui.mochy

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.lector.ui.AccionGesto
import com.example.lector.ui.EstadoGesto
import com.example.lector.ui.GestosCapitulos
import com.example.lector.ui.Iconos
import com.example.lector.ui.LectorColors
import com.example.lector.ui.LectorFont
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon

private val TrackTitleStyle get() = TextStyle(fontFamily = LectorFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 17.sp, letterSpacing = (-0.03).em)
private val TrackSubtitleStyle get() = TextStyle(fontFamily = LectorFont, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, lineHeight = 14.sp, letterSpacing = (-0.02).em)
private val TrackShape = RoundedCornerShape(9.dp)
private val BoneColor = Color(0xFFD9D9D9).copy(alpha = 0.22f)
private val TrackInset = 36.dp

/** Resalte redondeado que sale al pulsar y se apaga despacio (RoundedHighlight de Mochy). */
private class ResalteFila(private val radio: Dp, private val margenX: Dp, private val color: Color) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = Nodo(interactionSource, radio, margenX, color)
    override fun equals(other: Any?) = other is ResalteFila && other.radio == radio && other.margenX == margenX && other.color == color
    override fun hashCode() = (radio.hashCode() * 31 + margenX.hashCode()) * 31 + color.hashCode()
    private class Nodo(val fuente: InteractionSource, val radio: Dp, val margenX: Dp, val color: Color) : Modifier.Node(), DrawModifierNode {
        private val fuerza = Animatable(0f)
        override fun onAttach() {
            coroutineScope.launch {
                val pulsos = ArrayList<PressInteraction.Press>()
                fuente.interactions.collect { i ->
                    when (i) {
                        is PressInteraction.Press -> pulsos.add(i)
                        is PressInteraction.Release -> pulsos.remove(i.press)
                        is PressInteraction.Cancel -> pulsos.remove(i.press)
                        else -> return@collect
                    }
                    if (pulsos.isNotEmpty()) launch { fuerza.animateTo(1f, tween(90)) }
                    else launch { if (fuerza.value < 1f) fuerza.animateTo(1f, tween(70)); fuerza.animateTo(0f, tween(340)) }
                }
            }
        }
        override fun ContentDrawScope.draw() {
            val s = fuerza.value
            if (s > 0f) {
                val dx = margenX.toPx()
                drawRoundRect(color.copy(alpha = color.alpha * s), Offset(-dx, 0f), Size(size.width + dx * 2f, size.height), CornerRadius(radio.toPx()))
            }
            drawContent()
        }
    }
}

private fun Modifier.bandaElegida(color: Color, progreso: () -> Float): Modifier = drawBehind {
    val p = progreso()
    if (p <= 0f) return@drawBehind
    val inset = 14.dp.toPx()
    drawRoundRect(color.copy(alpha = 0.08f * p.coerceAtMost(1f)), Offset(inset, 0f), Size(size.width - inset * 2f, size.height), CornerRadius(14.dp.toPx()))
}

/**
 * Fila de capítulo de Mochy: punto de acento = sin leer, cuadrado de 39 dp con el número, dos líneas y, a la derecha,
 * la descarga o el visto de selección. Leído = texto apagado.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilaCapituloMochy(
    badge: String, title: String, subtitle: String, read: Boolean, accent: Color,
    onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null, leading: (@Composable () -> Unit)? = null,
    progressLabel: String? = null, selected: Boolean = false,
) {
    val band by animateFloatAsState(if (selected) 1f else 0f, tween(180), label = "selectedBand")
    val ink = LectorColors.Text
    val resalte = remember(ink) { ResalteFila(100.dp, (-10).dp, ink.copy(alpha = 0.12f)) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().bandaElegida(ink) { band }
            .combinedClickable(interactionSource = null, indication = resalte, onClick = onClick, onLongClick = onLongClick)
            .padding(start = TrackInset - 17.dp, end = 18.dp, top = 9.dp, bottom = 9.dp),
    ) {
        Box(Modifier.width(17.dp), contentAlignment = Alignment.CenterStart) {
            if (!read) Box(Modifier.padding(start = 3.dp).size(7.dp).clip(CircleShape).background(accent))
        }
        Box(Modifier.size(39.dp).clip(TrackShape).background(BoneColor), contentAlignment = Alignment.Center) {
            Text(badge, style = TrackSubtitleStyle.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                color = ink.copy(alpha = if (read) 0.45f else 0.85f), maxLines = 1, overflow = TextOverflow.Clip)
        }
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leading != null) { leading(); Spacer(Modifier.width(5.dp)) }
                Text(title, style = TrackTitleStyle, color = ink.copy(alpha = if (read && !selected) 0.45f else 0.94f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (progressLabel != null) {
                    Text(progressLabel, style = TrackSubtitleStyle.copy(fontWeight = FontWeight.Bold), color = accent, maxLines = 1)
                    if (subtitle.isNotEmpty()) Spacer(Modifier.width(6.dp))
                }
                Text(subtitle, style = TrackSubtitleStyle, color = ink.copy(alpha = if (read) 0.4f else 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke()
    }
}

/** Color de Mochy de cada acción de deslizar. */
private val AccionGesto.colorMochy: Color get() = when (this) {
    AccionGesto.LEIDO -> SwipeQueueColor
    AccionGesto.MARCA -> SwipeSaveColor
    AccionGesto.DESCARGA -> SwipeDownloadColor
    AccionGesto.ANTERIORES -> SwipePlayNextColor
    AccionGesto.NADA -> Color(0xFF8E8E93)
}

private fun pastilla(a: AccionGesto, e: EstadoGesto, alHacer: () -> Unit): SwipeAction = when (a) {
    AccionGesto.LEIDO -> if (e.leido) SwipeAction(Iconos.OjoTachado, a.colorMochy, "Marcar no leído", alHacer) else SwipeAction(a.icono, a.colorMochy, "Marcar leído", alHacer)
    AccionGesto.MARCA -> if (e.marcado) SwipeAction(Iconos.MarcaBorde, a.colorMochy, "Desmarcar", alHacer) else SwipeAction(a.icono, a.colorMochy, "Marcar", alHacer)
    AccionGesto.DESCARGA -> when {
        e.descargando -> SwipeAction(Icons.Rounded.Close, SwipeRemoveColor, "Cancelar descarga", alHacer)
        e.descargado -> SwipeAction(Icons.Rounded.Delete, SwipeRemoveColor, "Borrar descarga", alHacer)
        else -> SwipeAction(a.icono, a.colorMochy, "Descargar", alHacer)
    }
    AccionGesto.ANTERIORES -> SwipeAction(a.icono, a.colorMochy, "Anteriores leídos", alHacer)
    AccionGesto.NADA -> SwipeAction(a.icono, a.colorMochy, "Nada", alHacer)
}

/** Deslizar de Mochy (SongSwipeBox): la fila se mueve sobre una losa y sale la pastilla de la acción de ese lado. */
@Composable
fun DeslizarCapituloMochy(
    estado: EstadoGesto, onAction: (AccionGesto) -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, slab: Boolean = true, content: @Composable () -> Unit,
) {
    GestosCapitulos.iniciar(LocalContext.current)
    val der = GestosCapitulos.derecha
    val izq = GestosCapitulos.izquierda
    SwipeActionsBox(
        modifier = modifier, enabled = enabled, slab = slab,
        start = if (der == AccionGesto.NADA) null else { { pastilla(der, estado) { onAction(der) } } },
        end = if (izq == AccionGesto.NADA) null else { { pastilla(izq, estado) { onAction(izq) } } },
        content = content,
    )
}

private val PlaylistTracking = (-0.06).em
private val SectionTitleStyle get() = TextStyle(fontFamily = LectorFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = PlaylistTracking)
private val PlaylistStatsStyle get() = TextStyle(fontFamily = LectorFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = (-0.02).em)
private val ReceiptSmallStyle = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 8.5.sp, letterSpacing = 0.04.em)
private val ReceiptCenterStyle = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.42.em)

@Composable
fun TituloCapitulosMochy(
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val ink = LectorColors.Text
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = TrackInset - 4.dp, end = TrackInset - 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Text(text = title, style = SectionTitleStyle, color = ink, maxLines = 1)
            if (showChevron) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ink.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            trailing()
        }
    }
}
@Composable
fun OrdenCapitulosMochy(
    label: String,
    descending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ink = LectorColors.Text.copy(alpha = 0.72f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Text(
            text = label.lowercase(),
            style = PlaylistStatsStyle.copy(fontSize = 12.sp),
            color = ink,
            maxLines = 1,
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = if (descending) "Descendente" else "Ascendente",
            tint = ink,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { rotationZ = if (!descending) 180f else 0f }
        )
    }
}
@Composable
fun PieCapitulosMochy(
    start: String,
    end: String,
    modifier: Modifier = Modifier,
    center: String = "FIN",
) {
    val ink = LectorColors.Text
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TrackInset)
            .padding(top = 18.dp, bottom = 6.dp)
    ) {
        ReceiptRule()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
        ) {
            Text(
                text = start,
                style = ReceiptSmallStyle,
                color = ink.copy(alpha = 0.6f),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = center,
                style = ReceiptCenterStyle,
                color = ink.copy(alpha = 0.78f),
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            Text(
                text = end,
                style = ReceiptSmallStyle,
                color = ink.copy(alpha = 0.6f),
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        ReceiptRule()
    }
}

@Composable
private fun ReceiptRule() {
    val ink = LectorColors.Text
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = ink.copy(alpha = 0.24f),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 0.75.dp.toPx(),
                )
            }
    )
}
