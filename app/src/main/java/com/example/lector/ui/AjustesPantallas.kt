package com.example.lector.ui

import kotlin.math.roundToInt

import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import com.example.lector.TareaCopia
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.Formatter
import android.webkit.CookieManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lector.Almacen
import com.example.lector.Anilist
import com.example.lector.CopiaSeguridad
import com.example.lector.ModoTema
import com.example.lector.Paletas
import com.example.lector.Programador
import com.example.lector.Vista
import com.example.lector.VistaExplorar
import com.example.lector.colorDePaleta
import com.example.lector.ext.ExtManager
import com.example.lector.ext.bandera
import com.example.lector.ext.nombreIdioma
import com.example.lector.nombreVista
import com.example.lector.textoSobre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Ajustes: la raíz de ajustes : cabecera con la marca de la app,
 * una línea fina y cada apartado como una burbuja (CategoryBubble). El buscador es el de las subpáginas  y,
 * al escribir, los apartados dejan paso a los ajustes que coinciden.
 */
@Composable
fun AjustesScreen(almacen: Almacen, onBack: () -> Unit, onRuta: (Ruta) -> Unit) {
    var q by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    val ctx = LocalContext.current
    val version = remember { runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "?" }
    val texto = q.trim()
    val hallados = remember(texto) {
        if (texto.isEmpty()) emptyList() else {
            val norm = { t: String -> java.text.Normalizer.normalize(t.lowercase(), java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}"), "") }
            val qn = norm(texto)
            AJUSTES_BUSCABLES.filter { (t, d, r) ->
                norm(t).contains(qn) || norm(d).contains(qn) || APARTADOS_AJUSTES.firstOrNull { it.ruta == r }?.let { norm(it.titulo).contains(qn) } == true
            }
        }
    }
    PaginaSilencio("Ajustes", onBack) {
        Box(Modifier.padding(horizontal = MargenLateral, vertical = 6.dp)) { CampoBusqueda(q, { q = it }, "Buscar en ajustes") }
        if (texto.isNotEmpty()) {
            if (hallados.isEmpty()) LText("Ningún ajuste coincide con «$texto».", EstiloCuerpo, Modifier.padding(MargenLateral, 16.dp), color = LectorColors.Muted)
            if (hallados.isNotEmpty()) GrupoFilas {
                hallados.forEach { (t, _, r) ->
                    val ap = APARTADOS_AJUSTES.firstOrNull { it.ruta == r }
                    FilaSilencio(ap?.icono ?: Icons.Rounded.Settings, t, ap?.titulo, { onRuta(r) })
                }
            }
            return@PaginaSilencio
        }
        // Tarjeta de la app: logo, nombre y versión.
        Row(
            Modifier.padding(horizontal = MargenLateral, vertical = 10.dp).fillMaxWidth().clip(SuaveShape(16.dp))
                .background(LectorColors.Card).borde(16.dp).clickable { onRuta(Ruta.AcercaDe) }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogoApp(Modifier.size(44.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LText("Shiori", EstiloFilaTitulo.copy(fontSize = 17.sp))
                LText("versión $version", EstiloDatos, color = LectorColors.Muted)
            }
            LText("›", LectorType.Body.copy(fontSize = 20.sp), color = LectorColors.Muted)
        }
        TituloSeccion("General")
        GrupoFilas(Modifier.entrada(1)) { APARTADOS_AJUSTES.forEach { a -> FilaSilencio(a.icono, a.titulo, a.detalle, { onRuta(a.ruta) }) } }
    }
}

private class Apartado(val icono: androidx.compose.ui.graphics.vector.ImageVector, val titulo: String, val ruta: Ruta, val detalle: String? = null)

private val APARTADOS_AJUSTES = listOf(
    Apartado(Icons.Rounded.Create, "Aspecto", Ruta.Apariencia, "Tema, acento, portadas, gestos y animaciones"),
    Apartado(Icons.Rounded.List, "Biblioteca", Ruta.AjBiblioteca),
    Apartado(Icons.Rounded.PlayArrow, "Visor", Ruta.AjVisor),
    Apartado(Iconos.Descargar, "Descargas", Ruta.AjDescargas),
    Apartado(Icons.Rounded.Refresh, "Seguimiento", Ruta.AjSeguimiento),
    Apartado(Icons.Rounded.Search, "Explorar", Ruta.AjExplorar),
    Apartado(Icons.Rounded.Share, "Datos y almacenamiento", Ruta.Datos),
    Apartado(Icons.Rounded.Lock, "Seguridad y privacidad", Ruta.AjSeguridad),
    Apartado(Icons.Rounded.Build, "Avanzado", Ruta.AjAvanzado),
    Apartado(Icons.Rounded.Info, "Acerca de", Ruta.AcercaDe),
)

/** Lo que se puede buscar en Ajustes: (título, palabras clave, apartado donde está). */
private val AJUSTES_BUSCABLES: List<Triple<String, String, Ruta>> = listOf(
    Triple("Tema", "claro oscuro negro puro sistema", Ruta.Apariencia),
    Triple("Color de acento", "color tema portada", Ruta.Apariencia),
    Triple("Animación al abrir", "agujero negro arranque inicio", Ruta.Apariencia),
    Triple("Gestos de capítulos", "deslizar leído marcar descargar", Ruta.Apariencia),
    Triple("Redondeo de portadas", "esquinas portada", Ruta.Apariencia),
    Triple("Animaciones", "velocidad quitar animaciones movimiento", Ruta.Apariencia),
    Triple("Intensidad del vidrio", "vidrio transparencia cristal claro tintado", Ruta.Apariencia),
    Triple("Efecto luminoso", "luz brillo halo bordes", Ruta.Apariencia),
    Triple("Vista de la biblioteca", "cuadrícula lista columnas portadas", Ruta.AjBiblioteca),
    Triple("Categorías", "categoría ordenar ocultas", Ruta.AjBiblioteca),
    Triple("Actualizar biblioteca", "actualizaciones automáticas novedades capítulos nuevos", Ruta.AjBiblioteca),
    Triple("Modo de lectura", "paginado tira webtoon continuo derecha izquierda vertical", Ruta.AjVisor),
    Triple("Zonas de toque", "tocar pasar página toque", Ruta.AjVisor),
    Triple("Zoom", "doble toque ampliar", Ruta.AjVisor),
    Triple("Filtros de color", "tinte sepia luz cálida brillo", Ruta.AjVisor),
    Triple("Precarga de páginas", "precargar páginas siguiente capítulo", Ruta.AjVisor),
    Triple("Teclas de volumen", "volumen pasar página", Ruta.AjVisor),
    Triple("Pasar de capítulo solo", "continuo transición siguiente capítulo", Ruta.AjVisor),
    Triple("Solo con Wi‑Fi", "wifi datos móviles descargas", Ruta.AjDescargas),
    Triple("Borrar al leer", "borrar descargas leídos", Ruta.AjDescargas),
    Triple("Descargar por adelantado", "descarga automática siguientes", Ruta.AjDescargas),
    Triple("AniList", "seguimiento sincronizar progreso", Ruta.AjSeguimiento),
    Triple("MyAnimeList, Kitsu y MangaUpdates", "mal kitsu mangaupdates seguimiento", Ruta.AjSeguimiento),
    Triple("Contenido +18", "nsfw adultos", Ruta.AjExplorar),
    Triple("Idiomas", "idioma lenguaje fuentes", Ruta.AjExplorar),
    Triple("Listados de extensiones", "repositorios repos extensiones", Ruta.AjExplorar),
    Triple("Copias de seguridad", "backup copia restaurar mihon komikku tachiyomi", Ruta.Datos),
    Triple("Carpeta de la app", "almacenamiento carpeta descargas ubicación", Ruta.Datos),
    Triple("Borrar cachés", "caché memoria espacio", Ruta.Datos),
    Triple("Bloqueo de la app", "huella bloqueo seguridad pin", Ruta.AjSeguridad),
    Triple("Modo incógnito", "incógnito historial privacidad", Ruta.AjSeguridad),
    Triple("Modo SFW", "sfw nsfw +18 adultos ocultar contenido", Ruta.AjSeguridad),
    Triple("Modo NSFW", "nsfw +18 adultos solo contenido", Ruta.AjSeguridad),
    Triple("Pantalla segura", "capturas pantalla segura", Ruta.AjSeguridad),
    Triple("DNS sobre HTTPS", "dns doh cloudflare google bloqueo red", Ruta.AjAvanzado),
    Triple("Cookies", "cookies webview borrar", Ruta.AjAvanzado),
    Triple("Batería", "batería optimización segundo plano", Ruta.AjAvanzado),
    Triple("Versión y actualizaciones", "versión actualizar app novedades", Ruta.AcercaDe),
)



// ───────────────────── Apariencia ─────────────────────

private fun nombreTema(m: ModoTema) = when (m) {
    ModoTema.SISTEMA -> "Sistema"
    ModoTema.OSCURO_PURO -> "Negro"
    ModoTema.OSCURO -> "Grafito"
    ModoTema.CLARO -> "Claro"
}

/** Aspecto: tema, acento, portadas, biblioteca, gestos y animaciones. Cada elección se ve en su muestra. */
@Composable
fun AparienciaScreen(almacen: Almacen, onBack: () -> Unit) {
    val opciones = Paletas.fijas.map { it.nombre to it.color } +
        if (Build.VERSION.SDK_INT >= 31) listOf(Paletas.SISTEMA to colorDePaleta(Paletas.SISTEMA)) else emptyList()
    val acento = androidx.compose.material3.MaterialTheme.colorScheme.primary
    PaginaAjustes("Aspecto", onBack) {
        TituloSeccion("Tema")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = MargenLateral), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModoTema.entries.forEach { m ->
                val (fondo, tarjeta, texto) = when (m) {
                    ModoTema.CLARO -> Triple(Color.White, Color(0xFFF4F4F5), Color(0xFF111113))
                    ModoTema.OSCURO -> Triple(Color(0xFF1E1E1E), Color(0xFF262626), Color(0xFFDCDDDE))
                    ModoTema.OSCURO_PURO -> Triple(Color.Black, Color(0xFF141414), Color.White)
                    ModoTema.SISTEMA -> Triple(Color(0xFF6B6B73), Color(0xFFB0B0B8), Color.White)
                }
                TarjetaTema(nombreTema(m), acento, almacen.modoTema == m, fondo, tarjeta, texto) { almacen.modoTema = m }
            }
        }
        LText("«Grafito» es el oscuro de Silencio, con líneas finas como Obsidian; «Negro» ahorra batería en pantallas OLED.", LectorType.Label, Modifier.padding(MargenLateral, 10.dp), color = LectorColors.Muted)
        TituloSeccion("Acento")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = MargenLateral, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            opciones.forEach { (nombre, color) ->
                val elegido = almacen.paleta == nombre
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp).pressable { almacen.elegirPaleta(nombre) }) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(color)
                            .border(if (elegido) 3.dp else 0.6.dp, if (elegido) LectorColors.Text else LectorColors.Linea, CircleShape),
                        Alignment.Center,
                    ) { if (elegido) Icon(Icons.Rounded.Check, null, tint = textoSobre(color), modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.height(6.dp))
                    LText(if (nombre == Paletas.MONO) "Mono" else nombre, EstiloMono, color = if (elegido) LectorColors.Text else LectorColors.Muted, maxLines = 1)
                }
            }
        }
        FilaSwitch("Color de la portada", "La ficha y el lector toman el color de la portada.", almacen.colorPortada) { almacen.elegirColorPortada(it) }
        TituloSeccion("Vidrio y luz")
        FilaSilencio(null, "Intensidad del vidrio", when { Aspecto.vidrio < 0.25f -> "Tintado"; Aspecto.vidrio > 0.75f -> "Claro"; else -> "Normal" }, null)
        Box(Modifier.padding(horizontal = MargenLateral)) {
            DeslizadorExpresivo(Aspecto.vidrio, 0f..1f, { Aspecto.elegirVidrio(it) })
        }
        LText("Cuánto deja ver la barra, el mini lector y las barras de arriba lo que hay detrás.", LectorType.Label, Modifier.padding(MargenLateral, 6.dp), color = LectorColors.Muted)
        FilaSwitch("Efecto luminoso", "Bordes de luz en esferas y cápsulas, halo en el progreso y en la pestaña activa.", Aspecto.luminoso) { Aspecto.elegirLuminoso(it) }
        TituloSeccion("Portadas")
        FilaSilencio(null, "Redondeo", "${Aspecto.radioPortada.toInt()} dp", null, final = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(26.dp, 38.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape((Aspecto.radioPortada / 3f).dp)).background(acento.copy(alpha = 0.5f)))
            }
        })
        Box(Modifier.padding(horizontal = MargenLateral)) {
            DeslizadorExpresivo(Aspecto.radioPortada, 0f..28f, { Aspecto.elegirRadioPortada(it.roundToInt().toFloat()) })
        }
        FilaSwitch("Portadas panorámicas", "En Historial y Novedades, las portadas apaisadas se ven a lo ancho.", Aspecto.panoramica) { Aspecto.elegirPanoramica(it) }
        TituloSeccion("Biblioteca")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = MargenLateral, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Vista.CUADRICULA_COMODA to "Cómoda", Vista.CUADRICULA_COMPACTA to "Compacta", Vista.SOLO_PORTADA to "Portadas", Vista.LISTA to "Lista").forEach { (v, n) ->
                ChipSel(n, almacen.vista == v) { almacen.elegirVista(v) }
            }
        }
        FilaSilencio(null, "Mantener pulsada una portada", "Hoja: menú desde abajo · Elevado: la portada se levanta y el fondo se desenfoca · Directo: añade o quita sin menú", null)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = MargenLateral, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Aspecto.Mantener.entries.forEach { m -> ChipSel(m.nombre, Aspecto.mantener == m) { Aspecto.elegir(m) } }
        }
        TituloSeccion("Gestos de capítulos")
        val ctx = LocalContext.current
        LaunchedEffect(Unit) { GestosCapitulos.iniciar(ctx) }
        FilaGesto("Deslizar a la derecha", GestosCapitulos.derecha) { GestosCapitulos.elegirDerecha(it) }
        FilaGesto("Deslizar a la izquierda", GestosCapitulos.izquierda) { GestosCapitulos.elegirIzquierda(it) }
        TituloSeccion("Movimiento")
        FilaSwitch("Animaciones", "Apagadas, todo cambia al instante (también ahorra batería).", MovimientoApp.activado) { MovimientoApp.elegirActivado(it) }
        if (MovimientoApp.activado) Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = MargenLateral, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            VelocidadesAnimacion.forEach { v -> ChipSel(textoVelocidad(v), MovimientoApp.velocidad == v) { MovimientoApp.elegirVelocidad(v) } }
        }
        TituloSeccion("Al abrir")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = MargenLateral, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AnimArranque.entries.forEach { a -> ChipSel(a.nombre, Aspecto.arranque == a) { Aspecto.elegir(a) } }
        }
        Box(Modifier.padding(horizontal = MargenLateral, vertical = 10.dp)) {
            PillButton("Ver animación", Icons.Rounded.Refresh, { ArranqueRevelado.repetir++ })
        }
    }
}

