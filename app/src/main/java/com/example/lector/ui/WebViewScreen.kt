package com.example.lector.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import eu.kanade.tachiyomi.network.NetworkHelper

private const val UA_ESCRITORIO =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36"

/**
 * WebView dentro de la app (como el de Komikku, con más controles): sirve para ver el sitio de una fuente
 * (también antes de instalar su extensión), resolver Cloudflare o iniciar sesión. Usa el mismo agente de usuario
 * que las peticiones de las fuentes, así que las cookies que consiga (cf_clearance, sesión…) les sirven.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String, tituloInicial: String, alVolver: () -> Unit) {
    val ctx = LocalContext.current
    var web by remember { mutableStateOf<WebView?>(null) }
    var progreso by remember { mutableStateOf(0f) }
    var cargandoPagina by remember { mutableStateOf(true) }
    var titulo by remember { mutableStateOf(tituloInicial) }
    var actual by remember { mutableStateOf(url) }
    var puedeAtras by remember { mutableStateOf(false) }
    var puedeAdelante by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var escritorio by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    fun refrescarNav(v: WebView) { puedeAtras = v.canGoBack(); puedeAdelante = v.canGoForward() }

    BackHandler { if (web?.canGoBack() == true) web?.goBack() else alVolver() }
    DisposableEffect(Unit) {
        onDispose {
            CookieManager.getInstance().flush()
            web?.apply { stopLoading(); destroy() }
        }
    }

    val host = remember(actual) { runCatching { Uri.parse(actual).host?.removePrefix("www.") }.getOrNull() ?: actual }
    val seguro = actual.startsWith("https://")

    Column(Modifier.fillMaxSize().background(LectorColors.Bg)) {
        // Barra superior: cerrar · título y dominio · menú
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotonRedondo(Icons.Rounded.Close, "Cerrar") { alVolver() }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LText(titulo, LectorType.Section.copy(fontSize = 16.sp), maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (seguro) Icon(Icons.Rounded.Lock, "Conexión segura", Modifier.size(12.dp), tint = LectorColors.Muted)
                    else Icon(Icons.Rounded.Warning, "Conexión no segura", Modifier.size(12.dp), tint = Color(0xFFE0A030))
                    Spacer(Modifier.width(4.dp))
                    LText(host, LectorType.Label, color = LectorColors.Muted, maxLines = 1)
                }
            }
            Box {
                BotonRedondo(Icons.Rounded.MoreVert, "Más") { menu = true }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Copiar enlace") }, onClick = {
                        menu = false
                        (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("Enlace", actual))
                        Avisos.mostrar("Enlace copiado")
                    })
                    DropdownMenuItem(text = { Text("Compartir") }, onClick = {
                        menu = false
                        runCatching {
                            ctx.startActivity(
                                Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, actual), null)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    })
                    DropdownMenuItem(text = { Text("Abrir en el navegador") }, onClick = { menu = false; abrirFuera(ctx, actual) })
                    DropdownMenuItem(
                        text = { Text(if (escritorio) "Versión para móvil" else "Versión de escritorio") },
                        onClick = {
                            menu = false
                            escritorio = !escritorio
                            web?.settings?.userAgentString = if (escritorio) UA_ESCRITORIO else NetworkHelper.DEFAULT_USER_AGENT
                            web?.reload()
                        },
                    )
                    DropdownMenuItem(text = { Text("Borrar cookies de este sitio") }, onClick = {
                        menu = false
                        borrarCookies(actual)
                        Avisos.mostrar("Cookies de $host borradas")
                        web?.reload()
                    })
                }
            }
        }
        // Progreso de carga
        Box(Modifier.fillMaxWidth().height(2.dp)) {
            if (cargandoPagina && progreso in 0.01f..0.99f) {
                Box(Modifier.fillMaxWidth(progreso).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { c ->
                    WebView(c).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportMultipleWindows(false)
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            // El mismo que usan las fuentes: así las cookies de Cloudflare les sirven.
                            userAgentString = NetworkHelper.DEFAULT_USER_AGENT
                        }
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        setDownloadListener { u, _, _, _, _ -> abrirFuera(ctx, u) }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                                val u = r.url.toString()
                                if (u.startsWith("http://") || u.startsWith("https://")) return false
                                abrirFuera(ctx, u) // intent:, market:, mailto:…
                                return true
                            }
                            override fun onPageStarted(v: WebView, u: String, favicon: Bitmap?) {
                                actual = u; cargandoPagina = true; error = null; refrescarNav(v)
                            }
                            override fun onPageFinished(v: WebView, u: String) {
                                actual = u; cargandoPagina = false; refrescarNav(v)
                                CookieManager.getInstance().flush()
                            }
                            override fun doUpdateVisitedHistory(v: WebView, u: String, recarga: Boolean) {
                                actual = u; refrescarNav(v)
                            }
                            override fun onReceivedError(v: WebView, r: WebResourceRequest, e: WebResourceError) {
                                if (r.isForMainFrame) error = e.description?.toString() ?: "No se pudo cargar la página"
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(v: WebView, p: Int) { progreso = p / 100f }
                            override fun onReceivedTitle(v: WebView, t: String?) { if (!t.isNullOrBlank()) titulo = t }
                        }
                        loadUrl(url)
                        web = this
                    }
                },
            )
            error?.let { msg ->
                Column(
                    Modifier.fillMaxSize().background(LectorColors.Bg).padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LText("No se pudo abrir la página", LectorType.Section.copy(fontSize = 18.sp))
                    Spacer(Modifier.height(8.dp))
                    LText(msg, LectorType.Label, color = LectorColors.Muted)
                    Spacer(Modifier.height(20.dp))
                    BotonRedondo(Icons.Rounded.Refresh, "Reintentar") { error = null; web?.reload() }
                }
            }
        }
        // Barra inferior de navegación
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotonRedondo(Icons.Rounded.ArrowBack, "Atrás", activo = puedeAtras) { web?.goBack() }
            BotonRedondo(Icons.Rounded.ArrowForward, "Adelante", activo = puedeAdelante) { web?.goForward() }
            BotonRedondo(
                if (cargandoPagina) Icons.Rounded.Close else Icons.Rounded.Refresh,
                if (cargandoPagina) "Detener" else "Recargar",
            ) { if (cargandoPagina) web?.stopLoading() else web?.reload() }
            BotonRedondo(Icons.Rounded.Home, "Inicio") { web?.loadUrl(url) }
            Box(
                Modifier.size(44.dp).pressable { abrirFuera(ctx, actual) }.clip(CircleShape).background(Color(0x22FFFFFF)),
                Alignment.Center,
            ) { IconoGlobo(LectorColors.Text) }
        }
    }
}

@Composable
private fun BotonRedondo(icono: androidx.compose.ui.graphics.vector.ImageVector, desc: String, activo: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).alpha(if (activo) 1f else 0.35f)
            .then(if (activo) Modifier.pressable(onClick) else Modifier)
            .clip(CircleShape).background(Color(0x22FFFFFF)),
        Alignment.Center,
    ) { Icon(icono, desc, tint = LectorColors.Text) }
}

/** Icono de globo (web), dibujado a mano porque el set básico de iconos no lo trae. */
@Composable
fun IconoGlobo(color: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        val g = size.minDimension * 0.08f
        val r = size.minDimension / 2 - g
        val c = center
        val trazo = Stroke(width = g)
        drawCircle(color, r, c, style = trazo)
        drawOval(color, Offset(c.x - r * 0.45f, c.y - r), Size(r * 0.9f, r * 2), style = trazo)
        drawLine(color, Offset(c.x - r, c.y), Offset(c.x + r, c.y), g)
        drawLine(color, Offset(c.x - r * 0.85f, c.y - r * 0.5f), Offset(c.x + r * 0.85f, c.y - r * 0.5f), g)
        drawLine(color, Offset(c.x - r * 0.85f, c.y + r * 0.5f), Offset(c.x + r * 0.85f, c.y + r * 0.5f), g)
    }
}

private fun abrirFuera(ctx: Context, u: String) {
    runCatching {
        val intent = if (u.startsWith("intent:")) Intent.parseUri(u, Intent.URI_INTENT_SCHEME) else Intent(Intent.ACTION_VIEW, Uri.parse(u))
        ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure { Avisos.mostrar("No hay ninguna app para abrir esto") }
}

private fun borrarCookies(url: String) {
    val cm = CookieManager.getInstance()
    val host = runCatching { Uri.parse(url).host }.getOrNull() ?: return
    val dominios = buildList {
        val partes = host.split('.')
        for (i in 0 until partes.size - 1) add(partes.drop(i).joinToString("."))
    }
    dominios.forEach { d ->
        cm.getCookie("https://$d")?.split(';')?.map { it.substringBefore('=').trim() }?.filter { it.isNotEmpty() }?.forEach { n ->
            cm.setCookie("https://$d", "$n=; Max-Age=0; Path=/")
            cm.setCookie("https://$d", "$n=; Max-Age=0; Path=/; Domain=.$d")
        }
    }
    cm.flush()
}
