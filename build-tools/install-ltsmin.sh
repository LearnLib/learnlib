#!/bin/bash

LTSMIN_NAME="ltsmin-${LTSMIN_VERSION}-$TRAVIS_OS_NAME.tgz"
LTSMIN_URL="https://github.com/${LTSMIN_REPO:-utwente-fmt}/ltsmin/releases/download/$LTSMIN_VERSION/$LTSMIN_NAME"

if [ $TRAVIS_OS_NAME = "windows" ]; then
   FILE_SUFFIX=".exe"
fi

# test if we have a cached version
test -f "$HOME/ltsmin/${LTSMIN_VERSION}/bin/ltsmin-convert${FILE_SUFFIX}" -a -f "$HOME/ltsmin/${LTSMIN_VERSION}/bin/etf2lts-mc${FILE_SUFFIX}" && exit 0

# create the directoy where the binaries and downloads end up.
mkdir -p "$HOME/ltsmin"
mkdir -p "$HOME/ltsmin-download"

# download the LTSmin binaries
wget "$LTSMIN_URL" -P "$HOME/ltsmin-download"

# the files to extract
echo ${LTSMIN_VERSION}/bin/ltsmin-convert${FILE_SUFFIX} > $HOME/ltsmin-download/files
echo ${LTSMIN_VERSION}/bin/etf2lts-mc${FILE_SUFFIX} >> $HOME/ltsmin-download/files

# extract the files
tar -xf "$HOME/ltsmin-download/$LTSMIN_NAME" -C "$HOME/ltsmin" --files-from=$HOME/ltsmin-download/files
