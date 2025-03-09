#!/usr/bin/env bash

# downloadBz2.sh: Downloads bzip2 tarball and verifies its SHA checksum

BZIP2_VERSION="$1"
EXPECTED_SHA="$2"  # Provide the expected SHA as the second argument

if [ -z "$BZIP2_VERSION" ] || [ -z "$EXPECTED_SHA" ]; then
  echo "Usage: $0 <BZIP2_VERSION> <EXPECTED_SHA>"
  exit 1
fi

TARGET_DIR="bzip2-${BZIP2_VERSION}"

if [ ! -d "${TARGET_DIR}" ]; then
    echo "Downloading bzip2 ${BZIP2_VERSION} ..."
    TMP_FILE=$(mktemp)
    URL="http://sourceware.org/pub/bzip2/bzip2-${BZIP2_VERSION}.tar.gz"
    curl -L "$URL" -o "$TMP_FILE"

    echo "Verifying bzip2-${BZIP2_VERSION}..."
    sha256_actual="$(shasum -a 256 "${TMP_FILE}" | awk '{print $1}')"
    if [[ "${EXPECTED_SHA}" != "${sha256_actual}" ]]; then
      echo "Error: tarball checksum mismatch!" >&2
      echo "  expected: ${EXPECTED_SHA}" >&2
      echo "  actual:   ${sha256_actual}" >&2
      exit 1
    fi

    tar -C "./" -xzf "$TMP_FILE"
    rm "$TMP_FILE"
    echo "bzip2 ${BZIP2_VERSION} downloaded and extracted successfully!"
else
    echo "bzip2 ${BZIP2_VERSION} has already been downloaded!"
fi