@Composable
private fun FilaGesto(titulo: String, actual: AccionGesto, alElegir: (AccionGesto) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        FilaSilencio(actual.icono, titulo, actual.titulo, { abierto = true })
        MenuShiori(expanded = abierto, onDismissRequest = { abierto = false }) {
            AccionGesto.entries.forEach { a ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(a.titulo) }, leadingIcon = { Icon(a.icono, null, tint = a.color) },
                    onClick = { abierto = false; alElegir(a) },
                )
            }
        }
    }
}

/** Mini maqueta del tema: fondo, una tarjeta con línea fina y el acento. */
@Composable
private fun TarjetaTema(
    nombre: String, color: Color, seleccionada: Boolean, fondo: Color, tarjeta: Color, texto: Color, onClick: () -> Unit,
) {
    val forma = SuaveShape(16.dp)
    Column(Modifier.width(92.dp).pressable(onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().height(132.dp).clip(forma).background(fondo)
                .border(if (seleccionada) 2.5.dp else 0.6.dp, if (seleccionada) LectorColors.Text else LectorColors.Linea, forma),
        ) {
            Column(Modifier.padding(10.dp)) {
                Box(Modifier.size(38.dp, 8.dp).clip(CircleShape).background(texto))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(2) { Box(Modifier.size(30.dp, 44.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp)).background(tarjeta)) }
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.size(48.dp, 6.dp).clip(CircleShape).background(texto.copy(alpha = 0.4f)))
            }
            Box(Modifier.align(Alignment.BottomEnd).padding(8.dp).size(16.dp).clip(CircleShape).background(color))
        }
        Spacer(Modifier.height(8.dp))
        LText(nombre, EstiloMono, color = if (seleccionada) LectorColors.Text else LectorColors.Muted, maxLines = 1)
    }
}

