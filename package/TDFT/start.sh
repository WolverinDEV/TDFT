#!/bin/bash

# Test for Java
java -version &> /dev/null

# shellcheck disable=SC2181
[[ $? -ne 0 ]] && {
  echo "It seems like java hasn't been installed on your system."
  echo "This application requires at least Java 8!"
  exit 1
}

# Get the right file
cd $(dirname "$0")
if [[ -e "tdfp-gui.jar" ]]; then
  file="tdfp-gui.jar"
elif [[ -e "tdfp-cli.jar" ]]; then
  file="tdfp-cli.jar"
else
  echo "Failed to find TDFP file!"
  exit 1
fi

# Execute application
java -jar ${file} --plugin="./plugins/"