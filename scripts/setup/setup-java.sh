#!/bin/bash
set -e

# Install OpenJDK 21 on Debian/Ubuntu

sudo apt-get update
sudo apt-get install -y openjdk-21-jdk

echo "Java $(java -version 2>&1 | head -1) installed."
