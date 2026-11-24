package com.example.lector.ui

import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lector.Almacen
import com.example.lector.Comic
import com.example.lector.rememberPortada
import com.example.lector.ext.ExtManager
import com.example.lector.ext.Motor
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LText(
    text: String, style: TextStyle, modifier: Modifier = Modifier,
    color: Color = LectorColors.Text, maxLines: Int = Int.MAX_VALUE
) = Text(text, modifier, color, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)

/** Portada real (la que ya guarda tu Almacen); si no hay, muestra la inicial del título. */
@Composable
fun ComicCover(comic: Comic, almacen: Almacen, modifier: Modifier = Modifier, shape: Shape = FormaPortada) {
    val portada = rememberPortada(almacen, comic.id)
    if (portada != null) androidx.compose.runtime.SideEffect { Aspecto.anotarPortada("c:" + comic.id, portada.width, portada.height) }
    Box(modifier.clip(shape).background(LectorColors.CardHigh), contentAlignment = Alignment.Center) {
        when {
            portada != null ->
                Image(bitmap = portada, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            // Manga en línea sin portada guardada (añadido con pulsación larga, copia restaurada…): la de la fuente.
            comic.enLinea && !comic.portadaUrl.isNullOrBlank() ->
                ImagenRemota(comic.portadaUrl, Modifier.fillMaxSize(), rememberCabeceras(comic.fuenteId), fuenteId = comic.fuenteId, efectosPortada = false)
            else -> LText(comic.titulo.take(1).uppercase(), LectorType.Display, color = LectorColors.Muted)
        }
    }
}

/** Cabeceras HTTP de una fuente (Referer…) para cargar sus imágenes; se piden en segundo plano. */
@Composable
fun rememberCabeceras(fuenteId: Long): Map<String, String> {
    val ctx = LocalContext.current
    val ext = remember { ExtManager.obtener(ctx) }
    val f = ext.fuentes.firstOrNull { it.id == fuenteId }
    val inicial = remember(fuenteId, f) { f?.let { Motor.actual?.cabeceras(it) }.orEmpty() }
    val cab by produceState(inicial, fuenteId, f) {
        if (f != null) value = Motor.actual?.cabecerasCargando(f).orEmpty()
    }
    return cab
}

/** Progreso fino (3 dp): pista gris y avance del color del texto. */
/** Progreso fino y luminoso. */
@Composable
fun ProgressLine(progress: Float, modifier: Modifier = Modifier) = BarraLuminosa(progress, modifier, alto = 3.dp)

@Composable
fun PillButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) =
    BotonExpresivo(text, icon, onClick, modifier, EstiloBoton.TONAL, alto = 44.dp)

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(modifier.padding(horizontal = MargenLateral), verticalAlignment = Alignment.CenterVertically) {
        LText(title, EstiloSeccion)
        Icon(Icons.Rounded.KeyboardArrowRight, null, tint = LectorColors.Muted, modifier = Modifier.size(18.dp))
    }
}

/** Fila horizontal de portadas (Inicio). */
@Composable
fun CoverCarousel(items: List<Comic>, almacen: Almacen, onClick: (Comic) -> Unit, modifier: Modifier = Modifier) {
    FilaTarjetas("portadas", items, key = { it.id }, modifier = modifier) { c ->
        TarjetaPortada(c, almacen, Modifier.pressable { onClick(c) })
    }
}

enum class LectorTab(val label: String, val icon: ImageVector, val iconoApagado: ImageVector) {
    Home("Inicio", Icons.Rounded.Home, Icons.Outlined.Home),
    Search("Explorar", Icons.Rounded.Search, Icons.Outlined.Search),
    Library("Biblioteca", Icons.Rounded.List, Icons.Outlined.List),
    Updates("Actualizaciones", Icons.Rounded.Notifications, Icons.Outlined.Notifications),
    History("Historial", Icons.Rounded.DateRange, Icons.Outlined.DateRange),
    More("Más", Icons.Rounded.MoreVert, Icons.Outlined.MoreVert)
}

@Composable
fun FloatingNavBar(selected: LectorTab, onSelect: (LectorTab) -> Unit, modifier: Modifier = Modifier) =
    BarraSilencio(selected, onSelect, modifier)

/** Se aplasta un poco bajo el dedo y vuelve con un rebote corto. */
@Composable
fun Modifier.rebote(fuente: MutableInteractionSource, escala: Float = 0.92f): Modifier {
    val pulsado by fuente.collectIsPressedAsState()
    val e by androidx.compose.animation.core.animateFloatAsState(if (pulsado) escala else 1f, spring(dampingRatio = 0.6f, stiffness = 900f), label = "rebote")
    return graphicsLayer { scaleX = e; scaleY = e }
}

/** «Seguir leyendo»: el último cómic que abriste. */
@Composable
fun MiniReader(comic: Comic, almacen: Almacen, onClick: () -> Unit, modifier: Modifier = Modifier) =
    MiniReaderGenerico(
        comic.titulo, comic.textoPagina(), onClick, modifier,
        progreso = if (comic.paginas > 0) ((comic.ultimaPagina + if (comic.enLinea) 0 else 1).toFloat() / comic.paginas).coerceIn(0f, 1f) else null,
    ) { m -> ComicCover(comic, almacen, m, SuaveShape(10.dp)) }

@Composable
fun MiniReaderGenerico(
    titulo: String, subtitulo: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    progreso: Float? = null,
    portada: @Composable (Modifier) -> Unit,
) = MiniSilencio(titulo, subtitulo, onClick, modifier, progreso, portada)
