NormalizeMaps_Invisible
=======================

Minimal Android app that appears in the Share menu, extracts latitude/longitude from shared text,
and launches Google Maps with a stable offline-friendly URL: http://maps.google.com/?q=LAT,LNG

Features:
- No launcher icon (hidden from app drawer)
- Transparent activity (no UI flash)
- Fully offline
- Minimal permissions

Build:
1. Open this folder in Android Studio.
2. Build -> Build Bundle(s) / APK(s) -> Build APK(s).
3. Install the generated APK on your Android device.

Notes:
- The project uses Kotlin and targets SDK 34.
- If Android Studio prompts to download SDK/build tools, allow it.
