#!/usr/bin/env bash

if [ `basename $(pwd)` = "scripts" ]; then
    cd ..
fi

rm ./libs/gradle-witness.jar 2>/dev/null

set -eo pipefail

cd ./libs/gradle-witness
../../gradlew jar
cp ./build/libs/gradle-witness.jar ../gradle-witness.jar
