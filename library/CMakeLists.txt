cmake_minimum_required(VERSION 3.10)
project(RocksDBWithDependencies)

set(lz4_INCLUDE_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/../lz4/lz4/lz4-1.10.0/lib")
set(lz4_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../lz4/lz4/lz4-1.10.0")
set(lz4_LIBRARIES "${CMAKE_CURRENT_SOURCE_DIR}/../lz4/build/intermediates/library_and_local_jars_jni/release/copyReleaseJniLibsProjectAndLocalJars/jni/${ANDROID_ABI}/liblz4.so")

set(Snappy_INCLUDE_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/../snappy/snappy-1.2.1")
set(Snappy_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../snappy/snappy-1.2.1")
set(Snappy_DIR "${Snappy_ROOT_DIR}")
set(Snappy_LIBRARIES "${CMAKE_CURRENT_SOURCE_DIR}/../snappy/build/intermediates/library_and_local_jars_jni/release/copyReleaseJniLibsProjectAndLocalJars/jni/${ANDROID_ABI}/libsnappy.so")

set(BZIP2_INCLUDE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../bz2/bzip2-1.0.8")
set(BZIP2_LIBRARIES "${CMAKE_CURRENT_SOURCE_DIR}/../bz2/build/intermediates/library_and_local_jars_jni/release/copyReleaseJniLibsProjectAndLocalJars/jni/${ANDROID_ABI}/libbz2.so")

set(ZSTD_INCLUDE_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/../zstd/zstd-1.5.7/lib")
set(ZSTD_LIBRARIES "${CMAKE_CURRENT_SOURCE_DIR}/../zstd/build/intermediates/library_and_local_jars_jni/release/copyReleaseJniLibsProjectAndLocalJars/jni/${ANDROID_ABI}/libzstd.so")

include_directories(${lz4_ROOT_DIR}/lib)
include_directories(${Snappy_ROOT_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../snappy/include)

# Include the RocksDB project
add_subdirectory(../rocksdb rocksdb-build)
