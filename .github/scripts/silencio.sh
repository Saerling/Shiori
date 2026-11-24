#!/bin/bash
# Recorrido completo de Shiori Silencio en el emulador: todas las pestañas, Más, Ajustes y sus páginas, Explorar con una
# extensión real (Alejandría · Eternal Mangas), catálogo, ficha, lector, biblioteca, búsqueda global y un monkey test.
# Deja capturas numeradas (cap-NN-nombre.png), el logcat y un resumen en fallos.txt.
PKG=com.example.lector.silencio
N=0
R=resultado.txt
: > $R
adb install -r app/build/outputs/apk/silencio/app-silencio.apk
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS 2>/dev/null
adb shell appops set $PKG REQUEST_INSTALL_PACKAGES allow 2>/dev/null
# Extensión real desde el repo propio (Alejandría).
curl -sL -o ext.apk https://raw.githubusercontent.com/Saerling/Alejandria/repo/apk/tachiyomi-es.eternalmangas-v1.4.2.apk && adb install -r ext.apk && echo "extensión instalada" >> $R
adb logcat -c

abrir() { adb shell am start -n $PKG/com.example.lector.MainActivity >/dev/null; sleep "${1:-8}"; }
vivo() { [ -n "$(adb shell pidof $PKG)" ]; }
cap() { N=$((N+1)); f=$(printf "cap-%02d-%s.png" $N "$1"); adb exec-out screencap -p > "$f"
  if vivo; then echo "$1: ok" >> $R; else echo "$1: SE CERRÓ" >> $R; adb logcat -d | grep -A30 "FATAL EXCEPTION" | tail -32 >> $R; abrir 10; fi; }
dump() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml ui.xml >/dev/null 2>&1; }
centro() { # bounds del primer nodo con ese texto/descripción (coincidencia exacta)
  grep -oE "(text|content-desc)=\"$1\"[^>]*bounds=\"\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]\"" ui.xml | head -1 | grep -oE '\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]'; }
tocar() { # toca el nodo; si no está, desplaza hacia abajo hasta 6 veces
  local b=""
  for k in 1 2 3 4 5 6 7; do dump; b=$(centro "$1"); [ -n "$b" ] && break; adb shell input swipe 540 1700 540 900 300; sleep 1; done
  if [ -z "$b" ]; then echo "  no encontré «$1»" >> $R; return 1; fi
  set -- $(echo $b | grep -oE '[0-9]+'); adb shell input tap $(( ($1+$3)/2 )) $(( ($2+$4)/2 )); sleep 2; return 0; }
mantener() { dump; local b=$(centro "$1"); [ -z "$b" ] && { echo "  no encontré «$1» para mantener" >> $R; return 1; }
  set -- $(echo $b | grep -oE '[0-9]+'); local x=$(( ($1+$3)/2 )) y=$(( ($2+$4)/2 )); adb shell input swipe $x $y $x $y 900; sleep 2; }
atras() { adb shell input keyevent 4; sleep 2; }
arriba() { for k in 1 2 3; do adb shell input swipe 540 700 540 1800 200; done; sleep 1; }
bajar() { adb shell input swipe 540 1700 540 700 400; sleep 2; }

abrir 12
cap inicio-vacio
for t in Biblioteca Explorar Novedades Historial; do tocar "$t" && sleep 2; cap "tab-$t"; done

# ── Más y cada fila
tocar "Más"; cap mas; bajar; cap mas-abajo; arriba
for f in "Categorías" "Estadísticas" "Migraciones recientes" "Datos y almacenamiento" "Acerca de"; do
  arriba; tocar "$f" && { sleep 2; cap "mas-$f"; bajar; cap "mas-$f-abajo"; atras; }
done

