# Silencio — sistema de diseño de Shiori

Todo el diseño está hecho desde cero. Piezas en `app/src/main/java/com/example/lector/ui/`:

- `Theme.kt` — `LectorColors` (Claro, **Grafito** #1E1E1E como Obsidian, Negro puro) y `LectorType` (Inter; títulos 32 sp apretados).
- `Piezas.kt` — medidas (`MargenLateral` 20 dp), estilos (`EstiloDatos` monoespaciado para metadatos), `Regla` (línea de 0,6 dp),
  botones (`BotonCirculo`, `BotonLeer`), filas (`FilaPortada`, `FilaCapitulo`), gestos (`DeslizarCapitulo`), búsqueda
  (`CampoBuscar`, `CabeceraBusqueda`), resultados, vacíos, tarjetas de portada, menús y aparición.
- `FichaPiezas.kt` — la ficha como página de álbum: portada nítida centrada sobre su versión desenfocada.
- `BibliotecaPiezas.kt` — celdas de estante (sombra suave, borde fino, insignias monoespaciadas).
- `Lectura.kt` — propiedades y #etiquetas (Obsidian) y mapa de lectura (Anx Reader / Readest).
- `PaginaPerfil.kt` — páginas de extensión y fuente.
- `Aspecto.kt` — preferencias de aspecto, animaciones y gestos de capítulos.

Reglas: monocromo; el color lo ponen las portadas y el acento. Separaciones con líneas finas, no con sombras.
Metadatos en monoespaciada y en mayúsculas. Nada de ondas de Material: pulsar encoge o vela.
