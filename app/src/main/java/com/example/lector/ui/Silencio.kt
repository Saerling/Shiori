package com.example.lector.ui

import androidx.compose.ui.draw.drawBehind

import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
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

/** Vidrio dentro de las pantallas (barras de arriba): translúcido, con la intensidad elegida. */
@Composable
private fun FondoVidrio(modifier: Modifier = Modifier) =
    Box(modifier.background(LectorColors.Bg.copy(alpha = 0.99f - 0.14f * Aspecto.vidrio)))

/**
 * Barra de pestañas flotante (el «dock» de ColorOS 17): una cápsula de vidrio con las cinco pestañas y, aparte, la
 * esfera de Más. La pestaña activa la marca una cápsula de luz que se desliza con un muelle de una a otra.
 */
@Composable
fun BarraSilencio(selected: LectorTab, onSelect: (LectorTab) -> Unit, modifier: Modifier = Modifier) {
    val pestanas = remember { listOf(LectorTab.Home, LectorTab.Library, LectorTab.Updates, LectorTab.History, LectorTab.More) }
    val indice = pestanas.indexOf(selected)
    val pos by animateFloatAsState(if (indice >= 0) indice.toFloat() else -1f, spring(dampingRatio = 0.72f, stiffness = 420f), label = "indicador")
    val visible by animateFloatAsState(if (indice >= 0) 1f else 0f, spring(stiffness = 600f), label = "indicadorVis")
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val forma = CircleShape
        Box(Modifier.weight(1f).height(62.dp).vidrioFlotante(forma)) {
            // Cápsula de luz detrás de la pestaña activa.
            val acento = androidx.compose.material3.MaterialTheme.colorScheme.primary
            Box(
                Modifier.fillMaxSize().padding(4.dp).drawBehind {
                    if (visible <= 0f) return@drawBehind
                    val ancho = size.width / pestanas.size
                    val x = ancho * pos.coerceAtLeast(0f)
                    drawRoundRect(
                        (if (TemaActual.oscuro) Color.White else Color.Black).copy(alpha = 0.10f * visible),
                        androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Size(ancho, size.height),
                        androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                    )
                    if (Aspecto.luminoso) drawCircle(
                        Brush.radialGradient(listOf(acento.copy(alpha = 0.22f * visible), Color.Transparent), androidx.compose.ui.geometry.Offset(x + ancho / 2, size.height), ancho * 0.6f),
                        ancho * 0.6f, androidx.compose.ui.geometry.Offset(x + ancho / 2, size.height),
                    )
                },
            )
            Row(Modifier.fillMaxSize().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                pestanas.forEach { t -> PestanaSilencio(t, t == selected, Modifier.weight(1f)) { onSelect(t) } }
            }
        }
        Spacer(Modifier.width(10.dp))
        // La búsqueda (Explorar) va aparte, en su propia esfera, como en Apple Music.
        val activa = selected == LectorTab.Search
        val fuente = remember { MutableInteractionSource() }
        Box(
            Modifier.size(62.dp).respuestaFlexible(fuente, 0.88f).vidrioFlotante(CircleShape)
                .selectable(selected = activa, interactionSource = fuente, indication = null, role = Role.Tab) { Tacto.tic(); onSelect(LectorTab.Search) },
            contentAlignment = Alignment.Center,
        ) {
            if (activa) Box(Modifier.matchParentSize().padding(5.dp).clip(CircleShape).background(LectorColors.Text.copy(alpha = 0.12f)))
            Icon(Icons.Rounded.Search, "Explorar", tint = if (activa) LectorColors.Text else LectorColors.Text.copy(alpha = 0.75f), modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
private fun PestanaSilencio(t: LectorTab, activa: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val tinta by animateColorAsState(
        if (activa) LectorColors.Text else LectorColors.Text.copy(alpha = 0.55f),
        spring(stiffness = Spring.StiffnessMediumLow), label = "tinta",
    )
    val fuente = remember { MutableInteractionSource() }
    Column(
        modifier.fillMaxHeight().clip(CircleShape).respuestaFlexible(fuente, 0.9f)
            .selectable(selected = activa, interactionSource = fuente, indication = null, role = Role.Tab, onClick = { Tacto.tic(); onClick() }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(if (t == LectorTab.More) Icons.Rounded.Settings else if (activa) t.icon else t.iconoApagado, null, tint = tinta, modifier = Modifier.size(22.dp))
        LText(when (t) { LectorTab.Updates -> "Novedades"; LectorTab.Library -> "Biblioteca"; else -> t.label }, LectorType.Label.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp), color = tinta, maxLines = 1)
    }
}

/** Mini lector: píldora de vidrio con la portada, el capítulo y un botón para seguir. */
@Composable
fun MiniSilencio(
    titulo: String, subtitulo: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    progreso: Float? = null,
    portada: @Composable (Modifier) -> Unit,
) {
    val forma = SuaveShape(30.dp)
    Box(modifier.fillMaxWidth().height(60.dp).vidrioFlotante(CircleShape).clickable(onClickLabel = "Seguir leyendo", onClick = onClick)) {
        Row(Modifier.fillMaxSize().padding(start = 9.dp, end = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            portada(Modifier.size(42.dp).clip(SuaveShape(10.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LText(titulo, LectorType.Body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), maxLines = 1)
                LText(subtitulo, LectorType.Label, color = LectorColors.Muted, maxLines = 1)
            }
            Box(Modifier.size(42.dp).esfera(CircleShape, LectorColors.Text).clickable(onClickLabel = "Seguir leyendo", onClick = onClick), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, "Seguir leyendo", tint = LectorColors.Bg, modifier = Modifier.size(24.dp))
            }
        }
        if (progreso != null) {
            BarraLuminosa(progreso, Modifier.align(Alignment.BottomCenter).padding(horizontal = 30.dp), alto = 2.dp)
        }
    }
}

// ───────────── Piezas de página (Silencio) ─────────────

/** Botón redondo de vidrio (atrás, menús) que flota sobre el contenido. */
@Composable
fun BotonVidrio(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, modifier: Modifier = Modifier, tam: androidx.compose.ui.unit.Dp = 40.dp, onClick: () -> Unit) =
    BotonCirculo(icono, descripcion, onClick, modifier, tam = tam)

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
    val umbral = with(densidad) { 40.dp.toPx() }
    // La barra aparece justo cuando el título grande llega al botón atrás; el título se apaga a la vez (sin solaparse).
    val alfa by androidx.compose.runtime.remember { androidx.compose.runtime.derivedStateOf { (scroll.value / umbral).coerceIn(0f, 1f) } }
    Box(Modifier.fillMaxSize().background(LectorColors.Bg)) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll).statusBarsPadding().padding(bottom = 120.dp).anchoLectura()) {
            Spacer(Modifier.height(60.dp))
            LText(titulo, LectorType.Display, modifier = Modifier.padding(horizontal = MargenLateral).graphicsLayer { alpha = 1f - alfa; translationY = scroll.value * 0.25f }, maxLines = 2)
            Spacer(Modifier.height(10.dp))
            content()
        }
        Box(Modifier.fillMaxWidth().graphicsLayer { alpha = alfa }) {
            Box(Modifier.matchParentSize()) { FondoVidrio(Modifier.matchParentSize()); Regla(Modifier.align(Alignment.BottomCenter)) }
            Box(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp), contentAlignment = Alignment.Center) {
                LText(titulo, LectorType.Body.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold), maxLines = 1, modifier = Modifier.padding(horizontal = 70.dp))
            }
        }
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) BotonVidrio(Icons.Rounded.ArrowBack, "Atrás", onClick = onBack)
            Spacer(Modifier.weight(1f))
            acciones()
        }
    }
}

