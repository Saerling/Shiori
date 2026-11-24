package com.example.lector.ui

// Página de perfil (extensión, fuente): el icono desenfocado como cabecera, el icono nítido, nombre, datos en
// monoespaciada, acciones, y tarjetas con propiedades al estilo de una nota de Obsidian.

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val AvatarPerfilEn = 0.62f

@Composable
fun altoCabeceraPerfil(fraccion: Float = 0.42f): Dp = (LocalConfiguration.current.screenHeightDp * fraccion).dp.coerceAtMost(340.dp)

@Composable
fun PaginaPerfil(
    title: String, bannerHeight: Dp, scrollProgress: State<Float>, onBack: () -> Unit,
    banner: @Composable BoxScope.() -> Unit, topActions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize().background(LectorColors.Bg)) {
        Box(Modifier.fillMaxWidth().height(bannerHeight).graphicsLayer { alpha = 1f - scrollProgress.value; translationY = -scrollProgress.value * size.height * 0.3f }) {
            banner()
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to LectorColors.Bg.copy(alpha = 0.2f), 1f to LectorColors.Bg)))
        }
        content()
        val p = scrollProgress.value
        Box(Modifier.fillMaxWidth()) {
            Box(Modifier.matchParentSize().graphicsLayer { alpha = p }.background(LectorColors.Bg.copy(alpha = 0.96f)))
            Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                BotonCirculo(Icons.Rounded.ArrowBack, "Atrás", onBack, relleno = LectorColors.Bg.copy(alpha = 0.6f))
                Spacer(Modifier.width(12.dp))
                LText(title, EstiloFilaTitulo, Modifier.weight(1f).graphicsLayer { alpha = p }, maxLines = 1)
                topActions()
            }
            if (p > 0.95f) Regla(Modifier.align(Alignment.BottomCenter))
        }
    }
}

/** Fondo de cabecera: el icono a sangre, muy desenfocado. */
@Composable
fun FondoIcono(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().graphicsLayer { scaleX = 1.4f; scaleY = 1.4f; alpha = 0.6f }.then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(60.dp) else Modifier), content = content)
}

@Composable
fun AvatarPerfil(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.size(96.dp).clip(RoundedCornerShape(22.dp)).background(LectorColors.Card).border(0.6.dp, LectorColors.Linea, RoundedCornerShape(22.dp)), content = content)
}

@Composable
fun NombrePerfil(name: String, modifier: Modifier = Modifier) = LText(name, LectorType.Display, modifier, maxLines = 2)

@Composable
fun DatosPerfil(text: String, modifier: Modifier = Modifier) = LText(text.uppercase(), EstiloDatos, modifier, color = LectorColors.Muted, maxLines = 2)

@Composable
fun AccionesPerfil(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) =
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, content = content)

/** Tarjeta plana con borde fino. */
@Composable
fun TarjetaPerfil(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, tint: Color = LectorColors.Text, content: @Composable BoxScope.() -> Unit) {
    val forma = RoundedCornerShape(14.dp)
    Box(
        modifier.fillMaxWidth().clip(forma).background(LectorColors.Card).border(0.6.dp, if (tint == LectorColors.Text) LectorColors.Linea else tint.copy(alpha = 0.5f), forma)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(14.dp),
        content = content,
    )
}

/** Propiedad «clave  valor» (como en Obsidian). */
@Composable
fun FilaPropiedad(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        LText(label.lowercase(), EstiloDatos, Modifier.width(96.dp), color = LectorColors.Muted, maxLines = 1)
        LText(value, LectorType.Body.copy(fontSize = 14.sp), Modifier.weight(1f), maxLines = 3)
    }
}
