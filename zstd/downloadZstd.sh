#!/usr/bin/env bash

# downloadZstd.sh: Downloads zstd tarball and verifies its SHA checksum

ZSTD_VERSION="$1"
EXPECTED_SHA="$2"  # Provide the expected SHA as the second argument

if [ -z "$ZSTD_VERSION" ] || [ -z "$EXPECTED_SHA" ]; then
  echo "Usage: $0 <ZSTD_VERSION> <EXPECTED_SHA>"
  exit 1
fi

TARGET_DIR="zstd-${ZSTD_VERSION}"

if [ ! -d "${TARGET_DIR}" ]; then
    echo "Downloading zstd ${ZSTD_VERSION} ..."
    TMP_FILE=$(mktemp)
    URL="https://github.com/facebook/zstd/releases/download/v${ZSTD_VERSION}/zstd-${ZSTD_VERSION}.tar.gz"
    curl -s -L "$URL" -o "$TMP_FILE"

    echo "Verifying zstd-${ZSTD_VERSION}..."
    sha256_actual="$(shasum -a 256 "${TMP_FILE}" | awk '{print $1}')"
    if [[ "${EXPECTED_SHA}" != "${sha256_actual}" ]]; then
      echo "Error: tarball checksum mismatch!" >&2
      echo "  expected: ${EXPECTED_SHA}" >&2
      echo "  actual:   ${sha256_actual}" >&2
      exit 1
    fi

    tar -C "./" -xzf "$TMP_FILE"
    rm "$TMP_FILE"
    echo "zstd ${ZSTD_VERSION} downloaded and extracted successfully!"
else
    echo "zstd ${ZSTD_VERSION} has already been downloaded!"
fi
