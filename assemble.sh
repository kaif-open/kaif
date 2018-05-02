#!/bin/bash

CUR_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

$CUR_DIR/gradlew clean assemble \
  -Dorg.gradle.java.home=/Users/ingram/develop/jdk-all/adoptopenjdk/jdk8u144-b01

