#!/bin/bash
set -e

# Install minikube v1.38.1

VERSION="v1.38.1"
curl -Lo /tmp/minikube "https://storage.googleapis.com/minikube/releases/${VERSION}/minikube-linux-amd64"
chmod +x /tmp/minikube
sudo mv /tmp/minikube /usr/local/bin/minikube

echo "minikube $(minikube version --short) installed."
