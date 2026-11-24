package com.example.lector.ui

/*
 * Piezas de Silencio: todo lo que las pantallas usan para pintarse, hecho desde cero para Shiori.
 * Idea: monocromo, líneas finísimas (como Obsidian), títulos grandes y listas limpias (como Apple Music),
 * metadatos en letra monoespaciada, y el color lo ponen las portadas y el acento.
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.lector.Almacen
import com.example.lector.Comic
import kotlin.math.abs
import kotlin.math.roundToInt

// ───────────── Medidas ─────────────

/** Margen lateral de todas las páginas. */
val MargenLateral = 20.dp
/** Margen de la ficha (cabecera y capítulos). */
val MargenFicha = 20.dp
/** Hueco entre tarjetas de una fila o rejilla. */
val HuecoTarjetas = 14.dp
val AltoCampo = 44.dp
val TamPortadaFila = 52.dp
val RadioPortadaFila = 8.dp

// ───────────── Tipografía ─────────────

private val F get() = LectorFont
val EstiloFilaTitulo get() = TextStyle(fontFamily = F, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 19.sp, letterSpacing = (-0.01).em)
val EstiloFilaSub get() = TextStyle(fontFamily = F, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 16.sp)
val EstiloDatos get() = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.02.em)
val EstiloMono get() = EstiloDatos.copy(fontSize = 10.sp)
val EstiloSeccion get() = TextStyle(fontFamily = F, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em)
val EstiloTituloInicio get() = EstiloSeccion.copy(fontSize = 22.sp, lineHeight = 26.sp)
val EstiloPildora get() = TextStyle(fontFamily = F, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = (-0.01).em)
val EstiloTarjeta get() = TextStyle(fontFamily = F, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 16.sp)
val EstiloCuerpo get() = TextStyle(fontFamily = F, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)

/** Escala tipográfica de Material con la letra de Shiori (para lo que use MaterialTheme.typography). */
val TipoBase: Typography by lazy {
    val t = Typography()
    fun TextStyle.f() = copy(fontFamily = LectorFont)
    Typography(
        t.displayLarge.f(), t.displayMedium.f(), t.displaySmall.f(), t.headlineLarge.f(), t.headlineMedium.f(), t.headlineSmall.f(),
        t.titleLarge.f(), t.titleMedium.f(), t.titleSmall.f(), t.bodyLarge.f(), t.bodyMedium.f(), t.bodySmall.f(),
        t.labelLarge.f(), t.labelMedium.f(), t.labelSmall.f(),
    )
}

// ───────────── Colores de superficie ─────────────

/** Colores de campos, filas y chips; siguen al tema. */
object Tonos {
    val Campo: Color get() = LectorColors.Card
    val CampoFoco: Color get() = LectorColors.CardHigh
    val Fila: Color get() = LectorColors.Card
    val FilaAlta: Color get() = LectorColors.CardHigh
    val Linea: Color get() = LectorColors.Linea
    val Primario: Color get() = LectorColors.Text
    val Secundario: Color get() = LectorColors.Muted
    val Pista: Color get() = LectorColors.Pista
    val Chip: Color get() = LectorColors.Card
    val ChipElegido: Color get() = LectorColors.Text
}

/** Relleno de botones neutros (píldoras, círculos). */
val RellenoPildora: Color get() = LectorColors.CardHigh
val TintaPildora: Color get() = LectorColors.Text

/** Línea finísima de separación. */
@Composable
fun Regla(modifier: Modifier = Modifier, inicio: Dp = 0.dp) {
    Box(modifier.padding(start = inicio).fillMaxWidth().height(0.6.dp).background(LectorColors.Linea))
}

/** Borde finísimo alrededor de una superficie (estilo Obsidian). */
fun Modifier.borde(radio: Dp = 14.dp): Modifier = this.then(Modifier.border(0.6.dp, LectorColors.Linea, RoundedCornerShape(radio)))

// ───────────── Pulsación ─────────────

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.tocar(onLongClick: (() -> Unit)? = null, onClick: () -> Unit): Modifier =
    this.then(Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick))

// ───────────── Botones ─────────────

