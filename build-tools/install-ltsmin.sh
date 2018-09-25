#!/bin/bash

LTSMIN_NAME="ltsmin-${LTSMIN_VERSION}-$TRAVIS_OS_NAME.tgz"
LTSMIN_URL="https://github.com/utwente-fmt/ltsmin/releases/download/$LTSMIN_VERSION/$LTSMIN_NAME"

# test if we have a cached version
test -f "$HOME/ltsmin/${LTSMIN_VERSION}/bin/ltsmin-convert" -a -f "$HOME/ltsmin/${LTSMIN_VERSION}/bin/etf2lts-mc" && exit 0

# create the directoy where the binaries end up.
mkdir -p "$HOME/ltsmin"

# download the LTSmin binaries
wget "$LTSMIN_URL" -P /tmp

# the files to extract
echo ltsmin-convert > /tmp/files
echo etf2lts-mc >> /tmp/files

# extract the files
tar -xf "/tmp/$LTSMIN_NAME" -C "$HOME/ltsmin" --no-anchored --files-from=/tmp/files
