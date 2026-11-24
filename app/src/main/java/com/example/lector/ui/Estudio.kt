package com.example.lector.ui

// «Estudio»: piezas de Silencio inspiradas en otras apps, cada una con su interruptor en
// Personalización → Biblioteca de animaciones y funciones → «Silencio».
//  · Obsidian: tema grafito con acento violeta, propiedades de nota y #etiquetas.
//  · Lectores de Orion Store (Anx Reader, Readest, Book's Story, ReadYou): mapa de lectura por días.
// Nada de esto copia código de esas apps: son ideas redibujadas con las piezas de Shiori.

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lector.Almacen
import com.example.lector.Comic
import com.example.lector.fx.FxItem

val IdsSilencio = listOf("si_obsidiana", "si_propiedades", "si_mapa")

private val EstiloClave = LectorType.Label.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)

// ───────────── Propiedades (Obsidian) ─────────────

/**
 * Bloque de propiedades como el de una nota de Obsidian: clave apagada a la izquierda, valor a la derecha,
 * filas separadas por una línea finísima. Las filas sin valor no se pintan.
 */
@Composable
fun PropiedadesFicha(filas: List<Pair<String, String?>>, modifier: Modifier = Modifier) {
    val visibles = filas.filter { !it.second.isNullOrBlank() }
    if (visibles.isEmpty()) return
    val forma = RoundedCornerShape(14.dp)
    Column(modifier.fillMaxWidth().clip(forma).background(LectorColors.Card).border(1.dp, LectorColors.Linea, forma)) {
        visibles.forEachIndexed { i, (k, v) ->
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(LectorColors.Linea))
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                LText(k.lowercase(), EstiloClave, Modifier.width(108.dp), color = LectorColors.Muted, maxLines = 1)
                LText(v!!, LectorType.Body, Modifier.weight(1f), maxLines = 2)
            }
        }
    }
}

/** Etiqueta de Obsidian: «#género», texto de acento sobre un fondo tonal, esquinas pequeñas. */
@Composable
fun EtiquetaObsidian(texto: String, onClick: () -> Unit) {
    val a = MaterialTheme.colorScheme.primary
    LText(
        "#" + texto.trim().replace(' ', '-').lowercase(), LectorType.Label.copy(fontWeight = FontWeight.Medium),
        Modifier.clip(RoundedCornerShape(8.dp)).background(a.copy(alpha = 0.14f)).pressable(onClick).padding(horizontal = 9.dp, vertical = 4.dp),
        color = a, maxLines = 1,
    )
}

@Composable
fun FilaEtiquetas(generos: List<String>, modifier: Modifier = Modifier, alTocar: (String) -> Unit) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        generos.forEach { g -> EtiquetaObsidian(g) { alTocar(g) } }
    }
}

// ───────────── Mapa de lectura (Anx Reader / Readest) ─────────────

/**
 * Cuadrícula de días leídos (semanas en columnas, de lunes a domingo en filas), del más antiguo a hoy.
 * La intensidad del acento sigue a los capítulos leídos ese día. Se dibuja en un solo Canvas.
 */
@Composable
fun MapaLectura(almacen: Almacen, comics: List<Comic>, modifier: Modifier = Modifier, semanas: Int = 20) {
    val dias = remember(almacen.historialLinea, comics, semanas) { lecturasPorDia(almacen, comics, semanas * 7) }
    val maximo = remember(dias) { (dias.maxOrNull() ?: 0).coerceAtLeast(1) }
    val total = remember(dias) { dias.sum() }
    val activos = remember(dias) { dias.count { it > 0 } }
    val acento = MaterialTheme.colorScheme.primary
    val vacio = LectorColors.CardHigh
    var entro by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entro = true }
    val t by animateFloatAsState(if (entro || Mov.reducido) 1f else 0f, tween(700), label = "mapa")
    // Hoy cae en la fila de su día de la semana (lunes = 0); la última columna puede quedar a medias.
    val hoyFila = remember { (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 }
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(22.dp)).background(LectorColors.Card).padding(16.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            LText("Mapa de lectura", LectorType.Section, Modifier.weight(1f))
            LText("$total caps. · $activos días", EstiloClave, color = LectorColors.Muted)
        }
        Spacer(Modifier.height(12.dp))
        Canvas(Modifier.fillMaxWidth().aspectRatio(semanas / 7f)) {
            val paso = size.width / semanas
            val lado = paso * 0.78f
            val r = CornerRadius(lado * 0.28f)
            for (col in 0 until semanas) for (fila in 0 until 7) {
                // Días hacia atrás desde hoy para esta casilla.
                val atras = (semanas - 1 - col) * 7 + (hoyFila - fila)
                if (atras < 0 || atras >= dias.size) continue
                val n = dias[atras]
                val aparicion = ((t * (semanas + 4) - col) / 4f).coerceIn(0f, 1f)
                val c = if (n == 0) vacio else acento.copy(alpha = 0.28f + 0.72f * (n.toFloat() / maximo))
                drawRoundRect(c.copy(alpha = c.alpha * aparicion), Offset(col * paso, fila * paso), Size(lado, lado), r)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LText("hace ${semanas} semanas", EstiloClave, Modifier.weight(1f), color = LectorColors.Muted)
            listOf(0f, 0.35f, 0.65f, 1f).forEach { f ->
                Box(Modifier.padding(start = 3.dp).size(10.dp).clip(RoundedCornerShape(3.dp)).background(if (f == 0f) vacio else acento.copy(alpha = 0.28f + 0.72f * f)))
            }
        }
    }
}

// ───────────── Catálogo ─────────────

fun catalogoSilencio(almacen: Almacen, comics: List<Comic>): List<FxItem> {
    fun i(id: String, n: String, u: String, v: @Composable () -> Unit) = FxItem(id, n, "Silencio", u, vista = v)
    return listOf(
        i("si_obsidiana", "Tema Obsidiana", "Toda la app en grafito (#1E1E1E) con líneas finas y acento violeta, como Obsidian. Con la paleta «Musicfy» el acento pasa a violeta; las demás paletas se respetan") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Color(0xFF1E1E1E), Color(0xFF262626), Color(0xFF363636), Color(0xFFA88BFA)).forEach {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(it).border(1.dp, Color(0xFF363636), RoundedCornerShape(10.dp)))
                }
            }
        },
        i("si_propiedades", "Propiedades y #etiquetas", "Ficha de un manga: estado, autor, fuente y capítulos como las propiedades de una nota de Obsidian, y los géneros como #etiquetas") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PropiedadesFicha(listOf("Estado" to "En publicación", "Autor" to "Shiori", "Fuente" to "MangaDex", "Capítulos" to "120 · 87 leídos"))
                FilaEtiquetas(listOf("Acción", "Slice of life", "Fantasía")) {}
            }
        },
        i("si_mapa", "Mapa de lectura", "Historial: cuadrícula de las últimas 20 semanas; cada casilla se enciende según los capítulos que leíste ese día (idea de Anx Reader y Readest, vistos en Orion Store)") {
            MapaLectura(almacen, comics)
        },
    )
}
