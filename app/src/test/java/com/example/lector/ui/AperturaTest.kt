package com.example.lector.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Fotogramas quietos de la apertura de puntos (build/apertura/*.png), para verla sin teléfono. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AperturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun fotogramas() {
        var f by mutableFloatStateOf(0f)
        rule.setContent { key(f) { AperturaPuntos(PuntosIcono, "Shiori", fijo = f, alTerminar = {}) } }
        for (ms in listOf(0, 300, 600, 800, 930, 1050, 1200, 1450, 1650, 1900)) {
            f = ms / AperturaMs.toFloat(); rule.waitForIdle()
            rule.onRoot().captureRoboImage("build/apertura/$ms.png")
        }
    }
}