// ───────────────────── Biblioteca y actualizaciones ─────────────────────

@Composable
fun AjBibliotecaScreen(almacen: Almacen, onBack: () -> Unit, onRuta: (Ruta) -> Unit) {
    val ctx = LocalContext.current
    fun reprogramar() = Programador.programar(ctx)
    PaginaAjustes("Biblioteca", onBack) {
        TituloSeccion("Vista")
        Vista.entries.forEach { v -> FilaRadio(nombreVista(v), almacen.vista == v) { almacen.elegirVista(v) } }
        if (almacen.vista != Vista.LISTA) {
            TituloSeccion("Columnas")
            FilaChips(listOf("Auto", "2", "3", "4", "5", "6"), listOf(0, 2, 3, 4, 5, 6).indexOf(almacen.columnas).coerceAtLeast(0)) {
                almacen.elegirColumnas(listOf(0, 2, 3, 4, 5, 6)[it])
            }
        }
        FilaAjuste(Icons.Rounded.Menu, "Categorías", "Crear, ordenar, ocultar y renombrar") { onRuta(Ruta.Categorias) }
        run {
            val cats = almacen.categorias()
            val opciones = listOf("" to "Preguntar siempre", "-" to "Ninguna, sin preguntar") + cats.map { it.id to it.nombre }
            val i = opciones.indexOfFirst { it.first == almacen.catDefecto }.coerceAtLeast(0)
            FilaAjuste(Icons.Rounded.Add, "Categoría por defecto al añadir", opciones[i].second + " · toca para cambiar") {
                almacen.catDefecto = opciones[(i + 1) % opciones.size].first
            }
        }

        Divisor()
        TituloSeccion("Buscar capítulos nuevos automáticamente")
        val horas = listOf(0, 6, 12, 24, 48, 168)
        FilaChips(listOf("A mano", "Cada 6 h", "Cada 12 h", "Diario", "Cada 2 días", "Semanal"), horas.indexOf(almacen.intervaloActualizacion).coerceAtLeast(0)) {
            almacen.intervaloActualizacion = horas[it]; reprogramar()
        }
        if (almacen.intervaloActualizacion > 0) {
            FilaSwitch("Solo con Wi‑Fi", null, almacen.actSoloWifi) { almacen.actSoloWifi = it; reprogramar() }
            FilaSwitch("Solo cargando", null, almacen.actSoloCargando) { almacen.actSoloCargando = it; reprogramar() }
        }
        FilaSwitch("Saltar los completados", "No revisa los mangas terminados y sin capítulos por leer", almacen.actSaltarTerminados) { almacen.actSaltarTerminados = it }
        FilaSwitch("Saltar los que tienen capítulos sin leer", "Solo revisa los mangas que llevas al día", almacen.actSaltarSinLeer) { almacen.actSaltarSinLeer = it }
        FilaSwitch("Revisión inteligente", "Solo revisa los mangas que esperan capítulo hoy, según su ritmo de publicación (como Komikku). Los parados se revisan cada vez menos", almacen.actInteligente) { almacen.actInteligente = it }
        FilaSwitch("Saltar los no empezados", "No revisa los mangas de los que aún no has leído nada", almacen.actSaltarNoEmpezados) { almacen.actSaltarNoEmpezados = it }
        FilaCategoriasExcluidas(almacen, "Categorías que no se revisan", almacen.actCatExcluidas) { almacen.actCatExcluidas = it }
        FilaSwitch("Marcar leído el capítulo repetido", "Si sale otra versión de un capítulo cuyo número ya leíste, queda leída", almacen.marcarDuplicadosNuevos) { almacen.marcarDuplicadosNuevos = it }
        FilaSwitch("Descargar los capítulos nuevos", "Los pone en la cola de descargas al encontrarlos", almacen.descargarNuevos) { almacen.descargarNuevos = it }
        if (almacen.descargarNuevos) FilaCategoriasExcluidas(almacen, "Categorías que no se descargan solas", almacen.descCatExcluidas) { almacen.descCatExcluidas = it }
        LText(
            "Las revisiones automáticas avisan con una notificación de los capítulos nuevos. Android puede retrasarlas para ahorrar batería.",
            LectorType.Label.copy(fontSize = 13.sp), Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = LectorColors.Muted,
        )
    }
}

