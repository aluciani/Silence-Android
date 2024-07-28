#!/usr/bin/env bash

# Try to apply an upstream patches
# usage: ./scripts/apply-patch.sh <commit SHAs>

if [ "$#" -lt 1 ]; then
    echo "usage: ./scripts/apply-patch.sh <commit SHAs>"
    exit 1
fi

for sha in "$@"; do
    EXISTS=$(git --no-pager log --all --grep=$sha --pretty=%H)
    if [ "$EXISTS" == "" ]; then
        ./scripts/import-patch.sh $sha
        git apply --check -v $sha.patch 2> /dev/null
        if [ $(echo $?) != "0" ]; then
            echo -e "Cannot apply $sha.\n\nRun \`git apply --check -v $sha.patch\` to check why the patch fails to apply, then \`git am $sha.patch\` when it is fixed."
            exit 1
        fi
        git am $sha.patch >>/dev/null 2> /dev/null
        rm $sha.patch
        echo "$sha applied successfully"
    else
        echo "$sha exists in $EXISTS. Skipping..."
    fi
done
