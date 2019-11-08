#!/bin/bash

cd $(dirname "$0")

function resolve_latest() {
  if [[ ! -d $1 ]]; then
    echo "Missing directory $1. May build first?"
    exit 1
  fi

  # shellcheck disable=SC2012
  file=$(ls -t "$1" | grep -E "$2\S+\.jar" | head -n1)
  if [[ -z ${file} ]]; then
    echo "Failed to find .jar file beginning with $2 in $1. Sure you've compiled everyting?"
    exit 1
  fi

  file="$1/$file"
}

function cleanup_package() {
  rm TDFT/tdfp-*.jar
  rm TDFT/plugins/*.jar
}


function install_plugins() {
  mkdir -p "TDFT/plugins" || {
    echo "failed to create plugin dir"
    exit 1
  }

  #Grep the H3 plugin
  resolve_latest ../tests/h3/target/ h3
  cp "${file}" TDFT/plugins/H03.jar || {
    echo "Failed to copy H03 plugin"
    exit 1
  }
}

function package_cli() {
  resolve_latest ../core/target/ core
  cp "${file}" TDFT/tdfp-cli.jar || {
    echo "Failed to copy the core application"
    exit 1
  }

  zip -r out/TDFT-CLI.zip TDFT/* || {
    echo "Failed to create ZIP archive"
    exit 1
  }
}

function package_gui() {
  resolve_latest ../gui/target/ gui
  cp "${file}" TDFT/tdfp-gui.jar || {
    echo "Failed to copy the gui application"
    exit 1
  }

  zip -r out/TDFT-GUI.zip TDFT/* || {
    echo "Failed to create ZIP archive"
    exit 1
  }
}

if [[ -d "out" ]]; then
  rm -r out/*
else
  mkdir -p out
fi

# The CLI
cleanup_package
install_plugins
package_cli

# The GUI
cleanup_package
install_plugins
package_gui