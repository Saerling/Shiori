package com.example.lector.ui

// Biblioteca de animaciones y funciones de Shiori: qué hace cada interruptor, dónde actúa y su vista previa
// con las piezas reales de la app (cada previa fuerza su efecto con FxForzar, sin tocar el ajuste guardado).

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lector.Almacen
import com.example.lector.Comic
import com.example.lector.ModoTema
import com.example.lector.fx.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Los 12 estilos de diálogo (blocks.so Dialogs 01–12) como decoración del diálogo real de la app. */
fun Modifier.estiloDialogoFx(): Modifier = composed {
    if (!fxOn("dialogs")) return@composed Modifier
    val acento = MaterialTheme.colorScheme.primary
    when (Fx.variante("dialogs")) {
        0 -> Modifier.hazBorde(30.dp, acento)
        1 -> Modifier.bordeBrillante(30.dp)
        2 -> Modifier.noise(.06f)
        3 -> Modifier.foco(LectorColors.Text)
        4 -> Modifier.hazBorde(30.dp, acento).noise(.05f)
        5 -> Modifier.aparecerDifuso()
        6 -> Modifier.halo(listOf(acento, LectorColors.Text, acento))
        7 -> Modifier.bordeBrillante(30.dp, listOf(acento, LectorColors.Text, acento))
        8 -> Modifier.patron(Patron.Puntos, LectorColors.Text.copy(alpha = .10f))
        9 -> Modifier.patron(Patron.Cuadricula, LectorColors.Text.copy(alpha = .06f))
        10 -> Modifier.hazBorde(30.dp, acento, ms = 1400).aparecerDifuso()
        else -> Modifier.foco(acento).noise(.04f)
    }
}

/** Capas por encima de toda la app: revelado circular al cambiar el tema y la bienvenida (Onboarding). */
@Composable
fun FxCapasGlobales(oscuro: Boolean) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val rev = rememberRevelar()
    var primero by remember { mutableStateOf(true) }
    val conf = LocalConfiguration.current; val d = LocalDensity.current
    val centro = with(d) { Offset(conf.screenWidthDp.dp.toPx() / 2, conf.screenHeightDp.dp.toPx() / 3) }
    val fondoNuevo = LectorColors.Bg
    LaunchedEffect(oscuro) { if (primero) primero = false else if (Fx.on("themetoggle")) rev.revelar(centro, fondoNuevo) }
    CapaRevelar(rev, Modifier.fillMaxSize())
    val prefs = remember { ctx.getSharedPreferences("fx", android.content.Context.MODE_PRIVATE) }
    var vista by remember { mutableStateOf(prefs.getBoolean("onboarding_visto", false)) }
    if (fxOn("onboarding") && !vista) BienvenidaShiori { vista = true; prefs.edit().putBoolean("onboarding_visto", true).apply() }
}

