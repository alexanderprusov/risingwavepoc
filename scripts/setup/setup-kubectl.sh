#!/bin/bash
set -e

# Install kubectl v1.35.4

VERSION="v1.35.4"
INSTALL_DIR="$HOME/data/software/kubectl"

mkdir -p "$INSTALL_DIR"
curl -Lo "$INSTALL_DIR/kubectl" "https://dl.k8s.io/release/${VERSION}/bin/linux/amd64/kubectl"
chmod +x "$INSTALL_DIR/kubectl"

echo "kubectl $("$INSTALL_DIR/kubectl" version --client --short) installed to $INSTALL_DIR/kubectl."
