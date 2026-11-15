# Seguridad — Shiori

| # | Punto | Estado |
|---|---|---|
| 1 | Ocultar API keys | ✅ No hay claves de API en el código; el Client ID de AniList lo pone cada usuario. |
| 2 | Secretos fuera de Git | ✅ La clave histórica `app/firma/shiori.jks` era pública, así que se **rotó**: los APK publicados llevan una clave nueva (RSA 4096) que solo vive en los secretos de GitHub. La vieja queda en el repo solo como origen de la rotación. Ver «Firma» abajo. |
| 3 | Key pública para DB | N/A — la base de datos es local (Room/SQLite). |
| 4 | Row-Level Security | N/A — sin servidor. |
| 5 | Encriptar datos sensibles | ✅ Tokens de AniList/MAL/Kitsu/MangaUpdates y la contraseña WebDAV se cifran con AES-256-GCM (clave en el Android Keystore). |
| 6 | Forzar autenticación | ✅ AniList solo acepta el token de vuelta si el usuario pidió entrar en los últimos 15 min; MAL usa PKCE. |
| 7 | Restringir acceso a registros | ✅ FileProvider no exportado, solo la carpeta `compartir/`. Sin componentes exportados salvo la pantalla principal. |
| 8 | Bloquear manipulación de campos | ✅ La importación de ajustes solo acepta una lista blanca de claves. |
| 9 | Session cookies | ✅ Las cookies de las fuentes viven en el almacén privado de la app. |
| 10 | Hashear contraseñas | N/A — Shiori no guarda contraseñas propias; las de trackers se cambian por tokens y no se guardan. |
| 11 | Rate limiting | ✅ Límite por fuente en el cliente HTTP (como Mihon), para no saturar webs. |
| 12 | Protección contra bots | N/A — app cliente. |
| 13 | Queries parametrizadas | ✅ Room con parámetros `:x`; sin SQL concatenado. |
| 14 | Validar inputs | ✅ Deep links validados (esquema, host, origen del login). |
| 15 | Sanitizar contenido | ✅ HTML de las fuentes se parsea con Jsoup y se muestra como texto/imagen, nunca se ejecuta. |
| 16 | Restringir archivos | ✅ Extensiones solo desde repos con huella de firma verificada. |
| 17 | Devolver solo lo necesario | ✅ Copias de seguridad sin tokens; las copias de Android excluyen `seguidores` y `bitchord_webdav`. |
| 18 | Security headers | N/A — app nativa. |
| 19 | Forzar HTTPS | ✅ `usesCleartextTraffic=false`; actualizaciones solo desde GitHub por HTTPS y Android verifica la firma del APK. |
| 20 | Escanear dependencias | ✅ Dependabot (Gradle y Actions). |

## Firma (punto 2)

La clave vieja (`shiori.jks`, contraseña `shiori-lector`) estuvo pública, así que cualquiera podía firmar un APK que Android aceptaría como actualización de Shiori. Se cerró con **rotación de firma v3** (Android 9+):

- `app/firma/linaje.bin` es la prueba, firmada con la clave vieja, de que autoriza a la nueva. No es secreta.
- En GitHub Actions el APK se vuelve a firmar: la clave vieja para Android 8 (v2) y la nueva para Android 9+ (v3).
- Quien tenga Shiori instalado se actualiza encima **sin desinstalar ni perder nada**.
- Desde esa actualización, la clave vieja ya no tiene permiso de *rollback*: un APK firmado solo con ella es rechazado por Android.
- Android 8 (API 26-27) no soporta rotación: esos equipos siguen con la clave vieja.
- `Probar rotación de firma` (workflow) lo comprueba en emuladores Android 8 y 14.

Secretos necesarios en *Settings → Secrets and variables → Actions*:

| Secreto | Valor |
|---|---|
| `SHIORI_KEYSTORE_B64` | el archivo `shiori-2026.jks` en base64 |
| `SHIORI_KEYSTORE_PASSWORD` | su contraseña (alias `shiori2`) |

Huellas SHA-256: vieja `3fdd72a1…0479391e`, nueva `e68d6bed…e5fd40ed20`.

⚠️ Guarda una copia de `shiori-2026.jks` y su contraseña fuera de GitHub (los secretos no se pueden volver a leer). Si se pierde, no habrá más actualizaciones encima.
