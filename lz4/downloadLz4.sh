#!/usr/bin/env bash

LZ4_TARGET_DIRECTORY="lz4"
LZ4_VERSION=$1

if [ ! -d "${LZ4_TARGET_DIRECTORY}/lz4-${LZ4_VERSION}" ]; then
    echo "Downloading lz4 ${LZ4_VERSION} into $LZ4_TARGET_DIRECTORY ..."
    mkdir -p "$LZ4_TARGET_DIRECTORY"
    curl -s -L "https://github.com/lz4/lz4/archive/refs/tags/v${LZ4_VERSION}.tar.gz" | tar -C "$LZ4_TARGET_DIRECTORY" -xz
else
    echo "lz4 ${LZ4_VERSION} has already been downloaded!"
fi
