package com.example.lector.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * «Silencio»: la identidad propia de Shiori 3.
 * Vidrio solo en lo que flota sobre el contenido (barra, mini lector, controles), monocromo,
 * y el color lo ponen las portadas.
 */

/** Borde de vidrio: una línea finísima más clara arriba que abajo. */
private val BordeVidrio = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.06f)))

/** Superficie de vidrio flotante con sombra suave. */
@Composable
fun Modifier.vidrio(forma: Shape): Modifier = this
    .shadow(16.dp, forma, clip = false, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.35f))
    .clip(forma)
    .border(1.dp, BordeVidrio, forma)

/**
 * Vidrio que copia lo de detrás. SOLO para lo que flota fuera del contenido capturado (barra y mini lector):
 * dentro de una pantalla se copiaría a sí mismo y el dibujo entraría en un bucle infinito.
 */
@Composable
private fun FondoVidrioFlotante(modifier: Modifier = Modifier) =
    FondoCristal(CristalApp, modifier, tinte = LectorColors.Cristal.copy(alpha = if (hayDesenfoque) 0.55f else 0.94f))

/** Vidrio para botones y barras dentro de las pantallas: translúcido, sin copiar el fondo. */
@Composable
private fun FondoVidrio(modifier: Modifier = Modifier) =
    Box(modifier.background(if (TemaActual.oscuro) Color(0xFF2C2C2E).copy(alpha = 0.62f) else Color.White.copy(alpha = 0.7f)))

/**
 * Barra de pestañas flotante: una píldora con Inicio, Biblioteca, Novedades, Historial y Más,
 * y la búsqueda (Explorar) aparte, en un círculo a la derecha, como en Apple Music.
 */
@Composable
fun BarraSilencio(selected: LectorTab, onSelect: (LectorTab) -> Unit, modifier: Modifier = Modifier) {
    val pestanas = remember { LectorTab.values().filter { it != LectorTab.Search } }
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val forma = RoundedCornerShape(32.dp)
        Box(Modifier.weight(1f).height(64.dp).vidrio(forma)) {
            FondoVidrioFlotante(Modifier.matchParentSize())
            Row(Modifier.fillMaxSize().padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                pestanas.forEach { t -> PestanaSilencio(t, t == selected, Modifier.weight(1f)) { onSelect(t) } }
            }
        }
        Spacer(Modifier.width(10.dp))
        val activa = selected == LectorTab.Search
        Box(
            Modifier.size(64.dp).vidrio(CircleShape)
                .clickable(role = Role.Tab, onClickLabel = "Explorar y buscar") { onSelect(LectorTab.Search) },
            contentAlignment = Alignment.Center,
        ) {
            FondoVidrioFlotante(Modifier.matchParentSize())
            if (activa) Box(Modifier.matchParentSize().padding(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)))
            Icon(Icons.Rounded.Search, "Explorar", tint = if (activa) LectorColors.Text else LectorColors.Text.copy(alpha = 0.8f), modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun PestanaSilencio(t: LectorTab, activa: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val fondo by animateColorAsState(
        if (activa) LectorColors.Text.copy(alpha = 0.14f) else Color.Transparent,
        spring(stiffness = Spring.StiffnessMediumLow), label = "pestana",
    )
    val tinta by animateColorAsState(
        if (activa) LectorColors.Text else LectorColors.Text.copy(alpha = 0.58f),
        spring(stiffness = Spring.StiffnessMediumLow), label = "tinta",
    )
    Column(
        modifier.fillMaxHeight().clip(RoundedCornerShape(28.dp)).background(fondo)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(if (activa) t.icon else t.iconoApagado, null, tint = tinta, modifier = Modifier.size(22.dp))
        LText(if (t == LectorTab.Updates) "Novedades" else t.label, LectorType.Label.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp), color = tinta, maxLines = 1)
    }
}

/** Mini lector: píldora de vidrio con la portada, el capítulo y un botón para seguir. */
@Composable
fun MiniSilencio(
    titulo: String, subtitulo: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    progreso: Float? = null,
    portada: @Composable (Modifier) -> Unit,
) {
    val forma = RoundedCornerShape(30.dp)
    Box(modifier.fillMaxWidth().height(60.dp).vidrio(forma).clickable(onClickLabel = "Seguir leyendo", onClick = onClick)) {
        FondoVidrioFlotante(Modifier.matchParentSize())
        Row(Modifier.fillMaxSize().padding(start = 9.dp, end = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            portada(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LText(titulo, LectorType.Body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), maxLines = 1)
                LText(subtitulo, LectorType.Label, color = LectorColors.Muted, maxLines = 1)
            }
            Box(Modifier.size(42.dp).clip(CircleShape).background(LectorColors.Text).clickable(onClickLabel = "Seguir leyendo", onClick = onClick), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, "Seguir leyendo", tint = LectorColors.Bg, modifier = Modifier.size(24.dp))
            }
        }
        if (progreso != null) {
            Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = 30.dp).fillMaxWidth().height(2.dp).background(LectorColors.Text.copy(alpha = 0.12f))) {
                Box(Modifier.fillMaxWidth(progreso.coerceIn(0f, 1f)).fillMaxHeight().background(LectorColors.Text.copy(alpha = 0.8f)))
            }
        }
    }
}

