cmake_minimum_required (VERSION 3.10)

add_subdirectory(snappy-1.2.1 snappy)

file(COPY ${CMAKE_BINARY_DIR}/snappy/snappy-stubs-public.h DESTINATION ${CMAKE_SOURCE_DIR}/include)
