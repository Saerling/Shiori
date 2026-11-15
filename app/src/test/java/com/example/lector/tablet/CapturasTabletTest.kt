package com.example.lector.tablet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.LruCache
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.lector.Almacen
import com.example.lector.Comic
import com.example.lector.LectorTheme
import com.example.lector.Portadas
import com.example.lector.ext.ExtManager
import com.example.lector.fx.Fx
import com.example.lector.ui.AjBibliotecaScreen
import com.example.lector.ui.AjustesScreen
import com.example.lector.ui.AjustesVisorScreen
import com.example.lector.ui.ColaDescargasScreen
import com.example.lector.ui.DetalleScreen
import com.example.lector.ui.EstadisticasScreen
import com.example.lector.ui.Inicio
import com.example.lector.ui.LectorColors
import com.example.lector.ui.LectorTab
import com.example.lector.ui.PersonalizacionScreen
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas de las pantallas principales en tamaño tablet (y una de móvil como referencia), con una biblioteca de
 * ejemplo. Se guardan en build/tablet/<tamaño>/<pantalla>.png; el workflow «Capturas tablet» las sube como artefacto.
 * Una prueba por pantalla: si una revienta, falla solo esa.
 */
abstract class CapturasBase(private val id: String, private val carpeta: String) {
    @get:Rule val rule = createComposeRule()

    @Test fun captura() {
        val app = RuntimeEnvironment.getApplication()
        Fx.init(app)
        val almacen = Almacen.obtener(app)
        val ext = ExtManager.obtener(app)
        val comics = bibliotecaDeEjemplo()
        rule.mainClock.autoAdvance = false
        rule.setContent {
            LectorTheme {
                Box(Modifier.fillMaxSize().background(LectorColors.Bg)) {
                    when (id) {
                        "ajustes" -> AjustesScreen(almacen, {}) {}
                        "aj_biblioteca" -> AjBibliotecaScreen(almacen, {}) {}
                        "aj_visor" -> AjustesVisorScreen(almacen) {}
                        "personalizacion" -> PersonalizacionScreen(almacen, {})
                        "estadisticas" -> EstadisticasScreen(comics, almacen, ext) {}
                        "descargas" -> ColaDescargasScreen(almacen) {}
                        "detalle" -> DetalleScreen(comics[0], almacen, {}, {}, {})
                        else -> {
                            var tab by remember { mutableStateOf(LectorTab.valueOf(id)) }
                            Inicio(
                                tab = tab, onTab = { tab = it }, comics = comics, almacen = almacen,
                                alAnadir = {}, alAnadirCarpeta = {}, alAbrir = {}, alEliminar = {}, alRecargar = {},
                            )
                        }
                    }
                }
            }
        }
        // Las portadas y las entradas escalonadas tardan: se avanza el reloj dejando respirar a los hilos de fondo.
        repeat(30) { rule.mainClock.advanceTimeBy(100); Thread.sleep(20) }
        rule.onRoot().captureRoboImage("build/tablet/$carpeta/$id.png")
    }

    /** 14 cómics locales con portada de colores (directa a la caché en memoria: no hace falta disco). */
    private fun bibliotecaDeEjemplo(): List<Comic> {
        val titulos = listOf(
            "Vagabond", "Berserk", "Monster", "Pluto", "Blame!", "Akira", "Dorohedoro", "Vinland Saga",
            "Oyasumi Punpun", "Blue Period", "Frieren", "Dandadan", "Chainsaw Man", "Un título bastante más largo de lo normal",
        )
        @Suppress("UNCHECKED_CAST")
        val memoria = Portadas::class.java.getDeclaredField("memoria").apply { isAccessible = true }.get(Portadas) as LruCache<String, ImageBitmap>
        val ahora = System.currentTimeMillis()
        return titulos.mapIndexed { i, t ->
            val idC = "ejemplo$i"
            val bmp = Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888)
            val tono = i * 360f / titulos.size
            Canvas(bmp).drawRect(0f, 0f, 200f, 300f, Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 200f, 300f,
                    android.graphics.Color.HSVToColor(floatArrayOf(tono, 0.55f, 0.85f)),
                    android.graphics.Color.HSVToColor(floatArrayOf((tono + 40f) % 360f, 0.7f, 0.35f)),
                    Shader.TileMode.CLAMP,
                )
            })
            memoria.put(idC, bmp.asImageBitmap())
            Comic(idC, t, paginas = 180 + i * 7, ultimaPagina = if (i % 4 == 3) 0 else 20 + i * 11, leido = if (i % 4 == 3) 0L else ahora - i * 3_600_000L * 7, anadido = ahora - i * 86_400_000L)
        }
    }

    companion object {
        val pantallas: List<Array<Any>> = (
            LectorTab.values().map { it.name } +
                listOf("ajustes", "aj_biblioteca", "aj_visor", "personalizacion", "estadisticas", "descargas", "detalle")
            ).map { arrayOf<Any>(it) }
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h800dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CapturasTabletHorizontalTest(id: String) : CapturasBase(id, "horizontal-1280x800") {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun ids(): List<Array<Any>> = pantallas
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w800dp-h1280dp-port-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CapturasTabletVerticalTest(id: String) : CapturasBase(id, "vertical-800x1280") {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun ids(): List<Array<Any>> = pantallas
    }
}

/** Tablet pequeña / plegable (≥600 dp, por debajo de 840): el otro punto de corte. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w600dp-h960dp-port-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CapturasTabletPequenaTest(id: String) : CapturasBase(id, "vertical-600x960") {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun ids(): List<Array<Any>> = pantallas
    }
}

/** Móvil, como referencia: los cambios de tablet no deben mover nada aquí. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-port-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CapturasMovilTest(id: String) : CapturasBase(id, "movil-400x800") {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun ids(): List<Array<Any>> = pantallas
    }
}
