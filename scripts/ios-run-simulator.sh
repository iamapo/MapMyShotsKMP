#!/bin/sh
set -eu

PROJECT="iosApp/iosApp.xcodeproj"
SCHEME="iosApp"
CONFIGURATION="${CONFIGURATION:-Debug}"
DERIVED_DATA="${DERIVED_DATA:-build/ios-simulator-derived-data}"
SIMULATOR_NAME="${1:-iPhone 16}"

xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration "$CONFIGURATION" \
  -destination "platform=iOS Simulator,name=$SIMULATOR_NAME" \
  -derivedDataPath "$DERIVED_DATA" \
  build

APP_PATH="$DERIVED_DATA/Build/Products/${CONFIGURATION}-iphonesimulator/MapMyShots.app"
BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$APP_PATH/Info.plist")"

xcrun simctl boot "$SIMULATOR_NAME" >/dev/null 2>&1 || true
xcrun simctl install booted "$APP_PATH"
xcrun simctl launch booted "$BUNDLE_ID"
