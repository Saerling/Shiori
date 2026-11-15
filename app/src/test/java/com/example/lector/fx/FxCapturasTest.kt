package com.example.lector.fx

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.lector.Almacen
import com.example.lector.LectorTheme
import com.example.lector.ui.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Verificación de la biblioteca de animaciones: cada efecto se pinta en su pieza real (con el efecto forzado) y se
 * guarda una captura en build/fx/<id>.png. Una prueba por efecto: si una vista revienta, falla solo esa.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w400dp-h700dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FxCapturasTest(private val id: String) {
    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun ids(): List<Array<Any>> = IdsFxShiori.map { arrayOf<Any>(it) } + listOf(arrayOf<Any>("_biblioteca"), arrayOf<Any>("_mas"), arrayOf<Any>("_personalizacion"))
    }
    @get:Rule val rule = createComposeRule()

    @Test fun captura() {
        val app = RuntimeEnvironment.getApplication()
        Fx.init(app)
        val almacen = Almacen.obtener(app)
        val todos = IdsFxShiori.filter { it != "onboarding" }
        rule.mainClock.autoAdvance = false
        when (id) {
            "_biblioteca" -> rule.setContent { LectorTheme { BibliotecaFxPantalla(catalogoShiori(almacen, emptyList()) {}, {}) } }
            "_mas" -> { Fx.setAll(todos, true); rule.setContent { LectorTheme { Box(Modifier.fillMaxSize().background(LectorColors.Bg)) { MasScreen(almacen, {}, {}, {}) } } } }
            "_personalizacion" -> { Fx.setAll(todos, true); rule.setContent { LectorTheme { PersonalizacionScreen(almacen, {}) } } }
            else -> {
                val item = catalogoShiori(almacen, emptyList()) {}.first { it.id == id }
                rule.setContent { LectorTheme { Box(Modifier.fillMaxSize().background(LectorColors.Bg).padding(16.dp)) { FxForzar(id) { item.vista!!() } } } }
            }
        }
        repeat(12) { rule.mainClock.advanceTimeBy(100) }
        rule.onRoot().captureRoboImage("build/fx/$id.png")
        Fx.setAll(todos, false)
    }
}