// ───────────── Piezas de página (Silencio) ─────────────

/** Botón redondo de vidrio (atrás, menús) que flota sobre el contenido. */
@Composable
fun BotonVidrio(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, modifier: Modifier = Modifier, tam: androidx.compose.ui.unit.Dp = 40.dp, onClick: () -> Unit) {
    Box(
        modifier.size(tam).vidrio(CircleShape).clickable(onClickLabel = descripcion) { Tacto.tic(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        FondoVidrio(Modifier.matchParentSize())
        Icon(icono, descripcion, tint = LectorColors.Text, modifier = Modifier.size(22.dp))
    }
}

/**
 * Página con título grande (34 sp) que, al desplazar, pasa a una barra de vidrio arriba con el título pequeño
 * centrado, como en iOS.
 */
@Composable
fun PaginaSilencio(
    titulo: String, onBack: (() -> Unit)?, acciones: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = androidx.compose.foundation.rememberScrollState()
    val densidad = androidx.compose.ui.platform.LocalDensity.current
    val plegado by androidx.compose.runtime.remember { androidx.compose.runtime.derivedStateOf { scroll.value > with(densidad) { 64.dp.toPx() } } }
    val alfa by androidx.compose.animation.core.animateFloatAsState(if (plegado) 1f else 0f, androidx.compose.animation.core.tween(180), label = "barra")
    Box(Modifier.fillMaxSize().background(LectorColors.Bg)) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll).statusBarsPadding().padding(bottom = 120.dp).anchoLectura()) {
            Spacer(Modifier.height(64.dp))
            LText(titulo, LectorType.Display.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 22.dp), maxLines = 2)
            Spacer(Modifier.height(6.dp))
            content()
        }
        Box(Modifier.fillMaxWidth().graphicsLayerAlpha(alfa)) {
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) { FondoVidrio(Modifier.matchParentSize()) }
            Box(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp), contentAlignment = Alignment.Center) {
                LText(titulo, LectorType.Body.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold), maxLines = 1, modifier = Modifier.padding(horizontal = 70.dp))
            }
        }
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) BotonVidrio(Icons.Rounded.ArrowBack, "Atrás", onClick = onBack)
            Spacer(Modifier.weight(1f))
            acciones()
        }
    }
}

private fun Modifier.graphicsLayerAlpha(a: Float) = this.then(Modifier.graphicsLayer { alpha = a })

/** Fila de ajustes agrupada: superficie del sistema, icono en un cuadrito redondeado, título, detalle y chevron. */
@Composable
fun FilaSilencio(
    icon: androidx.compose.ui.graphics.vector.ImageVector?, titulo: String, detalle: String?,
    onClick: (() -> Unit)?, final: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 1.dp).clip(RoundedCornerShape(16.dp)).background(LectorColors.Card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 54.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(LectorColors.Text.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = LectorColors.Text, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            LText(titulo, LectorType.Body.copy(fontSize = 16.sp))
            if (!detalle.isNullOrBlank()) LText(detalle, LectorType.Label.copy(fontSize = 13.sp), color = LectorColors.Muted, maxLines = 3)
        }
        if (final != null) { Spacer(Modifier.width(12.dp)); final() }
        else if (onClick != null) { Spacer(Modifier.width(8.dp)); LText("›", LectorType.Body.copy(fontSize = 20.sp), color = LectorColors.Muted) }
    }
}