/** Esfera pulsable (acciones de cabecera, de fila, de la ficha): volumen suave, borde de luz y respuesta flexible. */
@Composable
fun BotonCirculo(
    icono: ImageVector, descripcion: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    tam: Dp = 40.dp, activo: Boolean = false, tinte: Color? = null, relleno: Color? = null,
) {
    val fondo by animateColorAsState(if (activo) LectorColors.Text else relleno ?: RellenoPildora, label = "circulo")
    val fuente = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier.size(tam).respuestaFlexible(fuente, 0.88f)
            .then(if (fondo.alpha == 0f) Modifier.clip(CircleShape) else Modifier.esfera(CircleShape, fondo, sombra = relleno == null))
            .clickable(fuente, null, role = Role.Button, onClickLabel = descripcion) { Tacto.tic(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, descripcion, tint = if (activo) LectorColors.Bg else tinte ?: LectorColors.Text, modifier = Modifier.size(tam * 0.5f))
    }
}

/** Botón principal: cápsula del color del texto (o de acento) con un halo suave debajo. */
@Composable
fun BotonLeer(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true, icono: ImageVector = Icons.Rounded.PlayArrow, color: Color = LectorColors.Text) {
    val tinta = textoSobreSil(color)
    val fuente = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val halo = MaterialTheme.colorScheme.primary
    Row(
        modifier.height(50.dp).respuestaFlexible(fuente, 0.95f)
            .then(if (Aspecto.luminoso && habilitado) Modifier.shadow(14.dp, CircleShape, clip = false, ambientColor = halo.copy(alpha = 0.5f), spotColor = halo.copy(alpha = 0.6f)) else Modifier)
            .esfera(CircleShape, if (habilitado) color else color.copy(alpha = 0.3f), sombra = false)
            .clickable(fuente, null, enabled = habilitado, onClickLabel = texto) { Tacto.tic(); onClick() }.padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icono, null, tint = tinta, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        LText(texto, EstiloPildora, color = tinta, maxLines = 1)
    }
}

fun textoSobreSil(c: Color): Color = if ((c.red * 0.299f + c.green * 0.587f + c.blue * 0.114f) > 0.6f) Color.Black else Color.White

/** Botón pequeño de fila (descargar, anclar…): círculo tonal de 34 dp. */
@Composable
fun AccionFila(icon: ImageVector, contentDescription: String, onClick: () -> Unit, tint: Color? = null, fill: Color? = null) =
    BotonCirculo(icon, contentDescription, onClick, tam = 34.dp, tinte = tint, relleno = fill)

// ───────────── Títulos ─────────────

/** Título de página grande con subtítulo opcional. */
@Composable
fun BloqueTitulo(title: String, subtitle: String?, modifier: Modifier = Modifier, subtitleContent: (@Composable () -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        LText(title, LectorType.Display, maxLines = 1)
        if (subtitleContent != null) subtitleContent()
        else if (subtitle != null) LText(subtitle, EstiloDatos, color = LectorColors.Muted, maxLines = 1)
    }
}
val AltoBloqueTitulo = 64.dp

/** Título de sección: negrita, «›» si se puede abrir, y algo al final (cuenta, acción). */
@Composable
fun TituloSeccionS(
    title: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, label: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null, showChevron: Boolean = onClick != null, count: Int? = null,
    thumbnail: (@Composable () -> Unit)? = null, expanded: Boolean = false,
) {
    Row(
        modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = MargenLateral, end = MargenLateral, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LText(title, EstiloTituloInicio, Modifier.weight(1f, fill = false), maxLines = 1)
        if (showChevron) LText("  ›", EstiloTituloInicio, color = LectorColors.Muted)
        Spacer(Modifier.weight(1f))
        if (label != null) LText(label, EstiloDatos, color = LectorColors.Muted)
        if (count != null) LText(count.toString(), EstiloDatos, color = LectorColors.Muted)
        trailing?.invoke(this)
    }
}

