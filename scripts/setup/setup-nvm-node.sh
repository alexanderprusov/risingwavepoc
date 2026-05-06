#!/bin/bash
set -e

NVM_VERSION="v0.40.3"
NVM_DIR="${NVM_DIR:-$HOME/.nvm}"

echo "==> Installing nvm $NVM_VERSION"
curl -fsSL "https://raw.githubusercontent.com/nvm-sh/nvm/$NVM_VERSION/install.sh" | bash

export NVM_DIR
# shellcheck source=/dev/null
[ -s "$NVM_DIR/nvm.sh" ] && source "$NVM_DIR/nvm.sh"

echo "==> Installing latest Node.js"
nvm install node
nvm use node
nvm alias default node

echo "==> Done"
node --version
npm --version