// ───────────────────── Descargas ─────────────────────

@Composable
fun AjDescargasScreen(almacen: Almacen, onBack: () -> Unit, onRuta: (Ruta) -> Unit) {
    val ctx = LocalContext.current
    var confirmar by remember { mutableStateOf(false) }
    var version by remember { mutableIntStateOf(0) }
    val espacio = remember(version, almacen.version) { Formatter.formatShortFileSize(ctx, almacen.bytesDescargas()) }
    PaginaAjustes("Descargas", onBack) {
        FilaAjuste(Iconos.Descargar, "Cola de descargas", "${com.example.lector.Descargas.cola.size} pendientes") { onRuta(Ruta.Descargas) }
        Divisor()
        FilaSwitch("Solo con Wi‑Fi", "Las descargas esperan a tener Wi‑Fi", almacen.descSoloWifi) { almacen.descSoloWifi = it }
        TituloSeccion("Fuentes a la vez")
        FilaChips(listOf("1", "2", "3", "4"), (almacen.descParalelas - 1).coerceIn(0, 3)) { almacen.descParalelas = it + 1 }
        LText(
            "Como en Komikku: de cada fuente se baja un capítulo cada vez (3 páginas a la vez), para que la web no bloquee. Si una página falla, al reintentar solo se bajan las que faltan.",
            LectorType.Label, Modifier.padding(horizontal = 24.dp, vertical = 4.dp), color = LectorColors.Muted,
        )
        Divisor()
        TituloSeccion("Al leer")
        FilaSwitch("Borrar al terminar de leer", "Borra el capítulo descargado cuando lo terminas", almacen.borrarAlLeer) { almacen.borrarAlLeer = it }
        FilaSwitch("Borrar al marcar como leído", "También cuando lo marcas a mano desde la ficha", almacen.borrarAlMarcar) { almacen.borrarAlMarcar = it }
        if (almacen.borrarAlLeer || almacen.borrarAlMarcar) FilaSwitch("Borrar también los capítulos con marcador", null, almacen.borrarMarcados) { almacen.borrarMarcados = it }
        LText("Descargar por adelantado (mangas de la biblioteca)", LectorType.Body.copy(fontSize = 15.sp), Modifier.padding(start = 24.dp, top = 10.dp))
        val opciones = listOf(0, 1, 2, 3, 5, 10)
        FilaChips(listOf("No", "1", "2", "3", "5", "10"), opciones.indexOf(almacen.descargarAdelante).coerceAtLeast(0)) {
            almacen.descargarAdelante = opciones[it]
        }
        Divisor()
        TituloSeccion("Almacenamiento")
        FilaAjuste(Icons.Rounded.Delete, "Borrar todas las descargas", "Ocupan $espacio") { confirmar = true }
    }
    if (confirmar) {
        DialogoAlerta(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Borrar todas las descargas?") },
            text = { Text("Se borran los capítulos descargados. Podrás seguir leyéndolos online.") },
            confirmButton = {
                TextButton(onClick = {
                    almacen.cargar().filter { it.enLinea }.forEach { almacen.borrarDescargasDe(it.fuenteId, it.urlManga.orEmpty()) }
                    File(ctx.filesDir, "descargas").listFiles()?.forEach { it.delete() }
                    com.example.lector.Descargas.version.value++
                    version++
                    confirmar = false
                }) { Text("Borrar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
        )
    }
}

// ───────────────────── Seguimiento ─────────────────────

@Composable
fun AjSeguimientoScreen(almacen: Almacen, onBack: () -> Unit) {
    val contexto = LocalContext.current
    var clientId by remember { mutableStateOf(almacen.anilistClientId) }
    PaginaAjustes("Seguimiento", onBack) {
        FilaSwitch("Subir el progreso al marcar como leído", "También al marcar capítulos a mano, no solo al leerlos", almacen.segAlMarcar) { almacen.segAlMarcar = it }
        Divisor()
        TituloSeccion("Otros servicios")
        CuentasSeguimiento()
        Divisor()
        TituloSeccion("AniList")
        if (almacen.anilistToken != null) {
            FilaAjuste(Icons.Rounded.Person, "Conectado como ${almacen.anilistUsuario ?: "…"}", "AniList") {}
            LText(
                "En la ficha de un cómic o de un manga de tu biblioteca, toca «Seguimiento» para vincularlo con su serie. " +
                    "Los cómics suben el progreso al llegar a la última página; los mangas en línea, al terminar cada capítulo.",
                LectorType.Label.copy(fontSize = 13.sp), Modifier.padding(24.dp, 8.dp), color = LectorColors.Muted,
            )
            Box(Modifier.padding(24.dp, 8.dp)) { PillButton("Cerrar sesión", Icons.Rounded.Close, { almacen.cerrarSesionAnilist() }) }
        } else {
            LText(
                "1. En anilist.co → Settings → Developer, crea una aplicación con la URL de redirección  lector://anilist\n" +
                    "2. Pega aquí su Client ID (un número) y pulsa «Iniciar sesión».",
                LectorType.Label.copy(fontSize = 13.sp), Modifier.padding(24.dp, 8.dp), color = LectorColors.Muted,
            )
            CampoTextoShiori(
                value = clientId, onValueChange = { clientId = it.filter { c -> c.isDigit() } },
                singleLine = true, label = { Text("Client ID") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )
            Box(Modifier.padding(24.dp, 12.dp)) {
                PillButton("Iniciar sesión", Icons.Rounded.Person, {
                    if (clientId.isNotEmpty()) {
                        almacen.guardarClientId(clientId)
                        runCatching { Anilist.marcarLogin(contexto); contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Anilist.urlDeLogin(clientId)))) }
                    }
                })
            }
        }
    }
}

// ───────────────────── Explorar ─────────────────────

@Composable
fun AjExplorarScreen(almacen: Almacen, ext: ExtManager, onBack: () -> Unit, onRuta: (Ruta) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var idiomas by remember { mutableStateOf(false) }
    var revisando by remember { mutableStateOf(false) }
    val elegirApk = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val error = withContext(Dispatchers.IO) {
                val tmp = java.io.File(java.io.File(ctx.cacheDir, "ext").apply { mkdirs() }, "archivo.apk")
                runCatching {
                    ctx.contentResolver.openInputStream(uri)!!.use { e -> tmp.outputStream().use { e.copyTo(it) } }
                    ext.instalarPrivadaDe(tmp)
                }.getOrElse { it.message ?: "No se pudo leer el archivo" }.also { tmp.delete() }
            }
            Avisos.mostrar(error ?: "Extensión instalada. Si no aparece en Fuentes, pulsa «Confiar» en Extensiones")
        }
    }
    PaginaAjustes("Explorar", onBack) {
        FilaAjuste(Icons.Rounded.List, "Listados de extensiones", "${ext.repos.size} listados") { onRuta(Ruta.ListadosExt) }
        FilaAjuste(Icons.Rounded.Place, "Idiomas", ext.idiomas.take(8).joinToString(" ") { bandera(it) }) { idiomas = true }
        FilaSwitch("Mostrar contenido +18", "Fuentes y extensiones para adultos", ext.mostrarNsfw) { ext.alternarNsfw() }
        Divisor()
        TituloSeccion("Navegar una fuente")
        FilaChips(listOf("Cuadrícula", "Lista", "Compacta", "Solo portadas"), almacen.vistaExplorar.ordinal) { almacen.vistaExplorar = VistaExplorar.entries[it] }
        FilaSwitch("Esconder lo que ya está en la biblioteca", null, almacen.explorarEnBiblioteca == com.example.lector.Tri.EXCLUIR) {
            almacen.explorarEnBiblioteca = if (it) com.example.lector.Tri.EXCLUIR else com.example.lector.Tri.IGNORAR
        }
        FilaSwitch("Búsqueda global solo en fuentes ancladas", "Más rápida si tienes muchas fuentes", almacen.globalSoloAncladas) { almacen.globalSoloAncladas = it }
        Divisor()
        TituloSeccion("Extensiones")
        FilaSwitch("Avisar de actualizaciones de extensiones", "Revisa cada 12 horas", almacen.revisarExtensiones) {
            almacen.revisarExtensiones = it; Programador.programar(ctx)
        }
        FilaSwitch("Extensiones privadas", "Se guardan dentro de Shiori sin instalarse en Android: sin permisos ni confirmaciones", ext.extPrivadas) { ext.cambiarExtPrivadas(it) }
        FilaAjuste(Icons.Rounded.Add, "Instalar una extensión desde un archivo", "Un .apk de extensión; se instala como privada") { elegirApk.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream")) }
        FilaAjuste(Icons.Rounded.Refresh, if (revisando) "Revisando…" else "Buscar actualizaciones ahora", null) {
            if (!revisando) {
                revisando = true
                scope.launch {
                    withContext(Dispatchers.IO) { runCatching { ext.actualizarListaAhora() } }
                    val n = ext.conActualizacion().size
                    Avisos.mostrar(if (n == 0) "Todas las extensiones están al día" else "$n extensión(es) con actualización")
                    revisando = false
                }
            }
        }
    }
    if (idiomas) {
        val todos = (ext.disponibles.map { it.lang } + ext.instaladas.map { it.lang } + ext.idiomas + "all")
            .filter { it.isNotBlank() }.distinct().sortedBy { nombreIdioma(it) }
        DialogoAlerta(
            onDismissRequest = { idiomas = false },
            title = { Text("Idiomas") },
            text = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                    todos.forEach { l ->
                        Row(Modifier.fillMaxWidth().pressable { ext.alternarIdioma(l) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            CasillaShiori(checked = l in ext.idiomas, onCheckedChange = null)
                            Spacer(Modifier.width(12.dp))
                            Text("${bandera(l)}  ${nombreIdioma(l)}")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { idiomas = false }) { Text("Listo") } },
            dismissButton = { TextButton(onClick = { ext.activarTodosIdiomas() }) { Text("Todos") } },
        )
    }
}

// ───────────────────── Datos y almacenamiento ─────────────────────

@Composable
fun DatosScreen(almacen: Almacen, alRecargar: () -> Unit, onBack: () -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var resultado by remember { mutableStateOf<String?>(null) }
    var version by remember { mutableIntStateOf(0) }

    // Carpeta de la app (descargas y copias), como «Ubicación de almacenamiento» de Komikku.
    var rutaCarpeta by remember { mutableStateOf(com.example.lector.CarpetaApp.ruta(contexto)) }
    val elegirCarpetaApp = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val ruta = uri?.let { com.example.lector.CarpetaApp.rutaDeArbol(it) } ?: return@rememberLauncherForActivityResult
        scope.launch {
            val movidas = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.lector.CarpetaApp.cambiar(contexto, ruta).also { almacen.recargarCarpeta() }
            }
            rutaCarpeta = ruta
            Tacto.confirmar()
            Avisos.mostrar(if (movidas > 0) "Carpeta lista: $movidas descargas movidas" else "Carpeta lista")
        }
    }

    // Crear y restaurar van en segundo plano con avance (píldora arriba + notificación) e informe al terminar.
    fun ocupado() = Avisos.mostrar("Ya hay una copia en marcha: espera a que termine")
    val crear = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null && !TareaCopia.lanzar(contexto, "Creando copia") {
                val (texto, informe) = CopiaSeguridad.crearConInforme(contexto, this)
                contexto.contentResolver.openOutputStream(uri)?.use { it.write(texto.toByteArray(Charsets.UTF_8)) } ?: error("No se pudo escribir el archivo")
                informe
            }
        ) ocupado()
    }
    val restaurar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && !TareaCopia.lanzar(contexto, "Restaurando copia") {
                paso(0.02f, "Abriendo el archivo…")
                val bytes = contexto.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("No se pudo leer el archivo")
                if (com.example.lector.Tachibk.esTachibk(bytes)) {
                    // Copia de Mihon / Komikku / TachiyomiSY / J2K (.tachibk o .proto.gz).
                    com.example.lector.Tachibk.restaurar(contexto, bytes, this)
                } else {
                    val texto = com.example.lector.Tachibk.descomprimirSiHace(bytes).toString(Charsets.UTF_8)
                    runCatching { CopiaSeguridad.restaurarConInforme(contexto, texto, this) }
                        .getOrElse { throw IllegalStateException("El archivo no es una copia válida (${it.message ?: it.javaClass.simpleName})") }
                }
            }
        ) ocupado()
    }
    // Copia en el formato de Mihon / Komikku (.tachibk): para pasar la biblioteca a esas apps.
    val crearCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val fuentes = com.example.lector.ext.ExtManager.obtener(contexto).fuentes.associate { it.id to it.nombre }
                    val texto = almacen.bibliotecaCsv { id -> fuentes[id] ?: "Fuente $id" }
                    contexto.contentResolver.openOutputStream(uri)!!.use { it.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())); it.write(texto.toByteArray()) }
                }.isSuccess
            }
            Avisos.mostrar(if (ok) "Lista exportada" else "No se pudo exportar")
        }
    }
    val crearTachibk = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null && !TareaCopia.lanzar(contexto, "Creando copia .tachibk") {
                val informe = contexto.contentResolver.openOutputStream(uri)?.use { com.example.lector.Tachibk.crear(contexto, it, this) }
                    ?: error("No se pudo escribir el archivo")
                informe.copy(bien = informe.bien + "Restáurala en Mihon, Komikku o TachiyomiSY (Ajustes → Copia de seguridad → Restaurar)")
            }
        ) ocupado()
    }
    val elegirCarpeta = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                contexto.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            almacen.copiaCarpeta = uri.toString()
        }
    }

    // Copias en WebDAV: subpantalla propia, sin ruta nueva.
    var verWebdav by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    if (verWebdav) { com.example.lector.ui.herramientas.WebdavScreen { verWebdav = false }; return }

    val tamImagenes = remember(version) { Formatter.formatShortFileSize(contexto, Imagenes.tamanoDisco()) }
    PaginaAjustes("Datos y almacenamiento", onBack) {
        TituloSeccion("Carpeta de la app")
        val permiso = com.example.lector.CarpetaApp.hayPermiso(contexto)
        FilaAjuste(
            Icons.Rounded.Add,
            if (rutaCarpeta == null) "Elegir carpeta" else rutaCarpeta!!.substringAfterLast('/').ifBlank { rutaCarpeta!! },
            when {
                rutaCarpeta == null -> "Ahora se guarda dentro de la app (no se ve). Elige o crea una carpeta para las descargas y las copias."
                !permiso -> "Falta el permiso de acceso a archivos: toca para darlo"
                else -> "$rutaCarpeta · dentro: descargas y copias"
            },
        ) {
            if (!com.example.lector.CarpetaApp.hayPermiso(contexto)) {
                Avisos.mostrar("Activa «Permitir acceso para gestionar todos los archivos» y vuelve")
                com.example.lector.CarpetaApp.pedirPermiso(contexto)
            } else elegirCarpetaApp.launch(null)
        }
        Divisor()
        TituloSeccion("Copia de seguridad")
        LText(
            "Guarda tu biblioteca (también la de Explorar), categorías, progreso, historial, capítulos leídos y marcados, " +
                "listados de extensiones, búsquedas guardadas y ajustes. No incluye los archivos de los cómics ni las descargas.",
            LectorType.Label.copy(fontSize = 13.sp), Modifier.padding(horizontal = 24.dp), color = LectorColors.Muted,
        )
        Spacer(Modifier.height(8.dp))
        FilaAjuste(Icons.Rounded.Share, "Crear copia", "Guardar en un archivo .json") { crear.launch(CopiaSeguridad.nombreArchivo()) }
        FilaAjuste(Icons.Rounded.Refresh, "Restaurar copia", "De Lector (.json) o de Mihon, Komikku, TachiyomiSY… (.tachibk)") { restaurar.launch(arrayOf("*/*")) }
        FilaAjuste(Icons.Rounded.List, "Exportar la lista de la biblioteca", "Archivo .csv con título, autor, fuente y progreso") { crearCsv.launch("biblioteca-shiori.csv") }
        FilaAjuste(Icons.Rounded.Share, "Exportar para Mihon / Komikku", "Archivo .tachibk con tus mangas de fuentes, capítulos leídos, categorías e historial") {
            crearTachibk.launch(com.example.lector.Tachibk.nombreArchivo())
        }
        FilaAjuste(
            Icons.Rounded.Refresh, "Copias en WebDAV",
            com.example.lector.herramientas.WebDavAjustes(contexto).let { w ->
                if (w.configurado) "${com.example.lector.herramientas.WebDavClient.hostOf(w.url)}${if (w.autoSubir) " · sube las automáticas" else ""}"
                else "Nextcloud, ownCloud, NAS…: subir y restaurar copias"
            },
        ) { verWebdav = true }

        Divisor()
        TituloSeccion("Copias automáticas")
        val dias = listOf(0, 1, 2, 3, 7)
        FilaChips(listOf("No", "Diaria", "Cada 2 días", "Cada 3 días", "Semanal"), dias.indexOf(almacen.copiaAutoDias).coerceAtLeast(0)) {
            almacen.copiaAutoDias = dias[it]; Programador.programar(contexto)
        }
        if (almacen.copiaAutoDias > 0) {
            FilaAjuste(
                Icons.Rounded.Home, "Carpeta de las copias",
                if (almacen.copiaCarpeta.isBlank()) "Interna: ${CopiaSeguridad.carpetaInterna(contexto).path}" else "Una carpeta que elegiste",
            ) { elegirCarpeta.launch(null) }
            LText("Copias que se guardan", LectorType.Body.copy(fontSize = 15.sp), Modifier.padding(start = 24.dp, top = 10.dp))
            val maximos = listOf(2, 5, 10, 20)
            FilaChips(maximos.map { "$it" }, maximos.indexOf(almacen.copiasMax).let { if (it < 0) 1 else it }) { almacen.copiasMax = maximos[it] }
        }
        FilaAjuste(
            Icons.Rounded.Check, "Hacer una copia ahora",
            if (almacen.ultimaCopia > 0) "Última automática: ${hace(almacen.ultimaCopia)}" else "En la carpeta de las copias automáticas",
        ) {
            if (!TareaCopia.lanzar(contexto, "Creando copia") {
                    paso(0.2f, "Reuniendo la biblioteca…")
                    val donde = CopiaSeguridad.guardarAutomatica(contexto)
                    TareaCopia.Informe("Copia de seguridad", listOf("Copia guardada en $donde"))
                }
            ) ocupado()
        }

        Divisor()
        TituloSeccion("Almacenamiento")
        FilaAjuste(Icons.Rounded.Delete, "Limpiar datos de mangas que no tienes", "Fichas y listas de capítulos guardadas de mangas que no están en la biblioteca ni en el historial") {
            scope.launch {
                val n = withContext(Dispatchers.IO) { almacen.limpiarHuerfanos() }
                Avisos.mostrar(if (n == 0) "No había nada que limpiar" else "$n archivos borrados")
            }
        }
        FilaAjuste(Icons.Rounded.Delete, "Vaciar la caché de imágenes", "Portadas guardadas: $tamImagenes") {
            scope.launch {
                withContext(Dispatchers.IO) { Imagenes.vaciar() }
                CacheVisor.vaciar()
                version++
                Avisos.mostrar("Caché vaciada")
            }
        }
        FilaAjuste(Icons.Rounded.Delete, "Vaciar la caché de páginas del visor", "Las páginas leídas que siguen en memoria") {
            CacheVisor.vaciar(); Avisos.mostrar("Caché del visor vaciada")
        }
    }
    resultado?.let {
        DialogoAlerta(
            onDismissRequest = { resultado = null },
            title = { Text("Copia de seguridad") },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = { resultado = null }) { Text("Aceptar") } },
        )
    }
}

