package com.example.lector.ui

// La ficha de Silencio, como la página de un álbum: portada nítida centrada con sombra sobre su propia versión
// desenfocada que se funde con el fondo; título, autor y datos; «Leer» y acciones; luego propiedades, #etiquetas
// y los capítulos como una lista de pistas. Arriba, una barra que aparece al desplazar.

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Alto de la zona de la portada en la ficha. */
@Composable
fun altoPortadaFicha(): Dp = (LocalConfiguration.current.screenHeightDp * 0.42f).dp.coerceIn(260.dp, 420.dp)

/** 0..1: cuánto se ha plegado la cabecera de la ficha (se lee al dibujar). */
@Composable
fun rememberPlegadoFicha(lista: LazyListState, alto: Dp): State<Float> {
    val px = with(LocalDensity.current) { alto.toPx() * 0.8f }
    return remember(lista, px) {
        derivedStateOf { if (lista.firstVisibleItemIndex > 0) 1f else (lista.firstVisibleItemScrollOffset / px).coerceIn(0f, 1f) }
    }
}

/** Fondo de la ficha: la portada desenfocada a sangre que se funde con el color de fondo. */
@Composable
fun FondoFicha(portadaSuave: @Composable (Modifier) -> Unit, alto: Dp, progreso: () -> Float, fondo: State<Color>, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(alto + 120.dp).clipToBounds().graphicsLayer { alpha = 1f - progreso() * 0.8f; translationY = -progreso() * alto.toPx() * 0.3f }) {
        portadaSuave(
            Modifier.matchParentSize().graphicsLayer { alpha = 0.55f; scaleX = 1.2f; scaleY = 1.2f }
                .then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(48.dp) else Modifier),
        )
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to fondo.value.copy(alpha = 0.2f), 0.55f to fondo.value.copy(alpha = 0.8f), 0.85f to fondo.value, 1f to fondo.value)))
    }
}

/** Cabecera de la ficha: portada nítida centrada, título, autor, datos, «Leer» y acciones. */
@Composable
fun CabeceraFichaS(
    portada: @Composable (Modifier) -> Unit,
    title: String, creators: List<String>, stats: String,
    playLabel: String, playEnabled: Boolean, onPlayClick: () -> Unit,
    acento: Color,
    description: String? = null, onDescriptionLongClick: () -> Unit = {},
    onCoverClick: () -> Unit = {}, onTitleClick: () -> Unit = {}, onTitleLongClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val alto = altoPortadaFicha()
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val forma = SuaveShape(12.dp)
        portada(
            Modifier.height(alto - 40.dp).aspectRatio(2f / 3f)
                .shadow(24.dp, forma, clip = false, ambientColor = Color.Black.copy(alpha = 0.5f), spotColor = Color.Black.copy(alpha = 0.6f))
                .clip(forma).border(0.6.dp, Color.White.copy(alpha = 0.1f), forma).clickable(onClick = onCoverClick),
        )
        Spacer(Modifier.height(18.dp))
        Box(Modifier.padding(horizontal = MargenFicha).tocar(onTitleLongClick, onTitleClick)) {
            LText(title, LectorType.Hero.copy(textAlign = TextAlign.Center), maxLines = 3)
        }
        if (creators.isNotEmpty()) LText(creators.joinToString(", "), LectorType.Body.copy(fontSize = 16.sp, textAlign = TextAlign.Center), Modifier.padding(horizontal = MargenFicha, vertical = 2.dp), color = acento, maxLines = 1)
        if (stats.isNotEmpty()) LText(stats.uppercase(), EstiloDatos.copy(textAlign = TextAlign.Center), Modifier.padding(horizontal = MargenFicha), color = LectorColors.Muted, maxLines = 1)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.padding(horizontal = MargenFicha).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BotonLeer(playLabel, onPlayClick, Modifier.weight(1f), habilitado = playEnabled)
            Spacer(Modifier.width(6.dp))
            actions()
        }
        if (!description.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().padding(horizontal = MargenFicha).clickable(onClick = onDescriptionLongClick)) {
                LText(description, EstiloCuerpo, color = LectorColors.Text.copy(alpha = 0.78f), maxLines = 4)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Estado de la selección para la barra de la ficha. */
class SeleccionFicha(val cuenta: Int, val todo: Boolean, val alternarTodo: () -> Unit, val invertir: () -> Unit, val cerrar: () -> Unit)

/** Barra de arriba de la ficha: atrás; al desplazar aparece el fondo y el título; eligiendo, la barra de selección. */
@Composable
fun BarraFicha(
    progreso: () -> Float, titulo: String, fondo: State<Color>, onBack: () -> Unit,
    seleccion: SeleccionFicha?, trailing: @Composable RowScope.() -> Unit,
) {
    val p = if (seleccion != null) 1f else progreso()
    Box(Modifier.fillMaxWidth()) {
        Box(Modifier.matchParentSize().graphicsLayer { alpha = p }.background(fondo.value.copy(alpha = 0.96f)))
        if (p > 0.95f) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(0.6.dp).background(LectorColors.Linea))
        AnimatedContent(seleccion != null, label = "barraFicha") { eligiendo ->
            Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (eligiendo && seleccion != null) {
                    BotonCirculo(Icons.Rounded.Close, "Cerrar selección", seleccion.cerrar)
                    Spacer(Modifier.width(12.dp))
                    LText("${seleccion.cuenta} elegidos", EstiloFilaTitulo.copy(fontSize = 17.sp), Modifier.weight(1f))
                    BotonCirculo(Icons.Rounded.Refresh, "Invertir selección", seleccion.invertir)
                    Spacer(Modifier.width(8.dp))
                    BotonCirculo(Icons.Rounded.Check, "Elegir todos", seleccion.alternarTodo, activo = seleccion.todo)
                } else {
                    BotonCirculo(Icons.Rounded.ArrowBack, "Atrás", onBack, relleno = LectorColors.Bg.copy(alpha = 0.6f))
                    Spacer(Modifier.width(12.dp))
                    LText(titulo, EstiloFilaTitulo.copy(fontSize = 17.sp), Modifier.weight(1f).graphicsLayer { alpha = progreso() }, maxLines = 1)
                    trailing()
                }
            }
        }
    }
}

/** Botón redondo de la barra de la ficha (sobre la portada lleva fondo translúcido). */
@Composable
fun BotonBarraFicha(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, onClick: () -> Unit, modifier: Modifier = Modifier, lleno: Boolean = false) =
    BotonCirculo(icono, descripcion, onClick, modifier, activo = lleno, relleno = LectorColors.Bg.copy(alpha = 0.6f))
