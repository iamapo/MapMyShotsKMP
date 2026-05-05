#!/bin/sh
set -eu

if [ "${1:-}" = "" ]; then
  echo "Usage: $0 <device-udid-or-name>" >&2
  echo "Optional env: APPLE_TEAM_ID=<team id> CONFIGURATION=Debug|Release" >&2
  exit 1
fi

DEVICE="$1"
PROJECT="iosApp/iosApp.xcodeproj"
SCHEME="iosApp"
CONFIGURATION="${CONFIGURATION:-Debug}"
DERIVED_DATA="${DERIVED_DATA:-build/ios-device-derived-data}"
TEAM_ID_VALUE="${APPLE_TEAM_ID:-${TEAM_ID:-}}"

set -- \
  xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration "$CONFIGURATION" \
  -destination "id=$DEVICE" \
  -derivedDataPath "$DERIVED_DATA" \
  build

if [ -n "$TEAM_ID_VALUE" ]; then
  set -- "$@" DEVELOPMENT_TEAM="$TEAM_ID_VALUE" TEAM_ID="$TEAM_ID_VALUE"
fi

"$@"

APP_PATH="$DERIVED_DATA/Build/Products/${CONFIGURATION}-iphoneos/MapMyShots.app"
BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$APP_PATH/Info.plist")"

xcrun devicectl device install app --device "$DEVICE" "$APP_PATH"
xcrun devicectl device process launch --device "$DEVICE" "$BUNDLE_ID" --terminate-existing