/** Cabecera de grupo (Hoy, Ayer, una fecha) en monoespaciada, como una propiedad de Obsidian. */
@Composable
fun CabeceraGrupo(text: String, modifier: Modifier = Modifier, count: String? = null) {
    Row(modifier.fillMaxWidth().background(LectorColors.Bg).padding(start = MargenLateral, end = MargenLateral, top = 20.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        LText(text.uppercase(), EstiloDatos.copy(fontWeight = FontWeight.Bold), Modifier.weight(1f), color = LectorColors.Muted, maxLines = 1)
        if (count != null) LText(count, EstiloDatos, color = LectorColors.Muted)
    }
}

/** Cuenta de resultados a la derecha de una cabecera. */
@Composable
fun Cuenta(n: Int) = LText(n.toString(), EstiloDatos, color = LectorColors.Muted)

/** «N capítulos · FIN · N leídos»: pie de una lista, monoespaciado. */
@Composable
fun PieLista(start: String, end: String, modifier: Modifier = Modifier, center: String = "FIN") {
    Row(modifier.fillMaxWidth().padding(horizontal = MargenFicha, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
        LText(start.uppercase(), EstiloMono, color = LectorColors.Muted)
        Regla(Modifier.weight(1f).padding(horizontal = 10.dp))
        LText(center, EstiloMono, color = LectorColors.Muted)
        Regla(Modifier.weight(1f).padding(horizontal = 10.dp))
        LText(end.uppercase(), EstiloMono, color = LectorColors.Muted)
    }
}

// ───────────── Filas ─────────────

/** Fila genérica: portada pequeña, título, subtítulo y algo al final. Se separan con una línea fina. */
@Composable
fun FilaPortada(
    title: String, subtitle: String?, onClick: (() -> Unit)?, modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null, selected: Float = 0f, dimmed: Boolean = false, titleMaxLines: Int = 1,
    subtitleColor: Color? = null, regla: Boolean = true,
    cover: @Composable () -> Unit, extra: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
) {
    val velo = LectorColors.Text.copy(alpha = 0.10f * selected)
    Column(modifier.fillMaxWidth().clip(SuaveShape(10.dp)).background(velo).then(if (onClick != null) Modifier.tocar(onLongClick, onClick) else Modifier)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            cover()
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f).graphicsLayer { alpha = if (dimmed) 0.45f else 1f }) {
                LText(title, EstiloFilaTitulo, maxLines = titleMaxLines)
                if (!subtitle.isNullOrEmpty()) LText(subtitle, EstiloFilaSub, color = subtitleColor ?: LectorColors.Muted, maxLines = 1)
                extra?.invoke()
            }
            if (trailing != null) { Spacer(Modifier.width(8.dp)); trailing() }
        }
        if (regla) Regla(inicio = TamPortadaFila + 14.dp)
    }
}

/** Fila de un cómic de la biblioteca (Inicio, Historial, Novedades). */
@Composable
fun FilaPortada(
    comic: Comic, almacen: Almacen, onClick: () -> Unit, onLongClick: (() -> Unit)?, onMoreClick: (() -> Unit)?,
    modifier: Modifier = Modifier, showProgress: Boolean = false,
) {
    FilaPortada(
        comic.titulo, if (showProgress) comic.textoPagina() else null, onClick, modifier.padding(horizontal = MargenLateral), onLongClick = onLongClick,
        cover = { ComicCover(comic, almacen, Modifier.size(TamPortadaFila).aspectRatio(1f), formaPortada(0.8f)) },
        extra = if (showProgress) ({ Spacer(Modifier.height(5.dp)); ProgressLine(comic.progresoLectura(), Modifier.width(120.dp)) }) else null,
        trailing = onMoreClick?.let { { AccionFila(Icons.Rounded.PlayArrow, "Seguir leyendo", it) } },
    )
}

/** Fila de capítulo (o pista): número a la izquierda, título; leído = apagado. */
@Composable
fun FilaCapitulo(
    badge: String, title: String, subtitle: String, read: Boolean, accent: Color,
    onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null, leading: (@Composable () -> Unit)? = null,
    progressLabel: String? = null, selected: Boolean = false,
) {
    val fondo by animateColorAsState(if (selected) accent.copy(alpha = 0.14f) else LectorColors.Bg.copy(alpha = 0f), label = "selCap")
    val tinta = if (read) LectorColors.Muted else LectorColors.Text
    Column(modifier.fillMaxWidth().background(fondo).tocar(onLongClick, onClick)) {
        Row(Modifier.fillMaxWidth().padding(start = MargenFicha, end = MargenFicha - 6.dp, top = 11.dp, bottom = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(40.dp)) {
                LText(badge, EstiloDatos.copy(fontSize = 12.sp), color = if (read) LectorColors.Muted.copy(alpha = 0.6f) else LectorColors.Muted, maxLines = 1)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!read) { Box(Modifier.size(6.dp).clip(CircleShape).background(accent)); Spacer(Modifier.width(7.dp)) }
                    leading?.let { it(); Spacer(Modifier.width(5.dp)) }
                    LText(title, EstiloFilaTitulo.copy(fontWeight = if (read) FontWeight.Normal else FontWeight.SemiBold), color = tinta, maxLines = 1)
                }
                if (subtitle.isNotEmpty() || progressLabel != null) Row {
                    if (progressLabel != null) { LText(progressLabel, EstiloFilaSub, color = accent, maxLines = 1); if (subtitle.isNotEmpty()) LText(" · ", EstiloFilaSub, color = LectorColors.Muted) }
                    LText(subtitle, EstiloFilaSub, color = LectorColors.Muted, maxLines = 1)
                }
            }
            trailing?.invoke()
        }
        Regla(inicio = MargenFicha + 40.dp)
    }
}

