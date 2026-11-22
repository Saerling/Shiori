package com.example.lector.ui

// Ideas de diseño UI/UX sacadas de la comunidad «UIUX Design» de Threads (nov. 2026).
// Cada una tiene su interruptor en Personalización → Biblioteca de animaciones y funciones → «Threads».
// Todo empieza apagado; el gancho en cada pantalla es `fxOn("th_…")`.

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lector.Almacen
import com.example.lector.Comic
import com.example.lector.Descargas
import com.example.lector.fx.Fx
import com.example.lector.fx.fxOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Ids de las ideas de Threads (para contar las activas y para el catálogo). */
val IdsThreads = listOf(
    "th_racha", "th_graficas", "th_perfil", "th_ambiente", "th_recientes", "th_confirmar", "th_calendario",
    "th_pills", "th_fuente", "th_optico", "th_liquido", "th_modulos", "th_stickers", "th_resumen",
)

private const val DIA = 86_400_000L

/** Capítulos leídos por día (índice 0 = hoy), a partir del historial en línea y de los cómics locales. */
private fun lecturasPorDia(almacen: Almacen, comics: List<Comic>, dias: Int): IntArray {
    val hoy = System.currentTimeMillis() / DIA
    val cuenta = IntArray(dias)
    almacen.historialLinea.forEach { val d = (hoy - it.leido / DIA).toInt(); if (d in 0 until dias) cuenta[d]++ }
    comics.forEach { if (!it.enLinea && it.leido > 0) { val d = (hoy - it.leido / DIA).toInt(); if (d in 0 until dias) cuenta[d]++ } }
    return cuenta
}

// ───────────── 1 · Racha de lectura (lejdi.diana, «Streak Counter Animation») ─────────────

@Composable
fun RachaLectura(almacen: Almacen, comics: List<Comic>, modifier: Modifier = Modifier) {
    val dias = remember(almacen.historialLinea, comics) { lecturasPorDia(almacen, comics, 60) }
    // La racha sigue viva si hoy aún no lees pero ayer sí.
    val inicio = if (dias[0] > 0) 0 else 1
    val racha = (inicio until dias.size).takeWhile { dias[it] > 0 }.size
    var mostrado by remember { mutableIntStateOf(0) }
    LaunchedEffect(racha) { if (Mov.reducido) mostrado = racha else { delay(250); mostrado = racha } }
    val salto = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { salto.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 260f)) }
    val acento = MaterialTheme.colorScheme.primary
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(acento.copy(alpha = 0.22f), LectorColors.Card))).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🔥", fontSize = 34.sp, modifier = Modifier.graphicsLayer { scaleX = salto.value; scaleY = salto.value })
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Odometro("$mostrado", LectorType.Display.copy(fontSize = 30.sp, lineHeight = 34.sp))
                Spacer(Modifier.width(6.dp))
                LText(if (racha == 1) "día seguido" else "días seguidos", LectorType.Body, Modifier.padding(bottom = 4.dp), color = LectorColors.Muted)
            }
            LText(
                when { racha == 0 -> "Lee un capítulo hoy para empezar una racha"; dias[0] == 0 -> "Lee hoy para no perder la racha"; else -> "¡Sigue así!" },
                LectorType.Label, color = LectorColors.Muted,
            )
        }
        // Los últimos 7 días, de más antiguo a hoy.
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 6 downTo 0) Box(
                Modifier.size(9.dp).clip(CircleShape).background(if (dias[i] > 0) acento else LectorColors.Text.copy(alpha = 0.12f)),
            )
        }
    }
}

// ───────────── 2 · Gráficas mínimas animadas (urdesignbro) ─────────────

