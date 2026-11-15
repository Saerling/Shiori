# Seguridad — Shiori

| # | Punto | Estado |
|---|---|---|
| 1 | Ocultar API keys | ✅ No hay claves de API en el código; el Client ID de AniList lo pone cada usuario. |
| 2 | Secretos fuera de Git | ⚠️ **La clave de firma `app/firma/shiori.jks` y su contraseña están en el repo** (se hizo para que cualquier compilación actualice encima). La compilación ya acepta la firma desde secretos (`SHIORI_KEYSTORE_B64`, `SHIORI_KEYSTORE_PASSWORD`, `SHIORI_KEY_ALIAS`); ver «Firma» abajo. |
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

Cualquiera puede firmar un APK con la clave pública del repo y Android lo aceptaría como actualización de Shiori. Para cerrarlo:

1. **Mantener la clave pero sacarla del repo** no sirve: ya está en el historial.
2. **Rotar la clave** (recomendado): crear una clave nueva, guardarla en los secretos de GitHub y publicar. Los usuarios tendrían que desinstalar e instalar **una vez** (o usar rotación v3 con `apksigner rotate` para que la actualización entre sin desinstalar en Android 9+).