# ── Ajustes y sus páginas
arriba; tocar "Ajustes"; cap ajustes
for f in "Aspecto" "Biblioteca" "Visor" "Descargas" "Seguimiento" "Explorar" "Datos y almacenamiento" "Seguridad y privacidad" "Avanzado" "Acerca de"; do
  arriba; tocar "$f" && { sleep 2; cap "aj-$f"; bajar; cap "aj-$f-2"; bajar; cap "aj-$f-3"; atras; }
done
# Tema claro y negro puro, para ver los tres.
arriba; tocar "Aspecto" && { tocar "Claro"; cap aspecto-claro; tocar "Negro"; cap aspecto-negro; tocar "Grafito"; cap aspecto-grafito; atras; }
atras

# ── Explorar: extensiones, fuentes, catálogo, ficha y lector
tocar "Explorar"; cap explorar
tocar "Extensiones"; sleep 4; cap explorar-extensiones
tocar "Confiar" && cap confiar
tocar "18+" 2>/dev/null
tocar "Fuentes"; sleep 2; cap explorar-fuentes
tocar "Eternal Mangas" && { sleep 10; cap catalogo; bajar; sleep 4; cap catalogo-abajo; arriba
  # Mantener pulsada la primera portada: menú.
  dump; p=$(grep -oE 'bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"' ui.xml | sed -n '40p'); echo "  portada: $p" >> $R
  adb shell input swipe 270 900 270 900 900; sleep 2; cap catalogo-mantener; atras
  adb shell input tap 270 900; sleep 10; cap ficha
  bajar; cap ficha-2; bajar; cap ficha-3; bajar; cap ficha-capitulos; arriba
  tocar "Añadir a la biblioteca" && cap ficha-en-biblioteca
  # Leer: el botón principal.
  dump; leer=$(grep -oE '(text)="(Empezar a leer|Continuar[^"]*|Leer)"' ui.xml | head -1 | cut -d'"' -f2); echo "  botón leer: $leer" >> $R
  [ -n "$leer" ] && tocar "$leer" && { sleep 12; cap lector; adb shell input tap 540 1200; sleep 2; cap lector-barras; adb shell input tap 980 1200; sleep 3; cap lector-pag2; atras; sleep 2; cap tras-lector; }
  # Deslizar un capítulo (gesto).
  bajar; adb shell input swipe 200 1500 800 1500 300; sleep 2; cap ficha-deslizar
  atras; atras; }

# ── Con un manga en la biblioteca
tocar "Biblioteca"; sleep 3; cap biblioteca-llena
tocar "Inicio"; sleep 3; cap inicio-lleno
tocar "Historial"; sleep 3; cap historial-lleno
tocar "Novedades"; sleep 3; cap novedades

# ── Búsqueda global
tocar "Explorar"; tocar "Búsqueda global" && { adb shell input text "solo"; adb shell input keyevent 66; sleep 15; cap busqueda-global; atras; }

# ── Rotación (tablet / horizontal)
adb shell settings put system accelerometer_rotation 0; adb shell settings put system user_rotation 1; sleep 4; cap horizontal
adb shell settings put system user_rotation 0; sleep 3

# ── Monkey: 2500 toques al azar para cazar cierres
adb shell monkey -p $PKG --throttle 120 --pct-syskeys 0 --pct-appswitch 2 -s 7 -v 2500 > monkey.txt 2>&1
cap tras-monkey

adb logcat -d > logcat-silencio.txt
{ echo "===== Recorrido ====="; cat $R
  echo "===== Cierres (FATAL) ====="; grep -B2 -A30 "FATAL EXCEPTION" logcat-silencio.txt | head -200
  echo "===== Monkey ====="; grep -E "CRASH|ANR|Monkey finished|Events injected" monkey.txt | head -20
  echo "===== Errores de Compose / avisos graves ====="; grep -E "E AndroidRuntime|IllegalArgument|IllegalState|NullPointer|IndexOutOfBounds" logcat-silencio.txt | head -40
  echo "===== ultimo_fallo ====="; adb shell run-as $PKG cat files/ultimo_fallo.txt 2>/dev/null | head -80; } > fallos.txt
cat fallos.txt
