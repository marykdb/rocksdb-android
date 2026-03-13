cmake_minimum_required (VERSION 3.10)

add_subdirectory(snappy-1.2.1 snappy)

# RocksDB uses RTTI for snappy::Sink (typeinfo symbol), but upstream snappy
# forces -fno-rtti. Re-enable RTTI for this target to satisfy shared linking.
if(TARGET snappy)
  target_compile_options(snappy PRIVATE "$<$<COMPILE_LANGUAGE:CXX>:-frtti>")
endif()

file(COPY ${CMAKE_BINARY_DIR}/snappy/snappy-stubs-public.h DESTINATION ${CMAKE_SOURCE_DIR}/include)
