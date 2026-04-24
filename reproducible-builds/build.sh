#!/bin/bash

set -eu

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/../"
cd "$ROOT_DIR"
rm -rf "$ROOT_DIR/dist"
mkdir -p "$ROOT_DIR/dist"

if command -v podman >/dev/null; then
	ENGINE=podman
	USER=
else
	ENGINE=docker
	USER="--user $(id -u):$(id -g)"
fi

VERSION=$(sed -n 's/\s*version\s*=\s*"\(.*\)".*/\1/p' build.gradle.kts | tail -n1)
echo "$VERSION" >dist/VERSION

# Build native-image
$ENGINE build -t signal-cli:native -f reproducible-builds/native.Containerfile .
git clean -Xfd -e '!/dist/' -e '!/dist/**' -e '!/github/' -e '!/github/**'
# shellcheck disable=SC2086
$ENGINE run --pull=never --rm -v "$(pwd)":/signal-cli:Z -e VERSION="$VERSION" $USER signal-cli:native
mv build/signal-cli-*-Linux-native.tar.gz dist/

ls -lsh dist/

echo -e "\e[32mBuild successful!\e[0m"
