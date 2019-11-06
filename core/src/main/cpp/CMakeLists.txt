cmake_minimum_required(VERSION 3.2)
project(FOPTester-Native)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/../resources/")
include_directories(${JAVA_INCLUDE_DIR})
if(NOT WIN32)
	include_directories(${JAVA_INCLUDE_DIR}/linux)
endif()
add_library(native SHARED native.cpp)