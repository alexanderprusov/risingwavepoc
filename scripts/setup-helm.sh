#!/bin/bash
set -e

# Install Helm v3.17.3

VERSION="v3.17.3"
INSTALL_DIR="$HOME/data/software/helm"

mkdir -p "$INSTALL_DIR"
curl -Lo /tmp/helm.tar.gz "https://get.helm.sh/helm-${VERSION}-linux-amd64.tar.gz"
tar -xzf /tmp/helm.tar.gz -C /tmp
mv /tmp/linux-amd64/helm "$INSTALL_DIR/helm"
rm -rf /tmp/helm.tar.gz /tmp/linux-amd64

"$INSTALL_DIR/helm" repo add risingwavelabs https://risingwavelabs.github.io/helm-charts
"$INSTALL_DIR/helm" repo add bitnami https://charts.bitnami.com/bitnami
"$INSTALL_DIR/helm" repo update

echo "Helm $("$INSTALL_DIR/helm" version --short) installed to $INSTALL_DIR/helm."
