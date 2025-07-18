#!/bin/bash
# SPDX-License-Identifier: MIT

set -e

cd "../.." # ./eclipse/ide-plugins/$root

pwd
./gradlew provideOpenAPIJavaClientForEclipse