package com.example.lector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Búsquedas recientes (SharedPreferences «busquedas»), las 12 últimas. */
object BusquedasRecientes {
    private var prefs: android.content.SharedPreferences? = null
    val lista = mutableStateListOf<String>()
    fun iniciar(ctx: android.content.Context) {
        if (prefs != null) return
        val p = ctx.applicationContext.getSharedPreferences("busquedas", android.content.Context.MODE_PRIVATE)
        prefs = p
        lista.addAll(p.getString("recientes", "").orEmpty().split('\n').filter { it.isNotBlank() })
    }
    fun anotar(q: String) {
        val t = q.trim(); if (t.isEmpty()) return
        lista.removeAll { it.equals(t, true) }; lista.add(0, t)
        while (lista.size > 12) lista.removeAt(lista.lastIndex)
        guardar()
    }
    fun quitar(q: String) { lista.remove(q); guardar() }
    fun borrarTodo() { lista.clear(); guardar() }
    private fun guardar() { prefs?.edit()?.putString("recientes", lista.joinToString("\n"))?.apply() }
}

/** Lista de búsquedas recientes: tocar repite, ✕ quita. */
@Composable
fun ChipsRecientes(onElegir: (String) -> Unit, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { BusquedasRecientes.iniciar(ctx) }
    Column(modifier.fillMaxWidth()) {
        CabeceraResultados("Recientes", large = true, ruleAbove = false, trailing = if (BusquedasRecientes.lista.isEmpty()) null else ({
            LText("Borrar", EstiloDatos, Modifier.pressable { BusquedasRecientes.borrarTodo() }.padding(8.dp), color = MaterialTheme.colorScheme.primary)
        }))
        if (BusquedasRecientes.lista.isEmpty()) LText("Escribe un título y pulsa buscar. Tus búsquedas aparecerán aquí.", EstiloCuerpo, Modifier.padding(horizontal = MargenLateral), color = LectorColors.Muted)
        BusquedasRecientes.lista.forEach { q ->
            FilaMenu(icon = { androidx.compose.material3.Icon(Icons.Rounded.Refresh, null, tint = LectorColors.Muted, modifier = Modifier.size(18.dp)) }, enabled = true, onClick = { onElegir(q) },
                trailing = { GlifoCampo(Icons.Rounded.Close, "Quitar", { BusquedasRecientes.quitar(q) }) }) { LText(q, LectorType.Body, maxLines = 1) }
            Regla(inicio = MargenLateral + 36.dp)
        }
    }
}