// ───────────────────── Seguridad y privacidad ─────────────────────

@Composable
fun AjSeguridadScreen(almacen: Almacen, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val seguro = remember { (ctx.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)?.isDeviceSecure == true }
    PaginaAjustes("Seguridad y privacidad", onBack) {
        TituloSeccion("Bloqueo")
        FilaSwitch(
            "Pedir desbloqueo",
            if (seguro) "Con el PIN, patrón, contraseña o huella del teléfono" else "Primero configura un bloqueo de pantalla en Android",
            almacen.bloqueo && seguro,
        ) { if (seguro) almacen.bloqueo = it else Avisos.mostrar("Configura un bloqueo de pantalla en los ajustes de Android") }
        if (almacen.bloqueo) {
            LText("Pedirlo al volver a la app", LectorType.Body.copy(fontSize = 15.sp), Modifier.padding(start = 24.dp, top = 10.dp))
            val minutos = listOf(0, 1, 2, 5, 10, 30)
            FilaChips(listOf("Siempre", "1 min", "2 min", "5 min", "10 min", "30 min"), minutos.indexOf(almacen.bloqueoMinutos).coerceAtLeast(0)) {
                almacen.bloqueoMinutos = minutos[it]
            }
        }
        Divisor()
        TituloSeccion("Privacidad")
        FilaSwitch("Ocultar el contenido de las notificaciones", "No se ven en la pantalla de bloqueo", almacen.ocultarNotificaciones) { almacen.ocultarNotificaciones = it }
        FilaSwitch("Pantalla segura", "Oculta la app en la lista de recientes e impide capturas", almacen.pantallaSegura) { almacen.pantallaSegura = it }
        FilaSwitch("Modo incógnito", "No se anota nada en el historial mientras esté activo", almacen.incognito) { almacen.incognito = it }
        val modoC = com.example.lector.ModoContenido.modo
        FilaSwitch("Modo SFW", "Oculta todo lo +18: extensiones, fuentes y mangas de adultos en toda la app", modoC == com.example.lector.ModoContenido.Modo.SFW) {
            com.example.lector.ModoContenido.fijar(if (it) com.example.lector.ModoContenido.Modo.SFW else com.example.lector.ModoContenido.Modo.NORMAL)
        }
        FilaSwitch("Modo NSFW", "Muestra solo contenido +18", modoC == com.example.lector.ModoContenido.Modo.NSFW) {
            com.example.lector.ModoContenido.fijar(if (it) com.example.lector.ModoContenido.Modo.NSFW else com.example.lector.ModoContenido.Modo.NORMAL)
        }
    }
}

