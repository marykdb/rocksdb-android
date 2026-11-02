#!/usr/bin/env bash

set -euo pipefail

LZ4_TARGET_DIRECTORY="lz4"
LZ4_VERSION="${1:-}"
EXPECTED_SHA="${2:-}"

if [ -z "$LZ4_VERSION" ] || [ -z "$EXPECTED_SHA" ]; then
    echo "Usage: $0 <LZ4_VERSION> <EXPECTED_SHA>"
    exit 1
fi

if [ ! -d "${LZ4_TARGET_DIRECTORY}/lz4-${LZ4_VERSION}" ]; then
    echo "Downloading lz4 ${LZ4_VERSION} into $LZ4_TARGET_DIRECTORY ..."
    mkdir -p "$LZ4_TARGET_DIRECTORY"
    TMP_FILE=$(mktemp)
    trap 'rm -f "$TMP_FILE"' EXIT
    curl --silent --fail --location "https://github.com/lz4/lz4/archive/refs/tags/v${LZ4_VERSION}.tar.gz" -o "$TMP_FILE"

    echo "Verifying lz4-${LZ4_VERSION}..."
    sha256_actual="$(shasum -a 256 "$TMP_FILE" | awk '{print $1}')"
    if [[ "$EXPECTED_SHA" != "$sha256_actual" ]]; then
        echo "Error: tarball checksum mismatch!" >&2
        echo "  expected: ${EXPECTED_SHA}" >&2
        echo "  actual:   ${sha256_actual}" >&2
        exit 1
    fi

    tar -C "$LZ4_TARGET_DIRECTORY" -xzf "$TMP_FILE"
    rm "$TMP_FILE"
    trap - EXIT
    echo "lz4 ${LZ4_VERSION} downloaded and extracted successfully!"
else
    echo "lz4 ${LZ4_VERSION} has already been downloaded!"
fi
