#!/usr/bin/env bash

VERSION_NAME=$(echo $1 | sed -e 's/\./\\\./g')
VERSION_CODE="$2"

USAGE_HELPER="usage: $0 <version_name> <version_code>"

if [ `basename $(pwd)` = "scripts" ]; then
    cd ..
fi

if [[ -z $1 ]] || [[ -z $2 ]]; then
    echo $USAGE_HELPER
    exit 1
fi

parsed_changelog=$(pcregrep -M "^^### \[$VERSION_NAME\] - .*\n((?!#).|\n)*" CHANGELOG.md | tail -n +2)

if [[ -z $parsed_changelog ]]; then
	echo "Cannot find $1"
    echo $USAGE_HELPER
    exit 1
fi

mkdir -p ./metadata/en-US/changelogs/

echo "$parsed_changelog" > "./metadata/en-US/changelogs/${VERSION_CODE}.txt"

echo "Changelog for v${1} written in ./metadata/en-US/changelogs/${VERSION_CODE}.txt"