#!/bin/bash
# Prueba rápida de Silencio: abre la app y toca cada pestaña; guarda capturas, el logcat y el informe de fallos.
PKG=com.example.lector.silencio
adb install -r app/build/outputs/apk/silencio/app-silencio.apk
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS
adb logcat -c
adb shell am start -n $PKG/com.example.lector.MainActivity
sleep 12
adb exec-out screencap -p > captura-0-inicio.png
tocar() { # toca el centro del nodo con ese texto o descripción
  for k in 1 2 3; do adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml ui.xml >/dev/null 2>&1; grep -q "$1" ui.xml && break; sleep 2; done
  b=$(grep -oE "(text|content-desc)=\"$1\"[^>]*bounds=\"\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]\"" ui.xml | head -1 | grep -oE '\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]')
  if [ -z "$b" ]; then echo "no encontré «$1» (textos: $(grep -oE '(text|content-desc)="[^"]+"' ui.xml | head -30 | tr '\n' ' '))" >> resultado.txt; return; fi
  set -- $(echo $b | grep -oE '[0-9]+'); adb shell input tap $(( ($1+$3)/2 )) $(( ($2+$4)/2 ))
}
n=1
for t in Biblioteca Actualizaciones Historial Más Ajustes Personalización Explorar Inicio; do
  tocar "$t"; sleep 5
  adb exec-out screencap -p > captura-$n-$t.png; n=$((n+1))
  vivo=$(adb shell pidof $PKG)
  echo "$t: $([ -n "$vivo" ] && echo sigue abierta || echo SE CERRÓ)" >> resultado.txt
  if [ -z "$vivo" ]; then adb shell am start -n $PKG/com.example.lector.MainActivity; sleep 10; fi
done
adb logcat -d > logcat-silencio.txt
{ echo "===== Pestañas ====="; cat resultado.txt; echo "===== Fallos ====="; grep -A40 "FATAL EXCEPTION" logcat-silencio.txt | head -120; echo "===== ultimo_fallo ====="; adb shell run-as $PKG cat files/ultimo_fallo.txt 2>/dev/null | head -80; } > fallos.txt
cat fallos.txt
