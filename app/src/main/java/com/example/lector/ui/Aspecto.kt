package com.example.lector.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/*
 * Preferencias de aspecto de Silencio (SharedPreferences «aspecto»). Solo lo que cambia cómo se usa la app:
 * fondo grafito, animaciones, portadas panorámicas, mantener pulsado, gestos de capítulos y descargas.
 */
object Aspecto {
    enum class LadoDescargas(val nombre: String) { IZQUIERDA("Izquierda"), CENTRO("Centro"), DERECHA("Derecha") }

    private var prefs: SharedPreferences? = null

    /** Animación al abrir la app. */
    var arranque by mutableStateOf(AnimArranque.PUNTOS); private set
    /** Redondeo de las portadas en toda la app, en dp. */
    var radioPortada by mutableFloatStateOf(14f); private set
    /** Fondo oscuro grafito (como Obsidian) en vez de negro puro. */
    var grafito by mutableStateOf(true); private set
    /** Mantener pulsado añade o quita de la biblioteca directamente, sin menú. */
    var accionDirecta by mutableStateOf(false); private set
    /** Portadas apaisadas a lo ancho en Historial y Novedades. */
    var panoramica by mutableStateOf(false); private set
    var verDescargas by mutableStateOf(true); private set
    /** Intensidad del vidrio de lo que flota (barra, mini lector, barras de arriba, hojas): 0 = casi opaco, 1 = muy claro. */
    var vidrio by mutableFloatStateOf(0.5f); private set
    /** Brillo de borde y halos (efecto luminoso de ColorOS 17). */
    var luminoso by mutableStateOf(true); private set
    var ladoDescargas by mutableStateOf(LadoDescargas.IZQUIERDA); private set

    fun iniciar(ctx: Context) {
        if (prefs != null) return
        val p = ctx.applicationContext.getSharedPreferences("aspecto", Context.MODE_PRIVATE).also { prefs = it }
        grafito = p.getBoolean("grafito", true)
        arranque = runCatching { AnimArranque.valueOf(p.getString("arranque", null)!!) }.getOrDefault(AnimArranque.PUNTOS)
        radioPortada = p.getFloat("radio_portada3", 14f)
        accionDirecta = p.getString("estilo_menu", null) == "DIRECTO" || p.getBoolean("accion_directa", false)
        panoramica = p.getBoolean("estilo_panoramica", false)
        verDescargas = p.getBoolean("estilo_descargas", true)
        vidrio = p.getFloat("vidrio", 0.5f)
        luminoso = p.getBoolean("luminoso", true)
        ladoDescargas = runCatching { LadoDescargas.valueOf(p.getString("estilo_descargas_lado", null)!!) }.getOrDefault(LadoDescargas.IZQUIERDA)
    }

    private fun b(k: String, v: Boolean) { prefs?.edit()?.putBoolean(k, v)?.apply() }
    fun elegir(a: AnimArranque) { arranque = a; prefs?.edit()?.putString("arranque", a.name)?.apply() }
    fun elegirRadioPortada(v: Float) { radioPortada = v; prefs?.edit()?.putFloat("radio_portada3", v)?.apply() }
    fun elegirGrafito(v: Boolean) { grafito = v; b("grafito", v) }
    fun elegirAccionDirecta(v: Boolean) { accionDirecta = v; b("accion_directa", v); prefs?.edit()?.remove("estilo_menu")?.apply() }
    fun elegirPanoramica(v: Boolean) { panoramica = v; b("estilo_panoramica", v) }
    fun elegirVidrio(v: Float) { vidrio = v.coerceIn(0f, 1f); prefs?.edit()?.putFloat("vidrio", vidrio)?.apply() }
    fun elegirLuminoso(v: Boolean) { luminoso = v; b("luminoso", v) }
    fun elegirVerDescargas(v: Boolean) { verDescargas = v; b("estilo_descargas", v) }
    fun elegir(v: LadoDescargas) { ladoDescargas = v; prefs?.edit()?.putString("estilo_descargas_lado", v.name)?.apply() }

    /** Portadas que resultaron apaisadas al cargarse (alto/ancho ≤ 0,75). */
    private val anchas = mutableStateMapOf<String, Boolean>()
    fun anotarPortada(clave: String?, ancho: Int, alto: Int) {
        if (clave.isNullOrBlank() || ancho <= 0) return
        val ancha = alto.toFloat() / ancho <= 0.75f
        if (anchas[clave] != ancha && (ancha || clave in anchas)) anchas[clave] = ancha
    }
    fun factorAncho(vararg claves: String?): Float = if (panoramica && claves.any { it != null && anchas[it] == true }) 1.6f else 1f
}

/** Animación al abrir la app. */
enum class AnimArranque(val nombre: String) { PUNTOS("Puntos"), AGUJERO("Agujero negro"), RAPIDA("Rápida"), NINGUNA("Ninguna") }

