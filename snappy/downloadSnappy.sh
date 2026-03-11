#!/usr/bin/env bash

set -euo pipefail

DEFAULT_SNAPPY_VER="1.2.2"
DEFAULT_SNAPPY_SHA256="90f74bc1fbf78a6c56b3c4a082a05103b3a56bb17bca1a27e052ea11723292dc"
DEFAULT_SNAPPY_DOWNLOAD_BASE="https://github.com/google/snappy/archive"

SNAPPY_VER="${1:-${SNAPPY_VER:-$DEFAULT_SNAPPY_VER}}"
SNAPPY_SHA256="${2:-${SNAPPY_SHA256:-$DEFAULT_SNAPPY_SHA256}}"
SNAPPY_DOWNLOAD_BASE="${SNAPPY_DOWNLOAD_BASE:-$DEFAULT_SNAPPY_DOWNLOAD_BASE}"

tarball="${SNAPPY_VER}.tar.gz"
target_path="snappy-${SNAPPY_VER}"

if [ -d "${target_path}" ]; then
    echo "snappy ${SNAPPY_VER} has already been downloaded!"
    exit 0
fi

echo "Downloading snappy-${SNAPPY_VER}..."
if curl --silent --fail --location -o "${tarball}" "${SNAPPY_DOWNLOAD_BASE}/${tarball}"; then
    echo "Verifying snappy-${SNAPPY_VER}..."
    sha256_actual="$(shasum -a 256 "${tarball}" | awk '{print $1}')"
    if [[ "${SNAPPY_SHA256}" != "${sha256_actual}" ]]; then
        echo "Error: ${tarball} checksum mismatch!" >&2
        echo "  expected: ${SNAPPY_SHA256}" >&2
        echo "  actual:   ${sha256_actual}" >&2
        exit 1
    fi
else
    echo "Error downloading snappy-${SNAPPY_VER}!" >&2
    exit 1
fi

tar xzf "${tarball}" -C "./" > /dev/null
echo "snappy ${SNAPPY_VER} downloaded and extracted successfully!"