/** Marca de selección redonda. */
@Composable
fun MarcaSeleccion(checked: Boolean, modifier: Modifier = Modifier) {
    val a = MaterialTheme.colorScheme.primary
    Box(
        modifier.padding(6.dp).size(22.dp).clip(CircleShape).background(if (checked) a else Color.Transparent)
            .border(1.5.dp, if (checked) a else LectorColors.Muted, CircleShape),
        contentAlignment = Alignment.Center,
    ) { if (checked) Icon(Icons.Rounded.Check, null, tint = textoSobreSil(a), modifier = Modifier.size(15.dp)) }
}

/** Control de orden: texto y flecha. */
@Composable
fun ControlOrden(label: String, descending: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(CircleShape).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        LText(label, EstiloDatos, color = LectorColors.Muted, maxLines = 1)
        Icon(if (descending) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp, null, tint = LectorColors.Muted, modifier = Modifier.size(16.dp))
    }
}

// ───────────── Gestos en filas de capítulo ─────────────

/**
 * Deslizar una fila de capítulo: a la derecha o a la izquierda hace lo elegido en Aspecto → Gestos.
 * Pasado un umbral se nota un toque y, al soltar, se hace la acción; la fila vuelve a su sitio.
 */
@Composable
fun DeslizarCapitulo(
    estado: EstadoGesto, onAction: (AccionGesto) -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, slab: Boolean = true, content: @Composable () -> Unit,
) {
    GestosCapitulos.iniciar(androidx.compose.ui.platform.LocalContext.current)
    val der = GestosCapitulos.derecha
    val izq = GestosCapitulos.izquierda
    val umbral = with(LocalDensity.current) { 84.dp.toPx() }
    var dx by remember { mutableFloatStateOf(0f) }
    var arrastrando by remember { mutableStateOf(false) }
    val mostrado by animateFloatAsState(if (arrastrando) dx else 0f, if (arrastrando) spring(stiffness = 10_000f) else spring(dampingRatio = 0.7f, stiffness = 500f), label = "desliz")
    val accion = if (mostrado > 0) der else if (mostrado < 0) izq else AccionGesto.NADA
    val pasado = abs(dx) >= umbral
    LaunchedEffect(pasado) { if (pasado && arrastrando) Tacto.umbral() }
    Box(modifier.fillMaxWidth()) {
        if (accion != AccionGesto.NADA && abs(mostrado) > 1f) {
            Box(Modifier.matchParentSize().background(accion.color.copy(alpha = if (pasado) 0.9f else 0.35f)), contentAlignment = if (mostrado > 0) Alignment.CenterStart else Alignment.CenterEnd) {
                Row(Modifier.padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(accion.icono, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    LText(textoGesto(accion, estado), EstiloDatos, color = Color.White, maxLines = 1)
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().offset { IntOffset(mostrado.roundToInt(), 0) }.background(LectorColors.Bg)
                .then(if (!enabled) Modifier else Modifier.pointerInput(der, izq) {
                    detectHorizontalDragGestures(
                        onDragStart = { arrastrando = true; dx = 0f },
                        onDragEnd = {
                            val a = if (dx > 0) der else izq
                            if (abs(dx) >= umbral && a != AccionGesto.NADA) onAction(a)
                            arrastrando = false; dx = 0f
                        },
                        onDragCancel = { arrastrando = false; dx = 0f },
                    ) { ch, d ->
                        val nuevo = dx + d
                        val permitido = (nuevo > 0 && der != AccionGesto.NADA) || (nuevo < 0 && izq != AccionGesto.NADA)
                        if (permitido) { dx = nuevo.coerceIn(-umbral * 1.6f, umbral * 1.6f); ch.consume() }
                    }
                }),
        ) { content() }
    }
}

private fun textoGesto(a: AccionGesto, e: EstadoGesto) = when (a) {
    AccionGesto.LEIDO -> if (e.leido) "No leído" else "Leído"
    AccionGesto.MARCA -> if (e.marcado) "Quitar marca" else "Marcar"
    AccionGesto.DESCARGA -> if (e.descargado || e.descargando) "Quitar descarga" else "Descargar"
    else -> a.corto
}

// ───────────── Búsqueda ─────────────

/** Campo de búsqueda: píldora gris con lupa; lo que haya al final (borrar, filtros). */
@Composable
fun CampoBuscar(
    value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit, onSearch: (String) -> Unit, placeholder: String,
    modifier: Modifier = Modifier, focused: Boolean = false, leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null, enabled: Boolean = true, readOnlyClick: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null, onFocusChanged: ((Boolean) -> Unit)? = null,
) {
    val fondo by animateColorAsState(if (focused) Tonos.CampoFoco else Tonos.Campo, label = "campo")
    Row(
        modifier.fillMaxWidth().height(AltoCampo).clip(CircleShape).background(fondo).bordeLuminoso(CircleShape, 0.6f)
            .then(if (readOnlyClick != null) Modifier.clickable(onClick = readOnlyClick) else Modifier)
            .padding(start = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) leading() else Icon(Icons.Rounded.Search, null, tint = LectorColors.Muted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.text.isEmpty()) LText(placeholder, LectorType.Body.copy(fontSize = 16.sp), color = LectorColors.Pista, maxLines = 1)
            if (readOnlyClick == null) BasicTextField(
                value, onValueChange, Modifier.fillMaxWidth()
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .onFocusChanged { onFocusChanged?.invoke(it.isFocused) },
                enabled = enabled, singleLine = true,
                textStyle = LectorType.Body.copy(fontSize = 16.sp, color = LectorColors.Text),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(value.text) }),
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun GlifoCampo(icon: ImageVector, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = LectorColors.Muted, onClick: () -> Unit) =
    GlifoCampo(icon, contentDescription, onClick as (() -> Unit)?, modifier, tint)

/** Icono pulsable dentro de un campo o una fila. */
@Composable
fun GlifoCampo(icon: ImageVector, contentDescription: String?, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier, tint: Color = LectorColors.Muted) {
    Box(
        modifier.size(34.dp).clip(CircleShape).then(if (onClick != null) Modifier.clickable(onClickLabel = contentDescription, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(20.dp)) }
}

/** Chip de categoría o filtro: gris; elegido, invertido. */
@Composable
fun ChipCategoria(label: String, selected: Boolean, onClick: () -> Unit) = ChipNativo(label, selected, onClick = onClick)

@Composable
fun FilaCategorias(categories: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = MargenLateral), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(categories.size) { i -> ChipCategoria(categories[i], i == selectedIndex) { onSelect(i) } }
    }
}

/** Tres puntos que laten mientras se busca. */
@Composable
fun PuntosCarga(modifier: Modifier = Modifier, color: Color = LectorColors.Muted) {
    val t = rememberInfiniteTransition(label = "puntos")
    val f by t.animateFloat(0f, 3f, infiniteRepeatable(tween(900, easing = androidx.compose.animation.core.LinearEasing)), label = "f")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(3) { i -> Box(Modifier.size(6.dp).graphicsLayer { alpha = if (Mov.reducido) 0.7f else 0.3f + 0.7f * (1f - abs(((f - i + 3) % 3) - 0.5f).coerceIn(0f, 1f)) }.clip(CircleShape).background(color)) }
    }
}

/**
 * Cabecera de las pantallas con búsqueda (Explorar, Búsqueda global): título grande que se pliega al desplazar,
 * con el campo de búsqueda debajo y lo que haga falta al final.
 */
@Composable
fun CabeceraBusqueda(
    progressProvider: () -> Float, title: String?, modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null, subtitle: String? = null, titleContent: (@Composable () -> Unit)? = null,
    below: (@Composable () -> Unit)? = null, titleBlockHeight: Dp = AltoBloqueTitulo, field: @Composable () -> Unit,
) {
    val p = progressProvider()
    Column(modifier.fillMaxWidth().background(LectorColors.Bg.copy(alpha = 0.94f + 0.06f * p)).statusBarsPadding()) {
        if (title != null || titleContent != null) {
            val alto = AltoBloqueTitulo * (1f - p)
            Row(
                Modifier.fillMaxWidth().height(alto.coerceAtLeast(0.dp)).padding(horizontal = MargenLateral).graphicsLayer { alpha = 1f - p },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) { if (titleContent != null) titleContent() else BloqueTitulo(title!!, subtitle) }
                trailing?.invoke()
            }
        }
        Box(Modifier.padding(horizontal = MargenLateral, vertical = 8.dp)) { field() }
        below?.invoke()
        if (p > 0.95f) Regla()
    }
}

// ───────────── Resultados ─────────────

/** Fila de resultado: miniatura, título, subtítulo y lo que la fila necesite al final. */
@Composable
fun FilaResultado(
    artwork: @Composable () -> Unit, title: String, subtitle: String, onClick: (() -> Unit)?, modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null, titleColor: Color = LectorColors.Text, subtitleColor: Color = LectorColors.Muted,
    trailing: (@Composable () -> Unit)? = null, rule: Boolean = true,
) {
    Column(modifier.fillMaxWidth().then(if (onClick != null) Modifier.tocar(onLongClick, onClick) else Modifier)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = MargenLateral, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(TamPortadaFila).clip(RoundedCornerShape(RadioPortadaFila)).background(LectorColors.Card), contentAlignment = Alignment.Center) { artwork() }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                LText(title, EstiloFilaTitulo, color = titleColor, maxLines = 1)
                if (subtitle.isNotEmpty()) LText(subtitle, EstiloFilaSub, color = subtitleColor, maxLines = 1)
            }
            trailing?.invoke()
        }
        if (rule) Regla(inicio = MargenLateral + TamPortadaFila + 14.dp)
    }
}

