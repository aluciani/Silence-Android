#!/usr/bin/env bash

if [ `basename $(pwd)` = "scripts" ]; then
    cd ..
fi

TAGS=$(git tag | sed -n '/unstable$/!p')

for tag in $TAGS
do
	version_name=$(echo "$tag" | sed 's/^v//')
    echo "Changelog for $tag..."
    git checkout tags/"$tag" build.gradle
    version_code=$(grep versionCode build.gradle | sed 's/ *versionCode //')
    echo "Version code for $version_name is $version_code, writing changelog"
    ./scripts/write-changelog.sh $version_name $version_code
    git restore --staged build.gradle
    git restore build.gradle
done