@Composable
fun GraficaSemanal(almacen: Almacen, comics: List<Comic>, modifier: Modifier = Modifier, semanas: Int = 8, alto: Dp = 96.dp, titulo: Boolean = true) {
    val valores = remember(almacen.historialLinea, comics, semanas) {
        val d = lecturasPorDia(almacen, comics, semanas * 7)
        List(semanas) { s -> (0 until 7).sumOf { d[(semanas - 1 - s) * 7 + it] } }
    }
    val max = (valores.maxOrNull() ?: 0).coerceAtLeast(1)
    val acento = MaterialTheme.colorScheme.primary
    Column(modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        if (titulo) {
            LText("Capítulos por semana", LectorType.Label, color = LectorColors.Muted)
            Spacer(Modifier.height(8.dp))
        }
        Row(Modifier.fillMaxWidth().height(alto), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            valores.forEachIndexed { i, v ->
                val f = remember { Animatable(0f) }
                LaunchedEffect(v) {
                    if (Mov.reducido) f.snapTo(v / max.toFloat())
                    else { delay(i * 45L); f.animateTo(v / max.toFloat(), spring(dampingRatio = 0.7f, stiffness = 180f)) }
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier.fillMaxWidth().fillMaxHeight(f.value.coerceIn(0.04f, 1f)).clip(RoundedCornerShape(6.dp))
                            .background(if (i == valores.lastIndex) acento else acento.copy(alpha = 0.35f)),
                    )
                }
            }
        }
        if (titulo) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                LText("hace $semanas sem.", LectorType.Label, Modifier.weight(1f), color = LectorColors.Muted)
                LText("esta semana · ${valores.last()}", LectorType.Label, color = LectorColors.Muted)
            }
        }
    }
}

// ───────────── 3 · Perfil de lector en una vista (uixamjad) ─────────────