// ───────────────────── Avanzado ─────────────────────

@Composable
fun AjAvanzadoScreen(almacen: Almacen, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var informe by remember { mutableStateOf<String?>(null) }
    val ext = remember { com.example.lector.ext.ExtManager.obtener(ctx) }
    val scope = rememberCoroutineScope()
    fun escanear() {
        ext.refrescarInstaladas()
        scope.launch {
            kotlinx.coroutines.delay(2500) // que termine de leer y cargar las extensiones
            informe = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { ext.informe() }
        }
    }
    informe?.let { texto ->
        DialogoAlerta(
            onDismissRequest = { informe = null },
            title = { androidx.compose.material3.Text("Diagnóstico de extensiones") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    LText(texto, LectorType.Label)
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val cm = ctx.getSystemService(android.content.ClipboardManager::class.java)
                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("Diagnóstico Shiori", texto))
                    Avisos.mostrar("Informe copiado: pégalo en el chat")
                }) { androidx.compose.material3.Text("Copiar") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { escanear() }) { androidx.compose.material3.Text("Volver a escanear") } },
        )
    }
    PaginaAjustes("Avanzado", onBack) {
        TituloSeccion("Extensiones")
        FilaAjuste(Icons.Rounded.Delete, "Revocar la confianza en extensiones desconocidas", "Las que no vienen de tus listados vuelven a pedir «Confiar»") {
            com.example.lector.ext.ExtManager.obtener(ctx).revocarConfianza(); Avisos.mostrar("Confianza revocada")
        }
        FilaAjuste(Icons.Rounded.Refresh, "Diagnóstico de extensiones", "Escanea las extensiones instaladas y dice por qué falta alguna. Se puede copiar para enviarlo.") {
            Avisos.mostrar("Escaneando extensiones…")
            escanear()
        }
        Divisor()
        TituloSeccion("Red")
        var ahorro by remember { mutableStateOf(ctx.getSharedPreferences("lector", android.content.Context.MODE_PRIVATE).getBoolean("ahorro_datos", false)) }
        FilaSwitch("Ahorro de datos", "Las páginas se piden recomprimidas (WebP, 1080 px) a wsrv.nl, como en Komikku. Si una web no lo permite, se usa la imagen normal.", ahorro) {
            ahorro = it
            ctx.getSharedPreferences("lector", android.content.Context.MODE_PRIVATE).edit().putBoolean("ahorro_datos", it).apply()
        }
        var uaAbierto by remember { mutableStateOf(false) }
        val prefsUa = remember { ctx.getSharedPreferences("lector", android.content.Context.MODE_PRIVATE) }
        var ua by remember { mutableStateOf(prefsUa.getString("user_agent", "").orEmpty()) }
        FilaAjuste(Icons.Rounded.Edit, "Agente de usuario", if (ua.isBlank()) "El de fábrica (Chrome en Android)" else ua) { uaAbierto = true }
        if (uaAbierto) {
            var texto by remember { mutableStateOf(ua) }
            DialogoAlerta(
                onDismissRequest = { uaAbierto = false },
                title = { Text("Agente de usuario") },
                text = {
                    androidx.compose.material3.OutlinedTextField(
                        texto, { texto = it }, placeholder = { Text("Vacío = el de fábrica") }, maxLines = 4,
                    )
                },
                dismissButton = { TextButton({ texto = "" }) { Text("Restablecer") } },
                confirmButton = {
                    TextButton({
                        ua = texto.trim(); prefsUa.edit().putString("user_agent", ua).apply(); uaAbierto = false
                        Avisos.mostrar("Se aplica a las peticiones nuevas")
                    }) { Text("Guardar") }
                },
            )
        }
        LText("DNS sobre HTTPS (para las fuentes)", LectorType.Body.copy(fontSize = 15.sp), Modifier.padding(start = 24.dp, top = 4.dp))
        FilaChips(listOf("Desactivado", "Cloudflare", "Google", "AdGuard", "Quad9"), almacen.doh.coerceIn(0, 4)) {
            almacen.doh = it
            Avisos.mostrar("Se aplicará al volver a abrir la app")
        }
        FilaAjuste(Icons.Rounded.Delete, "Borrar las cookies", "Cierra las sesiones iniciadas en los sitios de las fuentes") {
            CookieManager.getInstance().removeAllCookies { Avisos.mostrar("Cookies borradas") }
        }
        FilaAjuste(Icons.Rounded.Refresh, "Restablecer el modo de lectura de cada serie", "Todas vuelven al modo general") {
            Avisos.mostrar("${almacen.restablecerModosDeSerie()} series restablecidas")
        }
        FilaAjuste(Icons.Rounded.Delete, "Borrar los datos de WebView", "Caché y almacenamiento del navegador interno (Cloudflare, inicios de sesión)") {
            runCatching {
                android.webkit.WebStorage.getInstance().deleteAllData()
                android.webkit.WebView(ctx).apply { clearCache(true); clearHistory(); destroy() }
            }
            Avisos.mostrar("Datos de WebView borrados")
        }
        FilaAjuste(Icons.Rounded.Delete, "Vaciar la caché de red", null) {
            File(ctx.cacheDir, "network_cache").deleteRecursively()
            Avisos.mostrar("Caché de red vaciada")
        }
        Divisor()
        TituloSeccion("Sistema")
        FilaAjuste(Icons.Rounded.Notifications, "Notificaciones", "Permiso y canales de la app") {
            runCatching {
                ctx.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
        FilaAjuste(Icons.Rounded.Build, "Optimización de batería", "Desactívala para que las revisiones automáticas no se retrasen") {
            runCatching {
                ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        FilaAjuste(Icons.Rounded.Info, "Información de la app", null) {
            runCatching {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }
}

// ───────────────────── Acerca de ─────────────────────

@Composable
fun AcercaDeScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val version = remember {
        runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "?"
    }
    PaginaAjustes("Acerca de", onBack) {
        // Cabecera viva: el logo «respira» sobre un halo del color de acento, con nombre y versión en píldora.
        val acento = androidx.compose.material3.MaterialTheme.colorScheme.primary
        val ola = androidx.compose.animation.core.rememberInfiniteTransition(label = "halo")
        val r by ola.animateFloat(0.85f, 1.1f, androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(2600, easing = EnfasisEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "r")
        val radio = if (Mov.reducido) 1f else r
        Column(Modifier.fillMaxWidth().padding(vertical = 24.dp).entrada(0), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(200.dp).drawBehind {
                drawCircle(androidx.compose.ui.graphics.Brush.radialGradient(
                    listOf(acento.copy(alpha = 0.35f), androidx.compose.ui.graphics.Color.Transparent),
                    radius = size.minDimension / 2f * radio), radius = size.minDimension / 2f * radio)
            }, Alignment.Center) { LogoApp(Modifier.size(120.dp).graphicsLayer { scaleX = 0.96f + (radio - 0.85f) * 0.16f; scaleY = scaleX }) }
            LText("Shiori", LectorType.Display.copy(fontSize = 34.sp))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.clip(SuaveShape(14.dp)).background(LectorColors.PillFill).padding(horizontal = 14.dp, vertical = 6.dp)) {
                LText("Versión $version", LectorType.Label, color = LectorColors.Muted)
            }
        }
        val hay = com.example.lector.NuevaVersion.disponible
        FilaAjuste(
            Icons.Rounded.Refresh,
            when {
                com.example.lector.NuevaVersion.comprobando -> "Buscando actualizaciones…"
                hay != null -> "Actualizar a la ${hay.nombre}"
                else -> "Buscar actualizaciones"
            },
            "Las versiones nuevas se publican en GitHub (Saerling/lector) y se instalan encima de esta",
        ) {
            if (hay != null) com.example.lector.NuevaVersion.verDialogo = true
            else com.example.lector.NuevaVersion.buscarAhora(ctx)
        }
        LText(
            "Lector de cómics y manga para Android, inspirado en Komikku (fork de Mihon/Tachiyomi). " +
                "Usa las extensiones de Mihon/Komikku (source-api y red copiados de Komikku, licencia Apache 2.0).",
            LectorType.Body, Modifier.padding(24.dp), color = LectorColors.Muted,
        )
        FilaAjuste(Icons.Rounded.Share, "Komikku en GitHub", "github.com/komikku-app/komikku") {
            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/komikku-app/komikku")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
        LText(
            "Fuente tipográfica: Inter, © The Inter Project Authors, licencia SIL Open Font License 1.1.",
            LectorType.Label, Modifier.padding(24.dp), color = LectorColors.Muted,
        )
    }
}

@Composable
private fun Dato2(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp)) {
        LText(etiqueta, LectorType.Body, Modifier.width(110.dp), color = LectorColors.Muted)
        LText(valor, LectorType.Body, Modifier.weight(1f))
    }
}

/** Fila que abre las casillas de categorías y guarda las elegidas como ids separados por coma. */
@Composable
private fun FilaCategoriasExcluidas(almacen: Almacen, titulo: String, valor: String, alCambiar: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    val cats = almacen.categorias()
    val elegidas = almacen.idsDe(valor)
    val nombres = cats.filter { it.id in elegidas }.joinToString(", ") { it.nombre }
    FilaAjuste(Icons.Rounded.Close, titulo, nombres.ifBlank { "Ninguna" }) {
        if (cats.isEmpty()) Avisos.mostrar("Aún no tienes categorías") else abierto = true
    }
    if (abierto) DialogoAsignarCategorias(
        categorias = cats, seleccionInicial = elegidas,
        alEditar = { abierto = false }, alCancelar = { abierto = false },
        alAceptar = { alCambiar(it.joinToString(",")); abierto = false },
    )
}