@Composable
fun BienvenidaShiori(onTerminar: () -> Unit) {
    Box(Modifier.fillMaxSize().background(LectorColors.Bg)) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides LectorColors.Text) {
            Bienvenida(listOf(
                Triple("📚", "Bienvenido a Shiori", "Tu lector de manga y cómics: local y en línea, sin anuncios."),
                Triple("🧩", "Extensiones", "Añade listados en Explorar y lee de miles de fuentes."),
                Triple("✨", "Hazla tuya", "En Personalización están el cristal, los colores y esta biblioteca de animaciones."),
            ), onTerminar, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Command Menu: todas las pantallas de Shiori a un toque. */
fun comandosShiori(onRuta: (Ruta) -> Unit): List<Pair<String, () -> Unit>> = listOf(
    "Ajustes" to { onRuta(Ruta.Ajustes) }, "Personalización" to { onRuta(Ruta.Apariencia) },
    "Biblioteca de animaciones" to { onRuta(Ruta.BibliotecaFx) }, "Descargas" to { onRuta(Ruta.Descargas) },
    "Búsqueda global" to { onRuta(Ruta.BusquedaGlobal) }, "Estadísticas" to { onRuta(Ruta.Estadisticas) },
    "Categorías" to { onRuta(Ruta.Categorias) }, "Listados de extensiones" to { onRuta(Ruta.ListadosExt) },
    "Datos y almacenamiento" to { onRuta(Ruta.Datos) }, "Ajustes del lector" to { onRuta(Ruta.AjVisor) },
    "Ajustes de descargas" to { onRuta(Ruta.AjDescargas) }, "Seguridad" to { onRuta(Ruta.AjSeguridad) },
    "Acerca de" to { onRuta(Ruta.AcercaDe) },
)

/** Mapa de calor con los capítulos que leíste cada día (tu historial de lectura en línea). */
@Composable
fun MapaLecturaShiori(almacen: Almacen) {
    val dias = remember(almacen.historialLinea) {
        val hoy = System.currentTimeMillis() / 86_400_000L
        val cuenta = IntArray(140)
        almacen.historialLinea.forEach { val d = (hoy - it.leido / 86_400_000L).toInt(); if (d in 0 until 140) cuenta[139 - d]++ }
        cuenta.toList()
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp)) {
        LText("Tu lectura · últimas 20 semanas", LectorType.Label, color = LectorColors.Muted)
        Spacer(Modifier.height(8.dp))
        MapaActividad(dias, color = MaterialTheme.colorScheme.primary)
        LText("${dias.sum()} capítulos · racha de ${dias.reversed().takeWhile { it > 0 }.size} días", LectorType.Label, color = LectorColors.Muted)
    }
}

private val comicDemo = Comic(id = "fx_demo", titulo = "Shiori", paginas = 24, ultimaPagina = 8)

@Composable private fun Variantes(id: String, n: Int, etiqueta: String) {
    Row(Modifier.fillMaxWidth().horizontalScrollCompat(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(n) { k -> ChipSel("$etiqueta %02d".format(k + 1), Fx.variante(id) == k) { Fx.elegirVariante(id, k) } }
    }
}
@Composable private fun Modifier.horizontalScrollCompat() = this.then(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()))

/** El catálogo de Shiori. Cada vista usa las piezas reales de la app. */
fun catalogoShiori(almacen: Almacen, comics: List<Comic>, onRuta: (Ruta) -> Unit): List<FxItem> {
    val portada = comics.firstOrNull() ?: comicDemo
    fun i(id: String, n: String, o: String, u: String, v: @Composable () -> Unit) = FxItem(id, n, o, u, vista = v)
    @Composable fun Portada(m: Modifier = Modifier.width(120.dp)) = ComicCover(portada, almacen, m.aspectRatio(2f / 3f))
    return listOf(
        // ── Toda la app
        i("blurfade", "Blur Fade", "Magic UI", "Las listas y filas de toda la app aparecen subiendo con desenfoque") {
            var k by remember { mutableIntStateOf(0) }
            Column { key(k) { listOf("Descargas", "Categorías", "Estadísticas").forEachIndexed { n, t -> Box(Modifier.entrada(n)) { FilaAjuste(Icons.Rounded.Star, t, null) {} } } }; BotonExpresivo("Repetir", Icons.Rounded.Refresh, { k++ }) }
        },
        i("themetoggle", "Animated Theme Toggler", "Magic UI", "Al cambiar entre tema claro y oscuro, el nuevo color se expande en círculo") {
            val r = rememberRevelar(); val sc = rememberCoroutineScope(); val bg = LectorColors.Bg
            Box(Modifier.fillMaxWidth().height(120.dp)) { CapaRevelar(r, Modifier.matchParentSize())
                BotonExpresivo(if (almacen.modoTema == ModoTema.CLARO) "Pasar a oscuro" else "Pasar a claro", Icons.Rounded.Refresh, {
                    almacen.modoTema = if (almacen.modoTema == ModoTema.CLARO) ModoTema.OSCURO_PURO else ModoTema.CLARO; sc.launch { r.revelar(Offset(200f, 150f), bg) } }, Modifier.align(Alignment.Center)) }
        },
        i("noise", "Noise Texture", "Magic UI", "Grano fino sobre el cristal de la barra de navegación") { var t by remember { mutableStateOf(LectorTab.Home) }; FloatingNavBar(t, { t = it }) },
        i("ripple", "Ripple Button", "Magic UI", "Onda desde el dedo en todos los botones y portadas que se pulsan") { PillButton("Tócame", Icons.Rounded.Star, {}) },
        i("switch", "Switch", "coss ui", "Los interruptores de Ajustes con el estilo de coss") { var a by remember { mutableStateOf(true) }; FilaSwitch("Modo incógnito", "No anota el historial", a) { a = it } },
        i("checkbox", "Checkbox", "coss ui", "Las casillas y opciones (filtros, idiomas, listas) como casillas cuadradas") { var s by remember { mutableIntStateOf(0) }; Column { listOf("Sin leer", "Descargados", "Terminados").forEachIndexed { n, t -> FilaRadio(t, s == n) { s = n } } } },
        i("segmented", "Segmented Control / Tabs + Animated Background", "coss ui", "Los grupos de botones conectados (pestañas, filtros) con indicador que se desliza") { var s by remember { mutableIntStateOf(0) }; PestanasPildora(listOf("Capítulos", "Info", "Similares"), s, { s = it }) },
        i("empty", "Empty", "coss ui", "Las pantallas vacías (biblioteca, historial, descargas) con ícono y animación") { Box(Modifier.fillMaxWidth().height(240.dp)) { MensajeVacio("Tu biblioteca está vacía. Añade un cómic o explora fuentes.", "Explorar", {}) } },
        i("skeleton", "Skeleton", "coss ui", "Los esqueletos de carga con brillo") { BonesCapitulos(3) },
        i("spinner", "Spinner", "coss ui", "Los indicadores de carga de toda la app como girador") { IndicadorCarga(Modifier.size(48.dp)) },
        i("dialogs", "Dialogs 01–12", "blocks.so", "Todos los diálogos de la app con el estilo que elijas (12)") {
            var v by remember { mutableStateOf(false) }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) { Variantes("dialogs", 12, "Estilo")
                BotonExpresivo("Abrir un diálogo de verdad", Icons.Rounded.Info, { v = true }) }
            if (v) DialogoAlerta({ v = false }, { androidx.compose.material3.TextButton({ v = false }) { Text("Aceptar") } }, dismissButton = { androidx.compose.material3.TextButton({ v = false }) { Text("Cancelar") } },
                title = { Text("¿Borrar «${portada.titulo}»?") }, text = { Text("Se quitará de tu biblioteca. Los archivos descargados se conservan.") })
        },
        i("sheet", "Drawer / Sheet", "coss ui", "Las hojas inferiores con textura de grano") { var v by remember { mutableStateOf(false) }; BotonExpresivo("Abrir hoja", Icons.Rounded.KeyboardArrowUp, { v = true })
            if (v) HojaLector({ v = false }) { FilaAjuste(Icons.Rounded.Share, "Compartir", null) { v = false }; FilaAjuste(Icons.Rounded.Delete, "Borrar", null) { v = false } } },
        i("progress", "Progress", "coss ui", "Las barras de progreso de lectura con degradado animado") { var f by remember { mutableFloatStateOf(.3f) }; LaunchedEffect(Unit) { while (true) { delay(700); f = (f + .15f) % 1f } }; ProgressLine(f) },
        i("searchinput", "Search Input", "opensourceui", "Los buscadores muestran un haz de luz alrededor al escribir") { var q by remember { mutableStateOf("") }; CampoBusqueda(q, { q = it }, "Toca y escribe…") },
        i("textmorph", "Text Morph", "Motion Primitives", "Los botones cambian su texto letra a letra") { var b by remember { mutableStateOf(false) }; BotonExpresivo(if (b) "En la biblioteca" else "Añadir", if (b) Icons.Rounded.Check else Icons.Rounded.Add, { b = !b }) },
        i("comictext", "Comic Text", "Magic UI", "Los títulos de sección de Inicio con letras de cómic") { SectionHeader("Seguir leyendo") },
        i("pulsating", "Pulsating Button", "Magic UI", "El botón flotante «Continuar» late suavemente") { BotonFlotante("Continuar", MaterialTheme.colorScheme.primary, {}) },
        // ── Portadas
        i("tilt", "Tilt", "Motion Primitives", "Las portadas se inclinan bajo el dedo") { Portada() },
        i("glow", "Glow Effect", "Motion Primitives", "Halo de color girando detrás de cada portada") { Box(Modifier.padding(16.dp)) { Portada() } },
        i("magiccard", "Magic Card", "Magic UI", "Un foco de luz sigue al dedo sobre la portada") { Portada() },
        i("glare", "Glare Hover", "Magic UI", "Destello que cruza la portada al tocarla") { Portada() },
        i("backlight", "Backlight", "Magic UI", "La portada brilla con sus propios colores en la ficha del manga") { Contraluz(Modifier.width(124.dp).aspectRatio(2f / 3f)) { ComicCover(portada, almacen, Modifier.fillMaxSize()) } },
        i("marquee", "Marquee", "Magic UI", "Los carruseles de Inicio se desplazan solos sin fin") { CoverCarousel(comics.take(8).ifEmpty { listOf(comicDemo) }, almacen, {}) },
        i("carousel", "Carousel", "Motion Primitives", "Los carruseles de Inicio pasan página con escala") { CoverCarousel(comics.take(8).ifEmpty { listOf(comicDemo) }, almacen, {}) },
        // ── Estadísticas y descargas
        i("stats", "Stats 01–15", "blocks.so", "Las tarjetas de Estadísticas con el estilo que elijas (15)") { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Variantes("stats", 15, "Estilo"); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatCard("${comics.size}", "En la biblioteca", Modifier.weight(1f)); StatCard("42", "Terminados", Modifier.weight(1f)) } } },
        i("numberticker", "Number Ticker", "Magic UI", "Los números de Estadísticas cuentan desde cero al entrar") { var k by remember { mutableIntStateOf(0) }; Column { key(k) { StatCard("${comics.size.coerceAtLeast(128)}", "Capítulos leídos") }; BotonExpresivo("Repetir", Icons.Rounded.Refresh, { k++ }) } },
        i("activity", "Daily Activity / Github Contribution", "opensourceui", "Mapa de calor de tus capítulos leídos por día en Estadísticas") { MapaLecturaShiori(almacen) },
        i("circularprogress", "Animated Circular Progress Bar", "Magic UI", "La cola de descargas muestra cada capítulo en un anillo con %") { var f by remember { mutableFloatStateOf(0f) }; LaunchedEffect(Unit) { while (true) { delay(150); f = (f + .03f) % 1f } }; AnilloProgreso(f, Modifier.size(40.dp), MaterialTheme.colorScheme.primary, 4.dp, estilo = LectorType.Label) },
        // ── Lector
        i("nf_page", "Contador «12 / 40»", "NumberFlow", "El número de página del lector rueda al pasar de página") { var p by remember { mutableIntStateOf(12) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.background(androidx.compose.ui.graphics.Color.Black).padding(12.dp)) { NumeroFlowGrupo(p, 40, EstiloContador, Modifier.pildoraCristal(CircleShape, sombra = false).padding(horizontal = 14.dp, vertical = 5.dp), TintaBarra) }; BotonExpresivo("Siguiente página", Icons.Rounded.KeyboardArrowRight, { p = p % 40 + 1 }) } },
        i("scrollprogress", "Scroll Progress", "Magic UI", "Barra fina arriba del lector con tu avance en el capítulo") { var p by remember { mutableIntStateOf(5) }; Column { Box(Modifier.fillMaxWidth().height(90.dp).background(androidx.compose.ui.graphics.Color.Black)) { BarraProgresoScroll((p + 1f) / 20, color = MaterialTheme.colorScheme.primary) }; BotonExpresivo("Pasar página", Icons.Rounded.KeyboardArrowRight, { p = (p + 1) % 20 }) } },
        i("battery", "Battery Face + reloj", "opensourceui", "Hora y batería en el lector cuando ocultas el menú") { Box(Modifier.background(androidx.compose.ui.graphics.Color.Black).padding(12.dp)) { RelojBateria(Modifier.pildoraCristal(CircleShape, sombra = false).padding(horizontal = 10.dp, vertical = 4.dp), color = TintaBarra) } },
        i("confetti", "Confetti", "Magic UI", "Confeti al llegar a la última página de un capítulo") { val c = rememberConfeti(); Box(Modifier.fillMaxWidth().height(160.dp)) { BotonExpresivo("Terminar capítulo", Icons.Rounded.Check, { c.disparar() }, Modifier.align(Alignment.Center)); Confeti(c, Modifier.matchParentSize()) } },
        i("lens", "Lens", "Magic UI", "Lupa en el lector: mantén pulsado y mueve el dedo") { Box(Modifier.fx("lens") { lupa() }) { Portada(Modifier.width(200.dp)) } },
        // ── Navegación
        i("cmd", "Command Menu 01–04", "blocks.so", "En Más aparece «Comandos»: escribe y salta a cualquier pantalla") { var v by remember { mutableStateOf(false) }; BotonExpresivo("Abrir comandos", Icons.Rounded.Search, { v = true }); MenuComandos(v, comandosShiori(onRuta), { v = false }, "Buscar una pantalla o acción…") },
        i("onboarding", "Onboarding 01–07", "blocks.so", "Bienvenida de 3 pasos la próxima vez que abras Shiori") { var v by remember { mutableStateOf(false) }; BotonExpresivo("Ver la bienvenida", Icons.Rounded.PlayArrow, { v = true })
            if (v) androidx.compose.ui.window.Dialog({ v = false }, androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) { BienvenidaShiori { v = false } } },
    )
}

/** Ids del catálogo (para contar las activas sin construir las vistas). */
val IdsFxShiori = listOf("blurfade", "themetoggle", "noise", "ripple", "switch", "checkbox", "segmented", "empty", "skeleton", "spinner", "dialogs", "sheet",
    "progress", "searchinput", "textmorph", "comictext", "pulsating", "tilt", "glow", "magiccard", "glare", "backlight", "marquee", "carousel",
    "stats", "numberticker", "activity", "circularprogress", "nf_page", "scrollprogress", "battery", "confetti", "lens", "cmd", "onboarding")
