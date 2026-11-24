package com.example.lector.ui

import androidx.compose.foundation.border
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.lector.Almacen
import com.example.lector.Comic
import com.example.lector.ui.home.CarruselInicio
import com.example.lector.ui.home.DialogoCategoriasInicio
import com.example.lector.ui.home.DialogoEditarInicio
import com.example.lector.ui.home.DialogoEliminarInicio
import com.example.lector.ui.home.HojaMasInicio
import com.example.lector.ui.home.MarcaSeleccion
import com.example.lector.ui.home.OpcionInicio
import com.example.lector.ui.home.rememberAccionesInicio
import com.example.lector.ui.home.rememberOcultosInicio
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DropdownMenuItem

/*
 * Inicio con el diseño :
 * el héroe a sangre que pasa solo (ui/mochy/home/HeroCarousel.kt), secciones con título «ver todo» y filas de
 * portadas que aparecen en cascada, y la barra de arriba que cambia el logo por «Inicio» al desplazar.
 * Recomendaciones / escuchado hace poco / en bucle  -> seguir leyendo / recientes / en curso / sin leer.
 */

/** a Home row that fades and slides up the first time it scrolls into view */
private fun LazyListScope.homeItem(
    key: String,
    revealSeen: VistosAparicion,
    contentType: String,
    content: @Composable () -> Unit,
) {
    item(key = key, contentType = contentType) {
        Box(modifier = Modifier.aparecer(key, revealSeen, cascade = true)) {
            content()
        }
    }
}

private data class Destacado(val comic: Comic, val reason: String)