@Composable
fun PerfilLector(almacen: Almacen, modifier: Modifier = Modifier) {
    val comics = remember(almacen.version) { almacen.cargar() }
    val terminados = comics.count { it.terminado() }
    val generos = remember(comics) {
        comics.flatMap { it.generos }.map { it.trim() }.filter { it.isNotBlank() }
            .groupingBy { it.lowercase() }.eachCount().entries.sortedByDescending { it.value }.take(3)
    }
    val horas = almacen.tiempoLectura / 3_600_000L
    Column(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(24.dp))
            .background(LectorColors.Card).padding(16.dp),
    ) {
        LText("Tu perfil de lector", LectorType.Section)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DatoPerfil("${comics.size}", "en biblioteca", Modifier.weight(1f))
            DatoPerfil("$terminados", "terminados", Modifier.weight(1f))
            DatoPerfil("${horas}h", "leyendo", Modifier.weight(1f))
        }
        if (generos.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            LText("Lo que más lees", LectorType.Label, color = LectorColors.Muted)
            val max = generos.first().value.toFloat()
            generos.forEach { (g, n) ->
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LText(g.replaceFirstChar { it.uppercase() }, LectorType.Body, Modifier.width(110.dp), maxLines = 1)
                    Box(Modifier.weight(1f).height(8.dp).clip(CircleShape).background(LectorColors.Text.copy(alpha = 0.08f))) {
                        Box(Modifier.fillMaxWidth(n / max).fillMaxHeight().clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                    LText("$n", LectorType.Label, Modifier.padding(start = 8.dp), color = LectorColors.Muted)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        GraficaSemanal(almacen, comics, alto = 44.dp, titulo = false)
    }
}

@Composable
private fun DatoPerfil(valor: String, etiqueta: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(LectorColors.CardHigh).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Odometro(valor, LectorType.Section.copy(fontWeight = FontWeight.Bold))
        LText(etiqueta, LectorType.Label, color = LectorColors.Muted)
    }
}

// ───────────── 4 · Ambientes de la biblioteca (papering.app, «Custom Bookshelf») ─────────────

val NombresAmbiente = listOf("Biblioteca clásica", "Ventana de mañana", "Noche acogedora", "Atelier", "Ventana de verano")

/** Capa de fondo de la biblioteca: un degradado con la «luz» del ambiente elegido. Va detrás del contenido. */
@Composable
fun AmbienteBiblioteca(variante: Int, modifier: Modifier = Modifier) {
    val (luz, base) = when (variante.coerceIn(0, 4)) {
        0 -> Color(0xFFC88A4A) to Color(0xFF5A3A22)
        1 -> Color(0xFFFFE2A8) to Color(0xFFE8C98F)
        2 -> Color(0xFFFFB35C) to Color(0xFF15183A)
        3 -> Color(0xFFE07A5F) to Color(0xFF6D4C8F)
        else -> Color(0xFFB6E3A1) to Color(0xFF3E7D4F)
    }
    val fuerza = if (TemaActual.oscuro) 0.30f else 0.22f
    Box(
        modifier.drawBehind {
            drawRect(Brush.verticalGradient(listOf(base.copy(alpha = fuerza), Color.Transparent), endY = size.height * 0.7f))
            drawRect(Brush.radialGradient(listOf(luz.copy(alpha = fuerza * 1.3f), Color.Transparent), center = Offset(size.width * 0.85f, 0f), radius = size.width * 0.9f))
        },
    )
}

// ───────────── 5 · Búsquedas recientes (bpolacek.pdf) ─────────────

object BusquedasRecientes {
    private var prefs: android.content.SharedPreferences? = null
    val lista = mutableStateListOf<String>()
    fun iniciar(ctx: android.content.Context) {
        if (prefs != null) return
        val p = ctx.applicationContext.getSharedPreferences("busquedas", android.content.Context.MODE_PRIVATE)
        prefs = p
        lista.addAll(p.getString("recientes", "").orEmpty().split('\n').filter { it.isNotBlank() })
    }
    fun anotar(q: String) {
        val t = q.trim(); if (t.isEmpty()) return
        lista.removeAll { it.equals(t, true) }; lista.add(0, t)
        while (lista.size > 12) lista.removeAt(lista.lastIndex)
        guardar()
    }
    fun quitar(q: String) { lista.remove(q); guardar() }
    fun borrarTodo() { lista.clear(); guardar() }
    private fun guardar() { prefs?.edit()?.putString("recientes", lista.joinToString("\n"))?.apply() }
}

@Composable
fun ChipsRecientes(onElegir: (String) -> Unit, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { BusquedasRecientes.iniciar(ctx) }
    Column(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LText("Recientes", LectorType.Section, Modifier.weight(1f))
            if (BusquedasRecientes.lista.isNotEmpty()) LText("Borrar", LectorType.Label, Modifier.pressable { BusquedasRecientes.borrarTodo() }.padding(8.dp), color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))
        if (BusquedasRecientes.lista.isEmpty()) LText("Escribe un título y pulsa buscar. Tus búsquedas aparecerán aquí.", LectorType.Body, color = LectorColors.Muted)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BusquedasRecientes.lista.forEach { q ->
                Row(
                    Modifier.clip(CircleShape).background(LectorColors.CardHigh).pressable { onElegir(q) }.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LText(q, LectorType.Body, maxLines = 1)
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.size(22.dp).clip(CircleShape).pressable { BusquedasRecientes.quitar(q) }, Alignment.Center) {
                        Text("✕", fontSize = 11.sp, color = LectorColors.Muted)
                    }
                }
            }
        }
    }
}

// ───────────── 6 · Confirmación con todo a la vista (aoub_ak) ─────────────

@Composable
fun ResumenMigracion(origen: String, destino: String, comic: Comic, capsOrigen: Int?) {
    val acento = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(16.dp)).background(LectorColors.Text.copy(alpha = 0.06f)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LText(origen, LectorType.Body.copy(fontWeight = FontWeight.SemiBold), Modifier.weight(1f), maxLines = 1)
            LText("→", LectorType.Section, Modifier.padding(horizontal = 8.dp), color = acento)
            LText(destino, LectorType.Body.copy(fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End), Modifier.weight(1f), color = acento, maxLines = 1)
        }
        Spacer(Modifier.height(8.dp))
        FilaResumen("Capítulos leídos", "${comic.ultimaPagina}")
        FilaResumen("Capítulos en el origen", capsOrigen?.toString() ?: "—")
        FilaResumen("Categorías y portada", "Se conservan")
        FilaResumen("Descargas", "No se mueven")
    }
}

@Composable
private fun FilaResumen(a: String, b: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        LText(a, LectorType.Label, Modifier.weight(1f), color = LectorColors.Muted)
        LText(b, LectorType.Label.copy(fontWeight = FontWeight.SemiBold))
    }
}

