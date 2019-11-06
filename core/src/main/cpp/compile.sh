#!/bin/bash
# shellcheck disable=SC2046
cd $(dirname "$0") || exit 1

if [[ -z ${JAVA_INCLUDE_DIR} ]]; then
  echo "Please specify where to find the jni headers. (JAVA_INCLUDE_DIR)"
  exit 1
fi

if [[ ! -e "${JAVA_INCLUDE_DIR}/jni.h" ]]; then
  echo "Invalid java include dir: ${JAVA_INCLUDE_DIR}"
  echo "Missing jni.h"
  exit 1
fi

g++ -c -Wall -Werror -fpic -std=c++17 -I${JAVA_INCLUDE_DIR} -I${JAVA_INCLUDE_DIR}/linux native.cpp
g++ -shared -o ../resources/libnative.so -lstdc++ -static-libstdc++ -static-libgcc native.o -lstdc++fs

# Cleanup
rm native.o