@Composable
fun HomeScreen(
    comics: List<Comic>,
    almacen: Almacen,
    onOpen: (Comic) -> Unit,
    onRead: (Comic) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    ext: com.example.lector.ext.ExtManager? = null,
    onEliminar: ((Comic) -> Unit)? = null,
) {
    if (comics.isEmpty()) {
        InicioVacio(onAdd, modifier)
        return
    }
    // «Quitar de Inicio» (persistente; vuelven al leerlos otra vez) y las acciones del menú / selección
    val ocultos = rememberOcultosInicio()
    val acciones = rememberAccionesInicio(almacen, ext, ocultos)
    val todos = comics
    val comics = remember(todos, ocultos.mapa) { ocultos.filtrar(todos).ifEmpty { todos } }
    val cuantosOcultos = todos.size - comics.size

    val featured = remember(comics) {
        comics.firstOrNull { it.id == almacen.ultimoAbierto } ?: comics.maxByOrNull { it.leido } ?: comics.first()
    }
    val recientesTodos = remember(comics) { comics.filter { it.leido > 0 }.sortedByDescending { it.leido } }
    val recientes = remember(recientesTodos) { recientesTodos.take(12) }
    val enCursoTodos = remember(comics) { comics.filter { it.enCurso() }.sortedByDescending { it.leido } }
    val enCurso = remember(enCursoTodos) { enCursoTodos.take(3) }
    val sinLeer = remember(comics) { comics.filter { it.leido == 0L } }
    // Carrusel héroe: el destacado primero y luego lo que estás leyendo (o, si no hay, lo que tienes por empezar).
    val heroe = remember(comics, featured) {
        val base = (listOf(featured) + enCursoTodos + recientes).distinctBy { it.id }
        (if (base.size >= 2) base else (base + sinLeer).distinctBy { it.id }).take(6)
            .map { c ->
                Destacado(
                    c,
                    when {
                        c.id == featured.id && c.leido > 0 -> "Seguir leyendo"
                        c.enCurso() -> "En curso"
                        c.leido > 0 -> "Leído hace poco"
                        else -> "Para empezar"
                    },
                )
            }
    }

    // «Ver todo»: la sección se despliega en su sitio, en filas
    var verRecientes by rememberSaveable { mutableStateOf(false) }
    var verEnCurso by rememberSaveable { mutableStateOf(false) }
    var verSinLeer by rememberSaveable { mutableStateOf(false) }

    val lazylistState = rememberLazyListState()
    val revealSeen = rememberVistos()
    // ---- Selección múltiple (Komikku): mantener pulsado selecciona; «⋮» abre el resto de acciones ----
    var seleccion by remember { mutableStateOf(emptySet<String>()) }
    var hojaMas by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    var categoriasDe by remember { mutableStateOf<List<String>?>(null) }
    var editar by remember { mutableStateOf<Comic?>(null) }
    var eliminar by remember { mutableStateOf<List<Comic>?>(null) }
    var menuDescarga by remember { mutableStateOf(false) }
    val enSeleccion = seleccion.isNotEmpty()
    LaunchedEffect(comics) { seleccion = seleccion.filterTo(HashSet()) { id -> comics.any { it.id == id } } }
    LaunchedEffect(enSeleccion) { if (!enSeleccion) hojaMas = false }
    BackHandler(enabled = enSeleccion) { seleccion = emptySet() }
    // El mini lector (LectorApp) cede su hueco a la barra de acciones mientras se selecciona.
    SideEffect { com.example.lector.ui.home.SeleccionInicio.activa = enSeleccion }
    DisposableEffect(Unit) { onDispose { com.example.lector.ui.home.SeleccionInicio.activa = false } }
    val seleccionados = remember(seleccion, comics) { comics.filter { it.id in seleccion } }
    fun alternar(c: Comic) { seleccion = if (c.id in seleccion) seleccion - c.id else seleccion + c.id }
    val toque: (Comic) -> Unit = { c -> if (enSeleccion) alternar(c) else onOpen(c) }
    val pulsacionLarga: (Comic) -> Unit = { c ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (enSeleccion) alternar(c) else seleccion = setOf(c.id)
    }
    val leerOSel: (Comic) -> Unit = { c -> if (enSeleccion) alternar(c) else onRead(c) }

    Box(modifier.fillMaxSize().background(LectorColors.Bg)) {
        LazyColumn(state = lazylistState, contentPadding = PaddingValues(bottom = 190.dp)) {
            item(key = "cabecera", contentType = "cabecera") {
                val hoy = remember { java.text.SimpleDateFormat("EEEE d MMM", java.util.Locale("es")).format(java.util.Date()).uppercase() }
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = MargenLateral, end = 16.dp, top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        LText(hoy, EstiloDatos, color = LectorColors.Muted)
                        LText("Inicio", LectorType.Display)
                    }
                    BotonCabecera(Icons.Rounded.Add, "Añadir") { onAdd() }
                }
            }
            item(key = "seguir_t", contentType = "title") { TituloSilencio("Seguir leyendo") }
            item(key = "seguir", contentType = "seguir") {
                LazyRow(contentPadding = PaddingValues(horizontal = MargenLateral), horizontalArrangement = Arrangement.spacedBy(HuecoTarjetas)) {
                    items(heroe, key = { "s_" + it.comic.id }) { h ->
                        TarjetaSeguir(h.comic, almacen, h.reason, onOpen = { toque(h.comic) }, onRead = { leerOSel(h.comic) }, onLong = { pulsacionLarga(h.comic) })
                    }
                }
            }

            if (recientes.isNotEmpty()) {
                homeItem("recientes_title", revealSeen, "title") {
                    TituloSilencioSec(
                        title = "Leídos recientemente",
                        label = "${recientesTodos.size}",
                        expanded = verRecientes,
                        onClick = { verRecientes = !verRecientes },
                    )
                }
                if (verRecientes) seccionEnFilas("rec", recientesTodos, almacen, toque, leerOSel, seleccion, pulsacionLarga)
                else homeItem("recientes_list", revealSeen, "cardRow") {
                    CarruselInicio("rec", recientes, almacen, revealSeen, seleccion, toque, pulsacionLarga)
                }
                espacioSeccion("recientes")
            }

            if (enCurso.isNotEmpty()) {
                homeItem("enCurso_title", revealSeen, "title") {
                    TituloSilencioSec(
                        title = "En curso",
                        label = "${enCursoTodos.size}",
                        expanded = verEnCurso,
                        onClick = if (enCursoTodos.size > enCurso.size) ({ verEnCurso = !verEnCurso }) else null,
                    )
                }
                seccionEnFilas("ec", if (verEnCurso) enCursoTodos else enCurso, almacen, toque, leerOSel, seleccion, pulsacionLarga, progreso = true)
                espacioSeccion("enCurso")
            }

            if (sinLeer.isNotEmpty()) {
                homeItem("sinLeer_title", revealSeen, "title") {
                    TituloSilencioSec(
                        title = "Sin leer",
                        label = "${sinLeer.size}",
                        expanded = verSinLeer,
                        onClick = { verSinLeer = !verSinLeer },
                    )
                }
                if (verSinLeer) seccionEnFilas("sl", sinLeer, almacen, toque, leerOSel, seleccion, pulsacionLarga)
                else homeItem("sinLeer_list", revealSeen, "cardRow") {
                    CarruselInicio("sl", sinLeer, almacen, revealSeen, seleccion, toque, pulsacionLarga)
                }
                espacioSeccion("sinLeer")
            }

            if (cuantosOcultos > 0) item(key = "ocultos", contentType = "ocultos") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = MargenLateral, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LText(
                        if (cuantosOcultos == 1) "1 quitado de Inicio" else "$cuantosOcultos quitados de Inicio",
                        LectorType.Label, Modifier.weight(1f), color = LectorColors.Muted,
                    )
                    ChipSel("Mostrar", false) { ocultos.mostrarTodos() }
                }
            }

            item(key = "bottom_spacer", contentType = "spacer") { Spacer(Modifier.height(30.dp)) }
        }

        if (enSeleccion) {
            Box(Modifier.fillMaxWidth().align(Alignment.TopCenter).background(LectorColors.Bg).statusBarsPadding()) {
                CabeceraSeleccion(
                    cantidad = seleccion.size,
                    onCerrar = { seleccion = emptySet() },
                    onTodo = { seleccion = comics.mapTo(HashSet()) { it.id } },
                    onInvertir = { seleccion = comics.mapTo(HashSet()) { it.id } - seleccion },
                )
            }
        }

        // En el hueco del mini lector, justo encima de la barra flotante (64 dp + 8 de margen + 8 de separación;
        // BarraAcciones ya suma las barras del sistema y 10 dp propios).
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp, start = 12.dp, end = 12.dp)) {
            BarraAcciones(
                visible = enSeleccion,
                acciones = buildList {
                    fun fin(f: (List<Comic>) -> Unit) { val l = seleccionados; seleccion = emptySet(); f(l) }
                    add(AccionBarra(Iconos.OjoTachado, "Quitar de Inicio") { fin(acciones::quitarDeInicio) })
                    add(AccionBarra(Icons.Rounded.Done, "Leído") { fin { acciones.marcar(it, true) } })
                    if (seleccionados.any { it.enLinea }) add(AccionBarra(Iconos.Descargar, "Descargar") { menuDescarga = true })
                    add(AccionBarra(Icons.Rounded.Menu, "Categorías") { categoriasDe = seleccionados.map { it.id } })
                    add(AccionBarra(Icons.Rounded.MoreVert, "Más") { hojaMas = true })
                },
            )
            MenuShiori(expanded = menuDescarga, onDismissRequest = { menuDescarga = false }) {
                DropdownMenuItem(text = { Text("Siguiente capítulo sin leer") }, onClick = {
                    menuDescarga = false; val l = seleccionados; seleccion = emptySet(); acciones.descargar(l, 1)
                })
                DropdownMenuItem(text = { Text("Siguientes 5 sin leer") }, onClick = {
                    menuDescarga = false; val l = seleccionados; seleccion = emptySet(); acciones.descargar(l, 5)
                })
                DropdownMenuItem(text = { Text("Todos los no leídos") }, onClick = {
                    menuDescarga = false; val l = seleccionados; seleccion = emptySet(); acciones.descargar(l, null)
                })
            }
        }
    }

    if (hojaMas && seleccionados.isNotEmpty()) {
        HojaMasInicio(seleccionados, almacen, puedeEliminar = onEliminar != null, onCerrar = { hojaMas = false }, onElegir = { o ->
            hojaMas = false
            val l = seleccionados
            val uno = l.first()
            fun fin(f: () -> Unit) { seleccion = emptySet(); f() }
            when (o) {
                OpcionInicio.NO_LEIDO -> fin { acciones.marcar(l, false) }
                OpcionInicio.QUITAR_HISTORIAL -> fin { acciones.quitarDelHistorial(l.filter { it.leido > 0 }) }
                OpcionInicio.DESCARGAR_5 -> fin { acciones.descargar(l, 5) }
                OpcionInicio.FICHA -> fin { onOpen(uno) }
                OpcionInicio.EDITAR -> fin { editar = uno }
                OpcionInicio.COMPARTIR -> fin { acciones.compartir(uno) }
                OpcionInicio.ELIMINAR -> eliminar = l
            }
        })
    }
    categoriasDe?.let { ids -> DialogoCategoriasInicio(ids, almacen) { categoriasDe = null; seleccion = emptySet() } }
    editar?.let { c -> DialogoEditarInicio(c, almacen) { editar = null } }
    eliminar?.let { l ->
        DialogoEliminarInicio(l, onConfirmar = { seleccion = emptySet(); l.forEach { onEliminar?.invoke(it) } }, onCerrar = { eliminar = null })
    }
}