/** Mejor resultado: tarjeta con miniatura grande, título y un botón. */
@Composable
fun TarjetaResultado(
    artwork: @Composable () -> Unit, title: String, detail: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null, actionIcon: ImageVector? = null, actionDescription: String? = null,
    extra: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier.padding(horizontal = MargenLateral).fillMaxWidth().clip(SuaveShape(22.dp)).background(LectorColors.Card).bordeLuminoso(SuaveShape(22.dp), 0.6f)
            .tocar(onLongClick, onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(76.dp).clip(SuaveShape(10.dp)), contentAlignment = Alignment.Center) { artwork() }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            LText(title, EstiloFilaTitulo.copy(fontSize = 18.sp, lineHeight = 22.sp), maxLines = 2)
            Spacer(Modifier.height(3.dp))
            LText(detail, EstiloDatos, color = LectorColors.Muted, maxLines = 1)
        }
        extra?.invoke()
        if (actionIcon != null) BotonCirculo(actionIcon, actionDescription ?: "", onClick, tam = 40.dp, activo = true)
    }
}

/** Cabecera de una sección de resultados. */
@Composable
fun CabeceraResultados(title: String, modifier: Modifier = Modifier, large: Boolean = true, ruleAbove: Boolean = true, ruleBelow: Boolean = false, trailing: (@Composable () -> Unit)? = null) {
    Column(modifier.fillMaxWidth()) {
        if (ruleAbove) Regla()
        Row(Modifier.fillMaxWidth().padding(horizontal = MargenLateral, vertical = if (large) 14.dp else 10.dp), verticalAlignment = Alignment.CenterVertically) {
            LText(title, if (large) EstiloSeccion else EstiloDatos.copy(fontWeight = FontWeight.Bold), Modifier.weight(1f), color = if (large) LectorColors.Text else LectorColors.Muted, maxLines = 1)
            trailing?.invoke()
        }
        if (ruleBelow) Regla()
    }
}

