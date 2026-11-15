#!/usr/bin/env bash
# Uso: probar-firma.sh <api>. Necesita vieja.apk y rotada.apk en la carpeta actual.
set -u
api=$1; pkg=$(aapt2 dump packagename vieja.apk 2>/dev/null || "$BT/aapt2" dump packagename vieja.apk)
res() { echo "$1" | tee -a "$GITHUB_STEP_SUMMARY"; }
falla() { res "❌ API $api: $1"; exit 1; }
abre() {
  adb shell am force-stop "$pkg"; adb shell monkey -p "$pkg" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 8
  adb shell pidof "$pkg" >/dev/null || falla "la app no abre ($1)"
}
adb uninstall "$pkg" >/dev/null 2>&1
adb install vieja.apk | grep -q Success || falla "no instala la vieja"
abre "vieja"
adb install -r rotada.apk | grep -q Success || falla "la rotada NO se instala encima de la vieja"
abre "tras actualizar"
res "✅ API $api: la vieja se actualiza a la rotada sin desinstalar y la app abre"
if [ "$api" -ge 28 ]; then
  out=$(adb install -r vieja.apk 2>&1)
  echo "$out" | grep -q Success && falla "un APK con solo la clave vieja todavía entra encima"
  res "✅ API $api: un APK firmado solo con la clave vieja ya no se puede instalar encima ($(echo "$out" | grep -o 'INSTALL_FAILED_[A-Z_]*' | head -1))"
else
  res "ℹ️ API $api: Android 8 no conoce la rotación; sigue con la clave vieja (esperado)"
fi
