#!/bin/bash
set -e

# Install OpenJDK 21 on Debian/Ubuntu

sudo apt-get update
sudo apt-get install -y openjdk-21-jdk

# Gradle probes /usr/lib/jvm/openjdk-21 for the JDK, but apt creates an empty
# stub directory there instead of a real tree. Replace it with a symlink.
if [ -d /usr/lib/jvm/openjdk-21 ] && [ ! -f /usr/lib/jvm/openjdk-21/bin/java ]; then
  sudo rm -rf /usr/lib/jvm/openjdk-21
  sudo ln -s /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/openjdk-21
fi

echo "Java $(java -version 2>&1 | head -1) installed."
