package com.example.lector.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em

// Inter en todos los textos de la app ().
val LectorFont: FontFamily = com.example.lector.Inter

/**
 * Tema actual (claro / oscuro / negro puro). Es estado de Compose: al cambiarlo, todo lo que lee [LectorColors]
 * se vuelve a pintar. Lo fija la actividad al arrancar y cada vez que cambia el ajuste o el modo del sistema.
 */
@Stable
object TemaActual {
    var oscuro by mutableStateOf(true)
    var puro by mutableStateOf(true)
}

/**
 * Colores de Silencio. Oscuro = grafito (como Obsidian): fondo #1E1E1E, superficies #262626 y líneas #363636.
 * Negro puro = lo mismo sobre negro. Claro = blanco con grises fríos. Son propiedades calculadas: siguen al tema.
 */
object LectorColors {
    private val o get() = TemaActual.oscuro
    private val p get() = TemaActual.puro
    private fun c(claro: Long, grafito: Long, negro: Long) = Color(if (!o) claro else if (p) negro else grafito)

    val Bg: Color get() = c(0xFFFFFFFF, 0xFF1E1E1E, 0xFF000000)
    /** Campos, filas agrupadas y tarjetas. */
    val Card: Color get() = c(0xFFF4F4F5, 0xFF262626, 0xFF141414)
    /** Un escalón más (campo con foco, botones neutros). */
    val CardHigh: Color get() = c(0xFFE8E8EA, 0xFF303030, 0xFF222222)
    /** Fondo de hojas inferiores y menús. */
    val Hoja: Color get() = c(0xFFFFFFFF, 0xFF262626, 0xFF141414)
    val HojaFila: Color get() = c(0xFFF4F4F5, 0xFF303030, 0xFF1E1E1E)
    /** Tinte de lo que flota (barra, mini lector). */
    val Cristal: Color get() = c(0xFFF7F7F8, 0xFF262626, 0xFF161616)
    val Glass: Color get() = Cristal.copy(alpha = 0.92f)
    val GlassBorder: Color get() = Linea
    val GlassBorderBrush: Brush get() = Brush.verticalGradient(listOf(Linea, Linea))
    val Text: Color get() = c(0xFF111113, 0xFFDCDDDE, 0xFFF2F2F2)
    val Muted: Color get() = c(0xFF6E6E73, 0xFF999999, 0xFF8E8E93)
    val Pista: Color get() = c(0xFF9A9AA0, 0xFF6E6E6E, 0xFF636366)
    val PillFill: Color get() = CardHigh
    /** Línea finísima (estilo Obsidian). */
    val Linea: Color get() = c(0xFFE3E3E6, 0xFF363636, 0xFF262626)
    val Resalte: Color get() = Text.copy(alpha = 0.08f)
    val Iluminada: Color get() = Text.copy(alpha = 0.10f)
    val SobreImagen: Color get() = if (!o) Color(0x99FFFFFF) else Color(0x80000000)
    val IconoFila: Color get() = CardHigh
    val IconoFilaGlifo: Color get() = Text
    val Aviso: Color get() = c(0xFF111113, 0xFF303030, 0xFF222222)
    val Error: Color = Color(0xFFE5534B)
}

/** Tipografía de Silencio: Inter, títulos grandes y apretados; metadatos en monoespaciada. */
object LectorType {
    private fun t(peso: FontWeight, tam: Int, alto: Int, esp: Double) = TextStyle(fontFamily = LectorFont, fontWeight = peso, fontSize = tam.sp, lineHeight = alto.sp, letterSpacing = esp.em)
    val Display = t(FontWeight.Bold, 32, 38, -0.035)
    val Hero = t(FontWeight.Bold, 24, 29, -0.025)
    val Section = t(FontWeight.SemiBold, 17, 22, -0.015)
    val Body = t(FontWeight.Normal, 15, 21, -0.005)
    val Label = t(FontWeight.Medium, 12, 16, 0.0)
    val Mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
}
