#!/bin/bash
set -e

# Install Helm v3.17.3

VERSION="v3.17.3"
curl -Lo /tmp/helm.tar.gz "https://get.helm.sh/helm-${VERSION}-linux-amd64.tar.gz"
tar -xzf /tmp/helm.tar.gz -C /tmp
sudo mv /tmp/linux-amd64/helm /usr/local/bin/helm
rm -rf /tmp/helm.tar.gz /tmp/linux-amd64

helm repo add risingwavelabs https://risingwavelabs.github.io/helm-charts
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

echo "Helm $(helm version --short) installed."
