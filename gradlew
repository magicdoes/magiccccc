#!/usr/bin/env sh
set -eu
GRADLE_VERSION=9.2.1
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BOOT="$ROOT/.gradle-bootstrap"
ZIP="$BOOT/gradle-$GRADLE_VERSION-bin.zip"
HOME="$BOOT/gradle-$GRADLE_VERSION"

if [ ! -x "$HOME/bin/gradle" ]; then
  mkdir -p "$BOOT"
  echo "Downloading Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -L --fail "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  else
    echo "Need curl or wget to download Gradle." >&2
    exit 1
  fi
  echo "Extracting Gradle..."
  unzip -qo "$ZIP" -d "$BOOT"
fi

exec "$HOME/bin/gradle" "$@"