private fun LazyListScope.espacioSeccion(id: String) {
    item(key = "section_spacer_$id", contentType = "spacer") { Spacer(Modifier.height(34.dp)) }
}

/** Una sección desplegada (o «En curso»): filas , una por cómic, como items de la lista (6000+ sin problema). */
private fun LazyListScope.seccionEnFilas(
    prefijo: String, lista: List<Comic>, almacen: Almacen,
    onOpen: (Comic) -> Unit, onRead: (Comic) -> Unit,
    seleccion: Set<String>, onLong: (Comic) -> Unit, progreso: Boolean = false,
) {
    items(lista, key = { prefijo + it.id }, contentType = { "row" }) { c ->
        val enSel = seleccion.isNotEmpty()
        val sel = c.id in seleccion
        Box(
            Modifier.fillMaxWidth()
                .background(if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent)
        ) {
            FilaPortada(
                comic = c,
                almacen = almacen,
                onClick = { onOpen(c) },
                onLongClick = { onLong(c) },
                onMoreClick = if (enSel) null else ({ onRead(c) }),
                showProgress = progreso,
                modifier = Modifier.fillMaxWidth(),
            )
            if (enSel) MarcaSeleccion(sel, Modifier.align(Alignment.CenterEnd).padding(end = MargenLateral))
        }
    }
}

