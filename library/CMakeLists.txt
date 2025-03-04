cmake_minimum_required(VERSION 3.10)
project(RocksDBWithDependencies)

set(lz4_INCLUDE_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/../lz4/lz4/lz4-1.10.0/lib")
set(lz4_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../lz4/lz4/lz4-1.10.0")
set(lz4_LIBRARIES "${CMAKE_CURRENT_SOURCE_DIR}/../lz4/build/intermediates/library_and_local_jars_jni/release/copyReleaseJniLibsProjectAndLocalJars/jni/${ANDROID_ABI}/liblz4.so")

include_directories(${lz4_ROOT_DIR}/lib)

# Include the RocksDB project
add_subdirectory(../rocksdb rocksdb-build)
