cmake_minimum_required(VERSION 3.10)
project(bzip2_shared C)

# If BZ2_PATH is defined, assume it points to the bzip2 lib folder.
# The actual bzip2 source files are located in the parent directory of BZ2_PATH.
if(DEFINED BZ2_PATH)
    set(BZIP2_ROOT ${BZ2_PATH})
else()
    set(BZIP2_ROOT ${CMAKE_SOURCE_DIR})
endif()

message(STATUS "BZIP2_ROOT set to ${BZIP2_ROOT}")

# List of source files relative to BZIP2_ROOT
set(SOURCES
    ${BZIP2_ROOT}/blocksort.c
    ${BZIP2_ROOT}/huffman.c
    ${BZIP2_ROOT}/crctable.c
    ${BZIP2_ROOT}/randtable.c
    ${BZIP2_ROOT}/compress.c
    ${BZIP2_ROOT}/decompress.c
    ${BZIP2_ROOT}/bzlib.c
)

# Create a shared library named 'bz2' (resulting in libbz2.so on Unix-like systems)
add_library(bz2 SHARED ${SOURCES})

# Set compiler options matching the Makefile:
target_compile_options(bz2 PRIVATE -Wall -Winline -O2 -g)
target_compile_definitions(bz2 PRIVATE _FILE_OFFSET_BITS=64)

# Set the output name to 'bz2'
set_target_properties(bz2 PROPERTIES OUTPUT_NAME "bz2")

# Installation rules (optional)
install(TARGETS bz2
        LIBRARY DESTINATION lib
        ARCHIVE DESTINATION lib)
