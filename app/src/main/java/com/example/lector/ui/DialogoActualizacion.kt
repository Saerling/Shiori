package com.example.lector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lector.NuevaVersion
import com.example.lector.NuevaVersion.Paso

/** Aviso de versión nueva: hoja con novedades, avance de la descarga e instalación encima de la actual. */
@Composable
fun DialogoActualizacion() {
    val v = NuevaVersion.disponible
    if (!NuevaVersion.verDialogo || v == null) return
    val ctx = LocalContext.current
    val paso = NuevaVersion.paso
    val cerrar = { if (NuevaVersion.paso != Paso.DESCARGANDO) NuevaVersion.cerrar() }
    val actualizar = { NuevaVersion.descargarEInstalar(ctx) }
    val cuerpo = when (paso) {
        Paso.DESCARGANDO -> "Descargando… ${NuevaVersion.porcentaje} %"
        Paso.INSTALANDO -> "Confirma la instalación en la ventana de Android. La app se cerrará y se abrirá ya actualizada."
        Paso.ERROR -> NuevaVersion.error ?: "No se pudo instalar Shiori ${v.nombre}."
        Paso.LISTO -> "Tienes la ${NuevaVersion.nombreActual(ctx)}."
    }
    HojaLector(cerrar) {
        Column(Modifier.padding(horizontal = MargenLateral).padding(bottom = 16.dp)) {
            LText("VERSIÓN NUEVA", EstiloDatos, color = LectorColors.Muted)
            LText("Shiori ${v.nombre}", LectorType.Display)
            Spacer(Modifier.height(4.dp))
            LText(
                listOfNotNull("compilación ${v.codigo}", if (v.tamano > 0) "%.1f MB".format(v.tamano / 1048576f) else null).joinToString(" · "),
                EstiloDatos, color = LectorColors.Muted,
            )
            Spacer(Modifier.height(14.dp))
            LText(cuerpo, EstiloCuerpo, color = if (paso == Paso.ERROR) LectorColors.Error else LectorColors.Text)
            if (paso == Paso.DESCARGANDO) {
                Spacer(Modifier.height(12.dp))
                ProgresoLinea(progress = { NuevaVersion.porcentaje / 100f }, modifier = Modifier.fillMaxWidth())
            }
            if (v.notas.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Regla()
                Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()).padding(vertical = 12.dp)) {
                    LText(v.notas, EstiloCuerpo, color = LectorColors.Muted)
                }
                Regla()
            }
            Spacer(Modifier.height(18.dp))
            when (paso) {
                Paso.DESCARGANDO -> Unit
                else -> {
                    BotonLeer(if (paso == Paso.LISTO) "Actualizar" else "Reintentar", actualizar, Modifier.fillMaxWidth(), icono = Icons.Rounded.KeyboardArrowDown)
                    Spacer(Modifier.height(8.dp))
                    BotonExpresivo("Más tarde", null, cerrar, Modifier.fillMaxWidth(), EstiloBoton.TEXTO)
                }
            }
        }
    }
}