// ───────────── 7 · Onda al elegir un día del calendario (board.ui) ─────────────

/** Al pasar a elegido, un aro sale del borde y se desvanece. */
fun Modifier.ondaSeleccion(elegido: Boolean, color: Color, radio: Dp = 14.dp): Modifier = composed {
    val onda = remember { Animatable(1f) }
    LaunchedEffect(elegido) { if (elegido && !Mov.reducido) { onda.snapTo(0f); onda.animateTo(1f, tween(520)) } }
    drawBehind {
        val p = onda.value
        if (p < 1f) {
            val extra = 10.dp.toPx() * p
            drawRoundRect(
                color.copy(alpha = (1f - p) * 0.8f), topLeft = Offset(-extra, -extra),
                size = androidx.compose.ui.geometry.Size(size.width + extra * 2, size.height + extra * 2),
                cornerRadius = CornerRadius(radio.toPx() + extra), style = Stroke(2.dp.toPx()),
            )
        }
    }
}

// ───────────── 8 · Pills que dan un saltito al elegirlas (uixamjad / heymaxhez) ─────────────

fun Modifier.saltoAlElegir(elegido: Boolean): Modifier = composed {
    val s = remember { Animatable(1f) }
    var primera by remember { mutableStateOf(true) }
    LaunchedEffect(elegido) {
        if (primera) { primera = false; return@LaunchedEffect }
        if (Mov.reducido) return@LaunchedEffect
        s.animateTo(if (elegido) 1.12f else 0.94f, tween(90)); s.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
    }
    graphicsLayer { scaleX = s.value; scaleY = s.value }
}

// ───────────── 9 · Fuente legible (uxco.llective, «Hyperlegible Sans») ─────────────
// Se elige en Theme.kt al arrancar (com.example.lector.Inter): hace falta reiniciar Shiori para verla.

// ───────────── 11 · Buscador líquido (tranmautritam, «Liquid Gooey») ─────────────

/** El campo se estira como una gota al enfocarlo y tiembla un poco con cada letra. */
fun Modifier.gotaLiquida(foco: Boolean, letras: Int): Modifier = composed {
    val sx = remember { Animatable(1f) }
    val sy = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(foco) {
        if (Mov.reducido) return@LaunchedEffect
        val muelle = spring<Float>(dampingRatio = 0.35f, stiffness = 380f)
        launch { sx.snapTo(if (foco) 1.05f else 0.97f); sx.animateTo(1f, muelle) }
        sy.snapTo(if (foco) 0.88f else 1.04f); sy.animateTo(1f, muelle)
    }
    LaunchedEffect(letras) {
        if (Mov.reducido || !foco) return@LaunchedEffect
        scope.launch { sy.snapTo(1.03f); sy.animateTo(1f, spring(dampingRatio = 0.3f, stiffness = 900f)) }
    }
    graphicsLayer { scaleX = sx.value; scaleY = sy.value }
}

// ───────────── 12 · Ficha en módulos (ux.loc, diseño japonés) ─────────────