/** Interruptor al estilo iOS: verde encendido, gris apagado, perilla blanca que se desliza con muelle. */
@Composable
fun InterruptorSilencio(activado: Boolean, onChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    val x by androidx.compose.animation.core.animateDpAsState(if (activado) 22.dp else 2.dp, spring(dampingRatio = 0.8f, stiffness = 700f), label = "perilla")
    val fondo by animateColorAsState(if (activado) Color(0xFF30D158) else LectorColors.Text.copy(alpha = 0.16f), label = "fondo")
    Box(
        modifier.size(51.dp, 31.dp).clip(CircleShape).background(fondo)
            .then(if (onChange != null) Modifier.clickable(role = Role.Switch) { onChange(!activado) } else Modifier),
    ) {
        Box(Modifier.padding(start = x, top = 2.dp).size(27.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Color.White))
    }
}

/** Tarjeta grande de «Seguir leyendo» (como las de Inicio en Apple Music): portada a sangre y franja de vidrio abajo. */
@Composable
fun TarjetaSeguir(
    comic: com.example.lector.Comic, almacen: com.example.lector.Almacen, etiqueta: String,
    onOpen: () -> Unit, onRead: () -> Unit, onLong: () -> Unit,
) {
    val forma = RoundedCornerShape(24.dp)
    Box(
        Modifier.width(260.dp).height(360.dp).shadow(12.dp, forma, clip = false).clip(forma)
            .combinedClickableSilencio(onOpen, onLong),
    ) {
        ComicCover(comic, almacen, Modifier.fillMaxSize(), RoundedCornerShape(0.dp))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f))))
        Row(
            Modifier.align(Alignment.BottomCenter).padding(8.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.35f)).padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                LText(comic.titulo, LectorType.Body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = Color.White, maxLines = 1)
                LText(etiqueta + " · " + comic.textoPagina(), LectorType.Label, color = Color.White.copy(alpha = 0.75f), maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f))) {
                    Box(Modifier.fillMaxWidth(comic.progresoLectura().coerceIn(0f, 1f)).fillMaxHeight().background(Color.White))
                }
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(38.dp).clip(CircleShape).background(Color.White).clickable(onClickLabel = "Leer", onClick = onRead), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, "Leer", tint = Color.Black, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableSilencio(onClick: () -> Unit, onLong: () -> Unit) =
    this.then(Modifier.combinedClickable(onClick = onClick, onLongClick = onLong))

/** Título de sección con «›», como en Apple Music. */
@Composable
fun TituloSilencio(texto: String, cuenta: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LText(texto, LectorType.Display.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        if (onClick != null) LText(" ›", LectorType.Display.copy(fontSize = 22.sp), color = LectorColors.Muted)
        Spacer(Modifier.weight(1f))
        if (cuenta != null) LText(cuenta, LectorType.Label.copy(fontSize = 14.sp), color = LectorColors.Muted)
    }
}

@Composable
fun TituloSilencioSec(title: String, label: String? = null, expanded: Boolean = false, onClick: (() -> Unit)? = null) =
    TituloSilencio(title, label, onClick)

// ───────────── Biblioteca (Silencio) ─────────────

@Composable
fun LibraryTitleBlock(title: String, subtitle: String?, modifier: Modifier = Modifier, subtitleContent: (@Composable () -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        LText(title, LectorType.Display.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        if (subtitleContent != null) subtitleContent()
        else if (subtitle != null) LText(subtitle, LectorType.Label.copy(fontSize = 13.sp), color = LectorColors.Muted, maxLines = 1)
    }
}

/** Botón de la cabecera: círculo de vidrio; activo (búsqueda abierta, filtros puestos) se pone blanco. */
@Composable
fun LibraryIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, active: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).vidrio(CircleShape).clickable(role = Role.Button, onClickLabel = contentDescription) { Tacto.tic(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        FondoVidrio(Modifier.matchParentSize())
        if (active) Box(Modifier.matchParentSize().background(LectorColors.Text))
        Icon(icon, contentDescription, tint = if (active) LectorColors.Bg else LectorColors.Text, modifier = Modifier.size(20.dp))
    }
}

