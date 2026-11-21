# MapMyShots

MapMyShots findet Fotos ohne GPS-Daten in der Galerie und hilft, ihnen nachträglich einen Ort zu geben:
entweder von einem zeitlich benachbarten Foto mit Standort (Fenster 1 h / 4 h / 12 h) oder manuell auf der Karte.
Fotos lassen sich außerdem ignorieren oder löschen.

Kotlin Multiplatform + Compose Multiplatform für Android, iOS und Desktop (JVM).

- `composeApp/src/commonMain`: UI, ViewModels und Services (geteilt)
- `composeApp/src/{androidMain,iosMain,desktopMain}`: Foto-, EXIF- und Geocoder-Zugriff pro Plattform
- `iosApp`: iOS-Einstiegspunkt (Xcode)

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

Terminal helpers for this repo:

- List connected iPhones/iPads:
  ```shell
  ./scripts/ios-list-devices.sh
  ```
- Build, install, and launch on the iOS Simulator:
  ```shell
  ./scripts/ios-run-simulator.sh "iPhone 16"
  ```
- Build, install, and launch on a physical iPhone:
  ```shell
  APPLE_TEAM_ID=YOUR_TEAM_ID ./scripts/ios-run-device.sh YOUR_DEVICE_UDID
  ```

Notes:

- The Xcode project is `iosApp/iosApp.xcodeproj` and the shared scheme is `iosApp`.
- Physical-device deployment still requires Xcode, Apple signing, and a trusted device pairing.
- If signing is already configured in Xcode for your account, you may not need to pass `APPLE_TEAM_ID`.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
