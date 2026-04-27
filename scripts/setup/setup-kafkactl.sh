#!/bin/bash
set -e

# Install kafkactl v5.18.0 (https://github.com/deviceinsight/kafkactl)

VERSION="v5.18.0"
INSTALL_DIR="$HOME/data/software/kafkactl"

mkdir -p "$INSTALL_DIR"
curl -Lo /tmp/kafkactl.tar.gz "https://github.com/deviceinsight/kafkactl/releases/download/${VERSION}/kafkactl_${VERSION#v}_linux_amd64.tar.gz"
tar -xzf /tmp/kafkactl.tar.gz -C /tmp kafkactl
mv /tmp/kafkactl "$INSTALL_DIR/kafkactl"
chmod +x "$INSTALL_DIR/kafkactl"
rm /tmp/kafkactl.tar.gz

echo "kafkactl $("$INSTALL_DIR/kafkactl" version) installed to $INSTALL_DIR/kafkactl."
