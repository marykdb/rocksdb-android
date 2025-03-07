PROJECT(LZ4 C)
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "LZ4 compression library")
include(CPack)

cmake_minimum_required (VERSION 3.10)
INCLUDE (CheckTypeSize)
check_type_size("void *" SIZEOF_VOID_P)
IF(${SIZEOF_VOID_P} STREQUAL "8")
    set (CMAKE_SYSTEM_PROCESSOR "64bit")
    MESSAGE(STATUS "64 bit architecture detected, size of void * is " ${SIZEOF_VOID_P})
ENDIF()

IF("${CMAKE_C_COMPILER_ID}" STREQUAL "GNU" OR "${CMAKE_C_COMPILER_ID}" STREQUAL "Clang")
    SET(GNU_COMPATIBLE_COMPILER 1)
ENDIF()

if(GNU_COMPATIBLE_COMPILER)
    if(UNIX AND BUILD_LIBS)
        add_definitions(-fPIC)
    endif()
endif()

set(LZ4_SRCS_LIB ${LZ4_PATH}lz4.c ${LZ4_PATH}lz4hc.c ${LZ4_PATH}lz4.h ${LZ4_PATH}lz4hc.h ${LZ4_PATH}lz4frame.c ${LZ4_PATH}lz4frame.h ${LZ4_PATH}xxhash.c)

add_library(liblz4 SHARED ${LZ4_SRCS_LIB})

set_target_properties(liblz4 PROPERTIES
        OUTPUT_NAME lz4
        SOVERSION "${CPACK_PACKAGE_VERSION_MAJOR}.${CPACK_PACKAGE_VERSION_MINOR}"
        )

INCLUDE_DIRECTORIES (${LZ4_PATH})