// ───────────── Vacíos y esqueletos ─────────────

@Composable
fun Vacio(modifier: Modifier = Modifier, text: String = "Aquí no hay nada", title: String? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        LText("∅", LectorType.Display.copy(fontSize = 44.sp, fontWeight = FontWeight.Light), color = LectorColors.Muted.copy(alpha = 0.6f))
        Spacer(Modifier.height(10.dp))
        if (title != null) LText(title, EstiloSeccion.copy(textAlign = TextAlign.Center), maxLines = 2)
        LText(text, EstiloCuerpo.copy(textAlign = TextAlign.Center), color = LectorColors.Muted)
        if (action != null && onAction != null) { Spacer(Modifier.height(18.dp)); BotonLeer(action, onAction, icono = Icons.Rounded.Check) }
    }
}

@Composable
fun EsqueletoCapitulos(rows: Int = 7) = BonesCapitulos(rows)

// ───────────── Tarjetas de portada ─────────────

/** Tarjeta de un cómic en una fila horizontal: portada 2:3 con borde fino y título debajo. */
@Composable
fun TarjetaPortada(comic: Comic, almacen: Almacen, modifier: Modifier = Modifier) {
    Column(modifier) {
        ComicCover(comic, almacen, Modifier.fillMaxWidth().aspectRatio(2f / 3f).sombraPortada())
        Spacer(Modifier.height(7.dp))
        LText(comic.titulo, EstiloTarjeta, maxLines = 2)
    }
}

