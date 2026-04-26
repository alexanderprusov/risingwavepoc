#!/bin/bash
set -e

# Install kubectl v1.35.4

VERSION="v1.35.4"
curl -Lo /tmp/kubectl "https://dl.k8s.io/release/${VERSION}/bin/linux/amd64/kubectl"
chmod +x /tmp/kubectl
sudo mv /tmp/kubectl /usr/local/bin/kubectl

echo "kubectl $(kubectl version --client --short) installed."
