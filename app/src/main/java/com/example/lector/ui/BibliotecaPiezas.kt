package com.example.lector.ui

// Celdas de la biblioteca de Silencio: libros en un estante (portada con sombra suave, borde finísimo, insignias
// discretas arriba) y, debajo, título y progreso en monoespaciada. La lista es la misma idea en una fila.

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lector.Almacen
import com.example.lector.Comic
import com.example.lector.Vista
import com.example.lector.ext.bandera

@Composable
private fun Insignias(c: Comic, almacen: Almacen, descargas: Int, modifier: Modifier) {
    val acento = MaterialTheme.colorScheme.primary
    val sobre = textoSobreSil(acento)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!c.enLinea && almacen.insigniaLocal) Insignia("LOCAL", Color(0xE6262626), Color.White)
        if (c.enLinea && almacen.insigniaDescargas && descargas > 0) Insignia("↓$descargas", Color(0xD9000000), Color.White)
        if (c.enLinea && almacen.insigniaSinLeer && c.sinLeer > 0) Insignia("${c.sinLeer}", acento, sobre)
        if (almacen.insigniaProgreso && !c.enLinea) {
            val p = (c.progresoLectura() * 100).toInt()
            if (c.terminado()) Insignia("✓", acento, sobre) else if (p in 1..99) Insignia("$p%", Color(0xD9000000), Color.White)
        }
    }
}

@Composable
private fun Insignia(texto: String, fondo: Color, tinta: Color) {
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(fondo).padding(horizontal = 6.dp, vertical = 2.dp), Alignment.Center) {
        Text(texto, color = tinta, style = EstiloDatos.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold), maxLines = 1)
    }
}

@Composable
private fun BotonSeguir(onClick: () -> Unit, modifier: Modifier = Modifier, tam: androidx.compose.ui.unit.Dp = 30.dp) {
    Box(modifier.size(tam).clip(CircleShape).background(Color.White).pressable(onClick), Alignment.Center) {
        Icon(Icons.Rounded.PlayArrow, "Seguir leyendo", tint = Color.Black, modifier = Modifier.size(tam * 0.6f))
    }
}

/** Celda de la rejilla: según la vista, título debajo (cómoda), encima con degradado (compacta) o solo portada. */
@Composable
fun CeldaBiblioteca(
    c: Comic, almacen: Almacen, vista: Vista, seleccionado: Boolean, descargas: Int, lang: String?,
    onClick: () -> Unit, onLargo: () -> Unit, onContinuar: () -> Unit,
) {
    val comoda = vista == Vista.CUADRICULA_COMODA
    val compacta = vista == Vista.CUADRICULA_COMPACTA
    val escala by animateFloatAsState(if (seleccionado) 0.92f else 1f, label = "selCelda")
    val acento = MaterialTheme.colorScheme.primary
    Column(Modifier.graphicsLayer { scaleX = escala; scaleY = escala }.pressable(onLargo, onClick)) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).sombraPortada()) {
            ComicCover(c, almacen, Modifier.matchParentSize())
            Box(Modifier.matchParentSize().border(0.6.dp, Color.White.copy(alpha = 0.08f), FormaPortada))
            Insignias(c, almacen, descargas, Modifier.align(Alignment.TopStart).padding(6.dp))
            if (lang != null && almacen.insigniaIdioma) Text(bandera(lang), Modifier.align(Alignment.TopEnd).padding(6.dp), fontSize = 12.sp)
            if (compacta) {
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.45f).clip(FormaPortada)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                    Text(c.titulo, style = EstiloFilaTitulo.copy(fontSize = 13.sp, lineHeight = 16.sp), color = Color.White, maxLines = 2)
                    if (almacen.mostrarPagina) Text(c.etiquetaProgreso(), style = EstiloMono, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
                }
            }
            val puedeSeguir = almacen.botonContinuar && !seleccionado && !c.terminado() && (c.enLinea && c.paginas > 0 || !c.enLinea)
            if (puedeSeguir && !compacta) BotonSeguir(onContinuar, Modifier.align(Alignment.BottomEnd).padding(6.dp))
            if (seleccionado) {
                Box(Modifier.matchParentSize().clip(FormaPortada).background(acento.copy(alpha = 0.25f)).border(2.dp, acento, FormaPortada))
                MarcaSeleccion(true, Modifier.align(Alignment.Center))
            }
        }
        if (comoda) {
            Spacer(Modifier.height(7.dp))
            Text(c.titulo, style = EstiloTarjeta, color = LectorColors.Text, maxLines = 2)
            if (almacen.mostrarPagina) Text(c.etiquetaProgreso(), style = EstiloMono, color = LectorColors.Muted, maxLines = 1)
        }
    }
}

/** Fila de la vista de lista. */
@Composable
fun FilaBiblioteca(
    c: Comic, almacen: Almacen, seleccionado: Boolean, descargas: Int, lang: String?,
    onClick: () -> Unit, onLargo: () -> Unit, onContinuar: () -> Unit,
) {
    val sub = listOfNotNull(
        if (lang != null && almacen.insigniaIdioma) bandera(lang) else null,
        c.autor?.takeIf { it.isNotBlank() }, if (almacen.mostrarPagina) c.etiquetaProgreso() else null,
    ).joinToString(" · ")
    FilaPortada(
        c.titulo, sub, onClick, Modifier.padding(horizontal = MargenLateral), onLongClick = onLargo,
        selected = if (seleccionado) 1f else 0f,
        cover = { ComicCover(c, almacen, Modifier.size(TamPortadaFila, TamPortadaFila * 1.4f), formaPortada(0.7f)) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Insignias(c, almacen, descargas, Modifier.padding(end = 4.dp))
                if (almacen.botonContinuar && !seleccionado && !c.terminado()) AccionFila(Icons.Rounded.PlayArrow, "Seguir leyendo", onContinuar)
            }
        },
    )
}
