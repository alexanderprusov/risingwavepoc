#!/bin/bash
set -e

# Install protoc (Protocol Buffers compiler) v3.25.5
# Binary sourced from the Gradle dependency cache (com.google.protobuf:protoc:3.25.5).
# To use: run ./gradlew :app:generateProto once so Gradle downloads the binary, then run this script.

VERSION="3.25.5"
INSTALL_DIR="$HOME/data/software/protoc/bin"
GRADLE_CACHE="$HOME/.gradle/caches/modules-2/files-2.1/com.google.protobuf/protoc/${VERSION}"

SRC=$(find "$GRADLE_CACHE" -name "protoc-${VERSION}-linux-x86_64.exe" | head -1)
if [ -z "$SRC" ]; then
  echo "protoc binary not found in Gradle cache. Run: ./gradlew :app:generateProto" >&2
  exit 1
fi

mkdir -p "$INSTALL_DIR"
cp "$SRC" "$INSTALL_DIR/protoc"
chmod +x "$INSTALL_DIR/protoc"

echo "protoc $("$INSTALL_DIR/protoc" --version) installed to $INSTALL_DIR/protoc."