private fun Modifier.graphicsLayerAlpha(a: Float) = this.then(Modifier.graphicsLayer { alpha = a })

/** Dentro de un [GrupoFilas] las filas no pintan su propia tarjeta: las une el grupo. */
val LocalEnGrupo = androidx.compose.runtime.staticCompositionLocalOf { false }

/** Grupo de filas de ajustes: una sola tarjeta de esquinas continuas con líneas finas entre filas (como ColorOS e iOS). */
@Composable
fun GrupoFilas(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val forma = SuaveShape(22.dp)
    Column(modifier.padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth().clip(forma).background(LectorColors.Card).bordeSuave(forma)) {
        androidx.compose.runtime.CompositionLocalProvider(LocalEnGrupo provides true) { content() }
    }
}

/**
 * Fila de ajustes: icono en una esfera pequeña, título, detalle y chevron. Sola, es una cápsula propia de esquinas
 * continuas; dentro de [GrupoFilas], una fila más del grupo separada por una línea fina.
 */
@Composable
fun FilaSilencio(
    icon: androidx.compose.ui.graphics.vector.ImageVector?, titulo: String, detalle: String?,
    onClick: (() -> Unit)?, final: (@Composable () -> Unit)? = null,
    interruptor: Boolean? = null,
) {
    val enGrupo = LocalEnGrupo.current
    val forma = SuaveShape(20.dp)
    val fuente = remember { MutableInteractionSource() }
    Column(if (enGrupo) Modifier else Modifier.padding(horizontal = 12.dp, vertical = 3.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .then(if (enGrupo) Modifier else Modifier.clip(forma).background(LectorColors.Card).bordeSuave(forma))
                .respuestaFlexible(fuente, if (enGrupo) 1f else 0.97f)
                .then(
                    if (interruptor != null && onClick != null) Modifier.toggleable(value = interruptor, interactionSource = fuente, indication = null, role = Role.Switch) { onClick() }
                    else if (onClick != null) Modifier.clickable(fuente, null, onClick = onClick) else Modifier
                )
                .heightIn(min = 56.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(Modifier.size(32.dp).esfera(CircleShape, LectorColors.CardHigh, sombra = false), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = LectorColors.Text, modifier = Modifier.size(17.dp))
                }
                Spacer(Modifier.width(13.dp))
            }
            Column(Modifier.weight(1f)) {
                LText(titulo, LectorType.Body.copy(fontSize = 16.sp))
                if (!detalle.isNullOrBlank()) LText(detalle, LectorType.Label.copy(fontSize = 13.sp, fontWeight = FontWeight.Normal), color = LectorColors.Muted, maxLines = 3)
            }
            if (final != null) { Spacer(Modifier.width(12.dp)); final() }
            else if (onClick != null) { Spacer(Modifier.width(8.dp)); LText("›", LectorType.Body.copy(fontSize = 20.sp), color = LectorColors.Muted) }
        }
        if (enGrupo) Regla(inicio = if (icon != null) 59.dp else 14.dp)
    }
}