/** Sombra suave de libro en estante (Readest). */
fun Modifier.sombraPortada(): Modifier = this.then(Modifier.shadow(6.dp, FormaPortada, clip = false, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.3f)))

/** Fila horizontal de tarjetas. */
@Composable
fun <T> FilaTarjetas(rowKey: String, items: List<T>, key: (T) -> Any, modifier: Modifier = Modifier, ancho: Dp = 128.dp, content: @Composable (T) -> Unit) {
    LazyRow(modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = MargenLateral), horizontalArrangement = Arrangement.spacedBy(HuecoTarjetas)) {
        items(items, key = key) { Box(Modifier.width(ancho)) { content(it) } }
    }
}

// ───────────── Hoja de abajo flotante ─────────────

/** Hoja que sale de abajo con fondo del tema; se cierra tocando fuera o con atrás. */
@Composable
fun HojaFlotante(onClosed: () -> Unit, fullBleed: Boolean = false, surface: Color? = null, buttonBar: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    HojaLector(onClosed) {
        content()
        if (buttonBar != null) Box(Modifier.fillMaxWidth().padding(horizontal = MargenLateral, vertical = 12.dp)) { buttonBar() }
    }
}

/** Filas de un menú. */
@Composable
fun FilaMenu(icon: (@Composable () -> Unit)?, enabled: Boolean, onClick: () -> Unit, trailing: (@Composable () -> Unit)? = null, title: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(horizontal = MargenLateral, vertical = 13.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) { icon() }; Spacer(Modifier.width(14.dp)) }
        Box(Modifier.weight(1f)) { title() }
        trailing?.invoke()
    }
}

// ───────────── Aparición ─────────────

/** Recuerda qué elementos ya aparecieron (para no repetir la entrada al volver). */
class VistosAparicion { val vistos = mutableStateMapOf<String, Boolean>() }
@Composable fun rememberVistos(): VistosAparicion = remember { VistosAparicion() }

/** Aparición suave y corta la primera vez que se ve un elemento. */
fun Modifier.aparecer(key: String, seenState: VistosAparicion, enabled: Boolean = true, delayMillis: Int = 0, cascade: Boolean = false): Modifier =
    if (!enabled || seenState.vistos[key] == true) this else this.composed {
        val a = remember { Animatable(0f) }
        LaunchedEffect(key) { a.animateTo(1f, tween(320, delayMillis, EnfasisEasing)); seenState.vistos[key] = true }
        Modifier.graphicsLayer { alpha = a.value; translationY = (1f - a.value) * 14.dp.toPx() }
    }

@Composable
fun FilasMenu(content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), content = content)

val TamAvatar = 36.dp
val FormaFila = RoundedCornerShape(9.dp)

fun smoothstep(from: Float, to: Float, x: Float): Float { val t = ((x - from) / (to - from)).coerceIn(0f, 1f); return t * t * (3f - 2f * t) }

/** Botón redondo de la cabecera de búsqueda (perfil, filtros…). */
@Composable
fun BotonAvatar(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = TamAvatar, selected: Boolean = false) =
    BotonCirculo(icon, contentDescription ?: "", onClick, modifier, tam = size, activo = selected)