/** Forma de las portadas según el ajuste de redondeo ([escala] para portadas pequeñas). */
val FormaPortada: androidx.compose.ui.graphics.Shape get() = SuaveShape(Aspecto.radioPortada.dp)
fun formaPortada(escala: Float): androidx.compose.ui.graphics.Shape = SuaveShape((Aspecto.radioPortada * escala).dp)

/** Velocidad global de las animaciones: se entrega al Recomposer de la ventana. */
object MovimientoApp {
    private var prefs: SharedPreferences? = null
    var activado by mutableStateOf(true); private set
    var velocidad by mutableFloatStateOf(1f); private set
    private var escalaSistema by mutableFloatStateOf(1f)

    val escala: androidx.compose.ui.MotionDurationScale = object : androidx.compose.ui.MotionDurationScale {
        override val scaleFactor: Float get() = if (!activado) 0f else escalaSistema / velocidad.coerceAtLeast(0.1f)
    }

    fun iniciar(ctx: Context) {
        val p = ctx.getSharedPreferences("aspecto", Context.MODE_PRIVATE).also { prefs = it }
        activado = p.getBoolean("animaciones", true)
        velocidad = p.getFloat("velocidad_anim", 1f)
        Aspecto.iniciar(ctx)
        val res = ctx.contentResolver
        fun leer() = android.provider.Settings.Global.getFloat(res, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        escalaSistema = leer()
        res.registerContentObserver(
            android.provider.Settings.Global.getUriFor(android.provider.Settings.Global.ANIMATOR_DURATION_SCALE), false,
            object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) { escalaSistema = leer(); MovimientoReducido.actualizar() }
            },
        )
    }

    fun elegirActivado(v: Boolean) { activado = v; prefs?.edit()?.putBoolean("animaciones", v)?.apply(); MovimientoReducido.actualizar() }
    fun elegirVelocidad(v: Float) { velocidad = v; prefs?.edit()?.putFloat("velocidad_anim", v)?.apply() }
}

val VelocidadesAnimacion = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
fun textoVelocidad(v: Float): String = (if (v % 1f == 0f) v.toInt().toString() else v.toString().trimEnd('0')) + "×"

// ───────────── Gestos de capítulos ─────────────

/** Lo que hace deslizar una fila de capítulo, a cada lado (Ajustes → Aspecto → Gestos). */
enum class AccionGesto(val titulo: String, val icono: ImageVector, val color: Color, val corto: String) {
    NADA("Nada", Icons.Rounded.Close, Color(0xFF8E8E93), "Nada"),
    LEIDO("Marcar leído / no leído", Icons.Rounded.Check, Color(0xFF4C8DFF), "Leído"),
    MARCA("Marcar / desmarcar", Iconos.Marca, Color(0xFFF2C14E), "Marca"),
    DESCARGA("Descargar / borrar descarga", Iconos.Descargar, Color(0xFF3DDC84), "Descargar"),
    ANTERIORES("Marcar anteriores como leídos", Icons.Rounded.KeyboardArrowDown, Color(0xFFA88BFA), "Anteriores");
}

object GestosCapitulos {
    private var prefs: SharedPreferences? = null
    /** Deslizar hacia la derecha. */
    var derecha by mutableStateOf(AccionGesto.LEIDO); private set
    /** Deslizar hacia la izquierda. */
    var izquierda by mutableStateOf(AccionGesto.MARCA); private set

    // Claves antiguas (nombres en inglés) → nuevas: se conservan los gestos que ya tenías elegidos.
    private val viejos = mapOf("NONE" to AccionGesto.NADA, "MARK_READ" to AccionGesto.LEIDO, "BOOKMARK" to AccionGesto.MARCA,
        "DOWNLOAD" to AccionGesto.DESCARGA, "MARK_PREVIOUS_READ" to AccionGesto.ANTERIORES)

    fun iniciar(ctx: Context) {
        if (prefs != null) return
        val p = ctx.applicationContext.getSharedPreferences("gestos", Context.MODE_PRIVATE).also { prefs = it }
        fun leer(k: String, d: AccionGesto) = p.getString(k, null)?.let { n -> viejos[n] ?: runCatching { AccionGesto.valueOf(n) }.getOrNull() } ?: d
        derecha = leer("derecha", AccionGesto.LEIDO)
        izquierda = leer("izquierda", AccionGesto.MARCA)
    }
    fun elegirDerecha(a: AccionGesto) { derecha = a; prefs?.edit()?.putString("derecha", a.name)?.apply() }
    fun elegirIzquierda(a: AccionGesto) { izquierda = a; prefs?.edit()?.putString("izquierda", a.name)?.apply() }
}

/** Cómo está el capítulo: decide el texto de las acciones que alternan. */
class EstadoGesto(val leido: Boolean, val marcado: Boolean, val descargado: Boolean, val descargando: Boolean = false)
