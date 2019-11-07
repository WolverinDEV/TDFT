cmake_minimum_required(VERSION 3.2)
project(FOPTester-Native)

set(CMAKE_CXX_STANDARD 17)

include_directories(${JAVA_INCLUDE_DIR})
if(NOT WIN32)
	include_directories(${JAVA_INCLUDE_DIR}/linux)
	set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/../resources/")
else()
	include_directories(${JAVA_INCLUDE_DIR}/win32)
endif()
add_library(native SHARED native.cpp)
if (WIN32)
	if(CMAKE_SYSTEM_PROCESSOR MATCHES "win32")
		set(DLL_SUFFIX "_32")
	else()
		set(DLL_SUFFIX "_64")
	endif()
	add_custom_command(TARGET native
			POST_BUILD
			COMMAND ${CMAKE_COMMAND} -E copy "native.dll" "${CMAKE_SOURCE_DIR}/../resources/native${DLL_SUFFIX}.dll"
	)
endif ()