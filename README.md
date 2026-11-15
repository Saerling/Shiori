<p align="center">
  <img src="docs/icono.png" width="112" alt="Shiori">
</p>

<h1 align="center">Shiori</h1>

<p align="center">
  <b>Tu lector de manga y cómics para Android.</b><br>
  Miles de fuentes con las extensiones de Mihon/Komikku, tus archivos locales y un diseño de cristal hecho a mano.<br>
  Sin anuncios, sin rastreadores, de código abierto.
</p>

<p align="center">
  <a href="https://github.com/Saerling/lector/releases/latest"><img src="https://img.shields.io/github/v/release/Saerling/lector?style=for-the-badge&label=versi%C3%B3n&color=111114" alt="Última versión"></a>
  <a href="https://github.com/Saerling/lector/releases"><img src="https://img.shields.io/github/downloads/Saerling/lector/total?style=for-the-badge&label=descargas&color=111114" alt="Descargas"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-111114?style=for-the-badge&logo=android&logoColor=white" alt="Android 8.0+">
  <a href="LICENSE"><img src="https://img.shields.io/badge/licencia-GPL--3.0-111114?style=for-the-badge" alt="GPL-3.0"></a>
</p>

<p align="center">
  <a href="https://github.com/Saerling/lector/releases/latest"><b>⬇ Descargar el APK</b></a>
  &nbsp;·&nbsp;
  <a href="#instalar">Instalar con Orion Store</a>
  &nbsp;·&nbsp;
  <a href="#funciones">Funciones</a>
</p>

---

## Funciones

<table>
<tr>
<td width="50%" valign="top">

### 🧩 Extensiones
Compatible con las extensiones de **Mihon / Komikku**: añade listados, instala, confía y ajusta cada fuente con sus filtros. Tiene un listado propio, [Alejandria](https://github.com/Saerling/Alejandria).

### 🔎 Explorar
Fuentes, novedades, extensiones, migración entre fuentes y **búsqueda global** en todas a la vez.

### 📚 Biblioteca
Categorías (también ocultas), agrupar por fuente o estado, filtros, orden, insignias, búsqueda con operadores y selección múltiple.

</td>
<td width="50%" valign="top">

### 📖 Visor
Paginado ← / →, vertical, webtoon y continuo. Zonas de toque, zoom, recorte de bordes, páginas dobles, autodesplazamiento, teclas de volumen y filtros de color.

### 🔔 Siempre al día
Actualizaciones de la biblioteca en segundo plano con notificaciones y una **cola de descargas** que se conserva.

### 🛡 Tuyo y privado
Historial, modo incógnito, seguimiento con **AniList**, copias de seguridad, bloqueo de la app, pantalla segura y DNS sobre HTTPS.

</td>
</tr>
</table>

### ✨ Personalización
Cristal con desenfoque, tema claro, oscuro, negro puro o del sistema, acento propio o tomado de la portada, animaciones de Mochy Music y una **biblioteca de animaciones y funciones**. En ella activas, una por una, piezas de Magic UI, Motion Primitives, NumberFlow, coss ui, blocks.so y opensourceui, y cada una trae su vista previa.

### 📁 Archivos locales
Lee `.cbz` / `.zip`, PDF y carpetas de imágenes de tu teléfono, sin conexión.

## Instalar

- **Orion Store:** busca *Shiori* en [Orion Store](https://github.com/RookieEnough/Orion-Store) y toca Instalar. Orion avisa de cada versión nueva.
- **GitHub:** descarga el `.apk` de la [última versión](https://github.com/Saerling/lector/releases/latest) y ábrelo.

Después, Shiori se actualiza sola desde *Más → Acerca de → Buscar actualizaciones*. Cada versión nueva se instala encima de la anterior y conserva tus datos.

## Compilar

GitHub Actions compila y publica el APK en cada push a `main` o `claude/komikku`. Para compilar en tu equipo: `gradle assembleRelease`, con Gradle 8.9 y JDK 17.

Para desarrolladores: [`HANDOFF.md`](HANDOFF.md) y la comparación con Komikku en [`FUNCIONES_KOMIKKU.md`](FUNCIONES_KOMIKKU.md).

## Créditos

- [Komikku](https://github.com/komikku-app/komikku) y [Mihon](https://github.com/mihonapp/mihon): partes del motor de fuentes (Apache-2.0).
- [Mochy Music](https://github.com/realidkroo/Mochy-Music), de realidkroo: diseño y animaciones (GPL-3.0).
- Fuente tipográfica [Inter](https://rsms.me/inter/), con licencia SIL Open Font License 1.1.

## Licencia

[GPL-3.0](LICENSE). Shiori no está afiliada a Mihon, a Komikku ni a ninguna de las fuentes de las extensiones.