/** Botón de la cabecera de una página (buscar, filtros, menú); activo = invertido. */
@Composable
fun BotonCabecera(icon: ImageVector, contentDescription: String, active: Boolean = false, onClick: () -> Unit) =
    BotonCirculo(icon, contentDescription, onClick, activo = active)

/** Progreso 0..1 de plegado de una cabecera según cuánto se desplazó la lista. */
@Composable
fun rememberCollapseProgress(listState: androidx.compose.foundation.lazy.LazyListState): State<Float> {
    val lim = with(LocalDensity.current) { 56.dp.toPx() }
    return remember(listState) { derivedStateOf { if (listState.firstVisibleItemIndex > 0) 1f else (listState.firstVisibleItemScrollOffset / lim).coerceIn(0f, 1f) } }
}

/** Alto de una cabecera con búsqueda (para dejar hueco arriba en la lista). */
@Composable
fun altoCabeceraBusqueda(withTitle: Boolean = true, extra: Dp = 0.dp, titleBlockHeight: Dp = AltoBloqueTitulo): Dp {
    val barra = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
    return barra + (if (withTitle) titleBlockHeight else 0.dp) + AltoCampo + 16.dp + extra
}

/** Plegado por desplazamiento anidado: se pliega al bajar y se despliega al volver arriba. */
class PlegadoAnidado internal constructor(private val entra: Float) : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
    var desplazado by mutableFloatStateOf(0f); private set
    override fun onPostScroll(consumed: androidx.compose.ui.geometry.Offset, available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
        if (consumed.y != 0f) desplazado = (desplazado - consumed.y).coerceAtLeast(0f)
        if (available.y > 0f && consumed.y == 0f) desplazado = 0f
        return androidx.compose.ui.geometry.Offset.Zero
    }
    val progreso: Float get() = (desplazado / entra).coerceIn(0f, 1f)
}

@Composable
fun rememberPlegadoAnidado(clave: Any?): Pair<PlegadoAnidado, State<Float>> {
    val entra = with(LocalDensity.current) { 56.dp.toPx() }
    val c = remember(clave) { PlegadoAnidado(entra) }
    val p = remember(c) { derivedStateOf { c.progreso } }
    return c to p
}

/** Barra de progreso fina de Material, con los colores de Silencio. */
@Composable
fun ProgresoLinea(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, trackColor: Color = LectorColors.PillFill) =
    androidx.compose.material3.LinearProgressIndicator(modifier, color, trackColor)

@Composable
fun ProgresoLinea(progress: () -> Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, trackColor: Color = LectorColors.PillFill) =
    androidx.compose.material3.LinearProgressIndicator(progress, modifier, color, trackColor, gapSize = 0.dp, drawStopIndicator = {})

// ───────────── Menú de mantener pulsado ─────────────

/** Menú al mantener pulsado un elemento: hoja con su título arriba y las acciones en filas. */
@Composable
fun <T : Any> MenuRetener(
    item: T?, onDismiss: () -> Unit, onPreviewClick: ((T) -> Unit)?, titulo: (T) -> String, subtitulo: (T) -> String? = { null },
    actions: @Composable (T) -> Unit,
) {
    val i = item ?: return
    HojaLector(onDismiss) {
        Column(Modifier.fillMaxWidth().then(if (onPreviewClick != null) Modifier.clickable { onPreviewClick(i) } else Modifier).padding(horizontal = MargenLateral, vertical = 4.dp)) {
            LText(titulo(i), EstiloSeccion, maxLines = 2)
            subtitulo(i)?.let { LText(it.uppercase(), EstiloDatos, color = LectorColors.Muted, maxLines = 1) }
        }
        Spacer(Modifier.height(8.dp))
        Regla()
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { actions(i) }
    }
}

@Composable
fun AccionRetener(icono: ImageVector, texto: String, destructive: Boolean = false, onClick: () -> Unit) {
    val tinta = if (destructive) LectorColors.Error else LectorColors.Text
    FilaMenu(icon = { Icon(icono, null, tint = tinta, modifier = Modifier.size(20.dp)) }, enabled = true, onClick = onClick) {
        LText(texto, LectorType.Body.copy(fontSize = 16.sp), color = tinta)
    }
}