/** Interruptor nativo de Material 3 (TalkBack, tamaño de letra y gestos del sistema) con los colores de Silencio. */
@Composable
fun InterruptorSilencio(activado: Boolean, onChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    androidx.compose.material3.Switch(
        checked = activado, onCheckedChange = onChange, modifier = modifier,
        colors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = LectorColors.Bg, checkedTrackColor = LectorColors.Text, checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = LectorColors.Muted, uncheckedTrackColor = LectorColors.CardHigh, uncheckedBorderColor = Color.Transparent,
        ),
    )
}

/** Tarjeta grande de «Seguir leyendo» (como las de Inicio en Apple Music): portada a sangre y franja de vidrio abajo. */
@Composable
fun TarjetaSeguir(
    comic: com.example.lector.Comic, almacen: com.example.lector.Almacen, etiqueta: String,
    onOpen: () -> Unit, onRead: () -> Unit, onLong: () -> Unit,
) {
    val forma = SuaveShape(24.dp)
    Box(
        Modifier.width(260.dp).height(360.dp).shadow(12.dp, forma, clip = false).clip(forma)
            .combinedClickableSilencio(onOpen, onLong),
    ) {
        ComicCover(comic, almacen, Modifier.fillMaxSize(), RoundedCornerShape(0.dp))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f))))
        Row(
            Modifier.align(Alignment.BottomCenter).padding(8.dp).fillMaxWidth().clip(SuaveShape(18.dp))
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



/** Categorías: FilterChip nativos de Material 3 en una fila; la elegida en blanco. Se desplaza para dejarla a la vista. */
@Composable
fun LibraryTabChips(labels: List<String>, counts: List<Int>?, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val estado = androidx.compose.foundation.lazy.rememberLazyListState()
    androidx.compose.runtime.LaunchedEffect(selected) { if (selected in labels.indices) estado.animateScrollToItem((selected - 1).coerceAtLeast(0)) }
    androidx.compose.foundation.lazy.LazyRow(
        modifier.fillMaxWidth(), state = estado,
        contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(labels.size) { i ->
            val c = counts?.getOrNull(i)
            ChipNativo(if (c != null) "${labels[i]}  $c" else labels[i], i == selected) { onSelect(i) }
        }
    }
}

/** Chip nativo (FilterChip de Material 3) con el aspecto Silencio: píldora gris; elegido, blanco con texto negro. */
@Composable
fun ChipNativo(texto: String, seleccionado: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val fondo by animateColorAsState(if (seleccionado) LectorColors.Text else LectorColors.Card, spring(stiffness = 700f), label = "pildora")
    val tinta by animateColorAsState(if (seleccionado) LectorColors.Bg else LectorColors.Text, spring(stiffness = 700f), label = "pildoraTinta")
    val fuente = remember { MutableInteractionSource() }
    Box(
        modifier.height(36.dp).respuestaFlexible(fuente, 0.92f).esfera(CircleShape, fondo, sombra = seleccionado)
            .selectable(selected = seleccionado, interactionSource = fuente, indication = null, role = Role.Tab) { Tacto.tic(); onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) { LText(texto, LectorType.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = tinta, maxLines = 1) }
}


// ───────────── Ficha (Silencio): como la página de un álbum ─────────────

/** Cabecera de la ficha: título y autores centrados, datos, «Leer» en blanco y las acciones en círculos de vidrio. */
@Composable
fun CabeceraFicha(
    title: String,
    creators: List<String>,
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
    val alto = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.55f).dp
    Box(modifier.fillMaxWidth().heightIn(min = alto * 0.975f), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.matchParentSize().clickable(interactionSource = null, indication = null, onClick = onCoverClick))
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.combinedClickableSilencio(onTitleClick, onTitleLongClick)) {
                LText(title, LectorType.Display.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center), maxLines = 2)
            }
            if (creators.isNotEmpty()) LText(creators.joinToString(", "), LectorType.Body.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center), color = LectorColors.Text.copy(alpha = 0.78f), maxLines = 1)
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
fun AccionRedonda(
    icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit,
    spinOnClick: Boolean = false, tint: Color? = null, active: Boolean = false,
) {
    BotonCirculo(icon, contentDescription, onClick, Modifier.padding(start = 6.dp), tam = 48.dp, tinte = tint, activo = active)
}
