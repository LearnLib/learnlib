#!/bin/bash

LTSMIN_NAME="ltsmin-v3.0.0-$TRAVIS_OS_NAME.tgz"
LTSMIN_URL="https://github.com/Meijuh/ltsmin/releases/download/v3.0.0/$LTSMIN_NAME"

# test if we have a cached version
test -f "$HOME/ltsmin/bin/ltsmin-convert" -a -f "$HOME/ltsmin/bin/etf2lts-mc" && exit 0

# create the directoy where the binaries end up.
mkdir -p "$HOME/ltsmin"

# download the LTSmin binaries
wget "$LTSMIN_URL" -P /tmp

# the files to extract
echo ltsmin-convert > /tmp/files
echo etf2lts-mc >> /tmp/files

# extract the files
tar -xf "/tmp/$LTSMIN_NAME" -C "$HOME/ltsmin" --no-anchored --files-from=/tmp/files