/** Categorías como control segmentado: pista gris y la elegida en blanco; se desplaza para dejar a la vista la elegida. */
@Composable
fun LibraryTabChips(labels: List<String>, counts: List<Int>?, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val estado = androidx.compose.foundation.lazy.rememberLazyListState()
    androidx.compose.runtime.LaunchedEffect(selected) { if (selected in labels.indices) estado.animateScrollToItem((selected - 1).coerceAtLeast(0)) }
    androidx.compose.foundation.lazy.LazyRow(
        modifier.fillMaxWidth(), state = estado,
        contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(labels.size) { i ->
            val activa = i == selected
            val fondo by animateColorAsState(if (activa) LectorColors.Text else LectorColors.Card, spring(stiffness = Spring.StiffnessMediumLow), label = "tab")
            val tinta by animateColorAsState(if (activa) LectorColors.Bg else LectorColors.Text, spring(stiffness = Spring.StiffnessMediumLow), label = "tabT")
            Row(
                Modifier.height(36.dp).clip(CircleShape).background(fondo).clickable(role = Role.Tab) { Tacto.tic(); onSelect(i) }.padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LText(labels[i], LectorType.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = tinta, maxLines = 1)
                val c = counts?.getOrNull(i)
                if (c != null) LText("  $c", LectorType.Label.copy(fontSize = 13.sp), color = tinta.copy(alpha = 0.6f), maxLines = 1)
            }
        }
    }
}

/** Encabezado de grupo (Hoy, Ayer, una fecha): negrita de 17 sp y la cuenta en gris. */
@Composable
fun GeneratedGroupHeader(text: String, modifier: Modifier = Modifier, count: String? = null) {
    Row(modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        LText(text, LectorType.Body.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f), maxLines = 1)
        if (count != null) LText(count, LectorType.Label.copy(fontSize = 13.sp), color = LectorColors.Muted)
    }
}

// ───────────── Ficha (Silencio): como la página de un álbum ─────────────

/** Cabecera de la ficha: título y autores centrados, datos, «Leer» en blanco y las acciones en círculos de vidrio. */
@Composable
fun PlaylistHeader(
    title: String,
    creators: List<com.example.lector.ui.mochy.detail.CreatorUi>,
    stats: String,
    playLabel: String,
    playEnabled: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    onDescriptionLongClick: () -> Unit = {},
    onCoverClick: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    onTitleLongClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val alto = com.example.lector.ui.mochy.detail.playlistCoverHeight()
    Box(modifier.fillMaxWidth().heightIn(min = alto * 0.975f), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.matchParentSize().clickable(interactionSource = null, indication = null, onClick = onCoverClick))
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.combinedClickableSilencio(onTitleClick, onTitleLongClick)) {
                LText(title, LectorType.Display.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center), maxLines = 2)
            }
            if (creators.isNotEmpty()) LText(creators.joinToString(", ") { it.name }, LectorType.Body.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center), color = LectorColors.Text.copy(alpha = 0.78f), maxLines = 1)
            if (stats.isNotEmpty()) LText(stats, LectorType.Label.copy(fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center), color = LectorColors.Text.copy(alpha = 0.6f), maxLines = 1)
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(CircleShape).background(if (playEnabled) LectorColors.Text else LectorColors.Text.copy(alpha = 0.3f))
                    .clickable(enabled = playEnabled, onClickLabel = playLabel) { Tacto.tic(); onPlayClick() },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = LectorColors.Bg, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(6.dp))
                    LText(playLabel, LectorType.Body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold), color = LectorColors.Bg, maxLines = 1)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { actions() }
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().clickable(onClick = onDescriptionLongClick)) {
                    LText(description, LectorType.Body.copy(fontSize = 14.sp, lineHeight = 20.sp), color = LectorColors.Text.copy(alpha = 0.75f), maxLines = 3)
                }
            }
        }
    }
}

/** Acción redonda de vidrio de la ficha (biblioteca, categorías, web, descargar). */
@Composable
fun CircleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit,
    spinOnClick: Boolean = false, tint: Color? = null,
) {
    Box(
        Modifier.padding(horizontal = 4.dp).size(48.dp).vidrio(CircleShape).clickable(role = Role.Button, onClickLabel = contentDescription) { Tacto.tic(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        FondoVidrio(Modifier.matchParentSize())
        Icon(icon, contentDescription, tint = tint ?: LectorColors.Text, modifier = Modifier.size(22.dp))
    }
}
