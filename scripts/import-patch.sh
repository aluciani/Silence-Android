#!/usr/bin/env bash

# Import a patch from TextSecure and adapt it for Silence
# ../Signal-Android must be an cloned git tree
# usage: ./scripts/import-patch.sh <commit SHAs>

set -eo pipefail

UPSTREAM="https://github.com/signalapp/Signal-Android"

if [ "$#" -lt 1 ]; then
    echo "usage: ./scripts/import-patch.sh <commit SHAs>"
    exit 1
fi

if [ `basename $(pwd)` = "scripts" ]; then
    cd ..
fi

cwd=`pwd`

for sha in "$@"; do
    wget "$UPSTREAM/commit/$sha.patch" 2> /dev/null
    $cwd/scripts/fix-patch.sh "$cwd/$sha.patch"
done
