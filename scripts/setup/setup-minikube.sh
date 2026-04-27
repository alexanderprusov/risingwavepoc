#!/bin/bash
set -e

# Install minikube v1.38.1

VERSION="v1.38.1"
INSTALL_DIR="$HOME/data/software/minikube"

mkdir -p "$INSTALL_DIR"
curl -Lo "$INSTALL_DIR/minikube" "https://storage.googleapis.com/minikube/releases/${VERSION}/minikube-linux-amd64"
chmod +x "$INSTALL_DIR/minikube"

echo "minikube $("$INSTALL_DIR/minikube" version --short) installed to $INSTALL_DIR/minikube."