/** Estado vacío: el «QuietHero»  (nada aún) con el botón para añadir. */
@Composable
private fun InicioVacio(onAdd: () -> Unit, modifier: Modifier) {
    val acento = MaterialTheme.colorScheme.primary
    Box(
        modifier.fillMaxSize().background(LectorColors.Bg)
    ) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.entrada(0), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                listOf(96, 120, 104).forEachIndexed { i, h ->
                    Box(Modifier.size(64.dp, h.dp).clip(RoundedCornerShape(8.dp)).background(if (i == 1) acento.copy(alpha = 0.35f) else LectorColors.Card).border(0.6.dp, LectorColors.Linea, RoundedCornerShape(8.dp)))
                }
            }
            Spacer(Modifier.height(28.dp))
            LText(
                "Tu estantería espera",
                LectorType.Display.copy( textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                Modifier.entrada(1),
            )
            Spacer(Modifier.height(8.dp))
            LText(
                "Añade un cómic o manga (.cbz, .zip, .pdf) y aparecerá aquí para seguir leyendo.",
                LectorType.Body.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center), Modifier.entrada(2),
                color = LectorColors.Muted,
            )
            Spacer(Modifier.height(24.dp))
            BotonExpresivo("Añadir cómic", Icons.Rounded.Add, onAdd, Modifier.entrada(3), EstiloBoton.RELLENO, alto = 54.dp)
        }
        LogoApp(
            Modifier.align(Alignment.TopStart).statusBarsPadding()
                .padding(start = LogoEsquinaInicio, top = LogoEsquinaArriba).size(LogoEsquina)
        )
    }
}