@Composable
fun ModulosFicha(estado: Int, capitulos: Int, leidos: Int, fuente: String, ultimo: Long) {
    val estadoTxt = when (estado) { 1 -> "En emisión"; 2 -> "Completado"; 3 -> "Licenciado"; 4 -> "Publicación terminada"; 5 -> "Cancelado"; 6 -> "En pausa"; else -> "Desconocido" }
    val hace = if (ultimo <= 0L) "—" else {
        val d = ((System.currentTimeMillis() - ultimo) / DIA).toInt()
        when { d <= 0 -> "Hoy"; d == 1 -> "Ayer"; d < 30 -> "Hace $d días"; else -> "Hace ${d / 30} meses" }
    }
    val pct = if (capitulos > 0) "${leidos * 100 / capitulos}%" else "—"
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Modulo("Estado", estadoTxt, Modifier.weight(1f))
            Modulo("Capítulos", "$leidos / $capitulos", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Modulo("Fuente", fuente, Modifier.weight(1f))
            Modulo("Último cap.", hace, Modifier.weight(1f))
            Modulo("Leído", pct, Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun Modulo(etiqueta: String, valor: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).border(1.dp, LectorColors.Linea, RoundedCornerShape(14.dp)).background(LectorColors.Card).padding(horizontal = 12.dp, vertical = 9.dp)) {
        LText(etiqueta.uppercase(), LectorType.Label.copy(fontSize = 10.sp, letterSpacing = 0.8.sp), color = LectorColors.Muted)
        LText(valor, LectorType.Body.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
    }
}

// ───────────── 13 · Stickers en «Nueva versión» (actlikeacrazy) ─────────────

@Composable
fun StickersNovedad(modifier: Modifier = Modifier) {
    val stickers = listOf("📚" to Offset(0.08f, 0.2f), "✨" to Offset(0.3f, 0.65f), "🔥" to Offset(0.52f, 0.15f), "🌙" to Offset(0.72f, 0.6f), "💬" to Offset(0.9f, 0.22f))
    BoxWithConstraints(modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(20.dp)).background(LectorColors.Text.copy(alpha = 0.05f))) {
        val w = maxWidth; val h = maxHeight
        stickers.forEachIndexed { i, (e, p) ->
            val ent = remember { Animatable(0f) }
            LaunchedEffect(Unit) { if (Mov.reducido) ent.snapTo(1f) else { delay(80L * i); ent.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f)) } }
            val giro = if (i % 2 == 0) -12f else 10f
            Text(
                e, fontSize = 26.sp,
                modifier = Modifier.offset(w * p.x - 14.dp, h * p.y - 4.dp)
                    .graphicsLayer { scaleX = ent.value; scaleY = ent.value; rotationZ = giro * ent.value },
            )
        }
        Text(
            "NUEVA VERSIÓN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LectorColors.Text,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp).clip(CircleShape).background(LectorColors.Text.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

// ───────────── 14 · Resumen de un vistazo en Inicio (usamafaheemdev) ─────────────

@Composable
fun ResumenInicio(comics: List<Comic>, modifier: Modifier = Modifier) {
    val capsSinLeer = comics.filter { it.enLinea }.sumOf { it.sinLeer }
    val conNovedades = comics.count { it.enLinea && it.sinLeer > 0 }
    val enCola = Descargas.cola.size
    Row(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TarjetaResumen("$capsSinLeer", "caps. sin leer", Modifier.weight(1f), destacado = true)
        TarjetaResumen("$conNovedades", "mangas con novedades", Modifier.weight(1f))
        TarjetaResumen("$enCola", "en descarga", Modifier.weight(1f))
    }
}

@Composable
private fun TarjetaResumen(valor: String, etiqueta: String, modifier: Modifier, destacado: Boolean = false) {
    val acento = MaterialTheme.colorScheme.primary
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(if (destacado) acento.copy(alpha = 0.18f) else LectorColors.Card).padding(12.dp),
    ) {
        Odometro(valor, LectorType.Hero.copy(fontSize = 24.sp), color = if (destacado) acento else LectorColors.Text)
        LText(etiqueta, LectorType.Label, color = LectorColors.Muted, maxLines = 2)
    }
}

// ───────────── Catálogo ─────────────

/** Las 14 ideas como entradas de la Biblioteca de animaciones y funciones (origen «Threads»). */
fun catalogoThreads(almacen: Almacen, comics: List<Comic>): List<com.example.lector.fx.FxItem> {
    fun i(id: String, n: String, u: String, v: @Composable () -> Unit) = com.example.lector.fx.FxItem(id, n, "Threads", u, vista = v)
    val c0 = comics.firstOrNull() ?: Comic(id = "th_demo", titulo = "Shiori", paginas = 40, ultimaPagina = 12)
    return listOf(
        i("th_racha", "Racha de lectura", "Historial: tarjeta con tus días seguidos leyendo; el número rueda y la llama da un salto · @lejdi.diana") { RachaLectura(almacen, comics) },
        i("th_graficas", "Gráfica semanal animada", "Historial y Estadísticas: capítulos por semana con barras que crecen con muelle · @urdesignbro") { GraficaSemanal(almacen, comics) },
        i("th_perfil", "Perfil de lector", "Más: en una tarjeta, biblioteca, terminados, horas, géneros favoritos y tu ritmo · @uixamjad") { PerfilLector(almacen) },
        i("th_ambiente", "Ambientes de la biblioteca", "Biblioteca: luz de fondo según el ambiente elegido (el orden de categorías ya se arrastra en Categorías) · @papering.app") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NombresAmbiente.forEachIndexed { k, n -> ChipSel(n, Fx.variante("th_ambiente") == k) { Fx.elegirVariante("th_ambiente", k) } }
                }
                Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(18.dp)).background(LectorColors.Bg)) {
                    AmbienteBiblioteca(Fx.variante("th_ambiente"), Modifier.matchParentSize())
                    ComicCover(c0, almacen, Modifier.align(Alignment.Center).width(60.dp).aspectRatio(2f / 3f))
                }
            }
        },
        i("th_recientes", "Búsquedas recientes", "Búsqueda global: tus últimas búsquedas como chips; tócalas para repetir, ✕ para quitar · @bpolacek.pdf") { ChipsRecientes({}) },
        i("th_confirmar", "Confirmar con todo a la vista", "Migrar: antes de migrar ves origen → destino, capítulos leídos y qué se conserva · @aoub_ak") { ResumenMigracion("Fuente vieja", "Fuente nueva", c0, 52) },
        i("th_calendario", "Onda en el calendario", "Próximos: al elegir un día sale un aro desde la casilla · @board.ui") {
            var e by remember { mutableStateOf(false) }; val a = MaterialTheme.colorScheme.primary
            Box(Modifier.padding(16.dp).size(54.dp).ondaSeleccion(e, a).clip(RoundedCornerShape(14.dp)).background(if (e) a.copy(alpha = 0.16f) else LectorColors.Card).border(2.dp, if (e) a else Color.Transparent, RoundedCornerShape(14.dp)).pressable { e = !e }, Alignment.Center) { LText("22", LectorType.Section) }
        },
        i("th_pills", "Pills con salto", "Todos los chips de filtro dan un saltito al elegirlos o quitarlos · @uixamjad, @heymaxhez") {
            var s by remember { mutableIntStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Sin leer", "Descargados", "Terminados").forEachIndexed { k, t -> ChipSel(t, s == k) { s = k } } }
        },
        i("th_fuente", "Fuente Atkinson Hyperlegible", "Toda la app usa una letra pensada para leer mejor (se aplica al reiniciar Shiori) · @uxco.llective") {
            LText("Ilustración 1lI 0O — más fácil de leer", LectorType.Body.copy(fontFamily = com.example.lector.FuenteLegible, fontSize = 17.sp))
        },
        i("th_optico", "Botones centrados ópticamente", "Los botones recortan el interlineado y compensan el ícono para que el texto quede centrado a la vista · @diegohaz") { BotonExpresivo("Continuar", Icons.Rounded.PlayArrow, {}) },
        i("th_liquido", "Buscador líquido", "Los buscadores se estiran como una gota al tocarlos y tiemblan al escribir · @tranmautritam") { var q by remember { mutableStateOf("") }; com.example.lector.ui.CampoBusqueda(q, { q = it }, "Toca y escribe…") },
        i("th_modulos", "Ficha en módulos", "Ficha de un manga: estado, capítulos, fuente, último capítulo y % en una cuadrícula · @ux.loc") { ModulosFicha(1, 120, 87, "MangaDex", System.currentTimeMillis() - 3 * DIA) },
        i("th_stickers", "Stickers en «Nueva versión»", "La hoja de actualización de Shiori lleva stickers que entran rebotando · @actlikeacrazy") { StickersNovedad() },
        i("th_resumen", "Resumen en Inicio", "Inicio: caps. sin leer, mangas con novedades y descargas en tres tarjetas bajo el carrusel · @usamafaheemdev") { ResumenInicio(comics) },
    )
}
