#!/bin/bash
# Copyright (C) 2013 TU Dortmund
# This file is part of AutomataLib, http://www.automatalib.net/.
# 
# AutomataLib is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License version 3.0 as published by the Free Software Foundation.
# 
# AutomataLib is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with AutomataLib; if not, see
# http://www.gnu.de/documents/lgpl.en.html.

PROJECT_ROOT=".."
ABSPATH=`readlink -f $0`
THIS_DIR=`dirname "$ABSPATH"`


GIT_REPO="git@github.com:LearnLib/learnlib.git"
GIT_BRANCH="gh-pages"
GIT_PATH="/maven-site"

CHECKOUT_DIR="./pages"
MVN="mvn"
GIT="git"



PROJECT_ROOT=`readlink -f "$PROJECT_ROOT"`

cd $PROJECT_ROOT

$MVN site:stage
if [ $? != 0 ]; then
	echo "Error executing mvn site:deploy"
	exit 1
fi

stagingDir=`$MVN help:evaluate -Dexpression=stagingDirectory | egrep -v '^\['`

if [ "$stagingDir" = "null object or invalid expression" ]; then
	stagingDir=`$MVN help:evaluate -Dexpression=project.build.directory | egrep -v '^\['`/staging
fi

stagingDir=`readlink -f "$stagingDir"`

echo "Determined $stagingDir as staging directory"

if [ ! -d "$stagingDir" ]; then
	echo "Staging directory does not exist!"
	exit 1
fi

cd $THIS_DIR

CHECKOUT_DIR=`readlink -f "$CHECKOUT_DIR"`

if [ -d "$CHECKOUT_DIR" ]; then
	echo "Found $CHECKOUT_DIR, pulling..."
	cd "$CHECKOUT_DIR"
	git pull
	if [ $? != 0 ]; then
		echo "Pulling failed"
		exit 1
	fi
else
	echo "Cloning into $CHECKOUT_DIR"
	git clone -b "$GIT_BRANCH" --single-branch "$GIT_REPO" "$CHECKOUT_DIR"
	if [ $? != 0 ]; then
		echo "Cloning failed"
		exit 1
	fi
	cd "$CHECKOUT_DIR"
fi

targetDir=`readlink -f "$CHECKOUT_DIR/$GIT_PATH"`
mkdir -p "$targetDir"
rm -rf "$targetDir/*"
cp -r "$stagingDir"/* $targetDir

if [ $? != 0 ]; then
	echo "Copying $stagingDir to $targetDir failed"
	exit 1
fi

git add $targetDir
if [ $? != 0 ]; then
	echo "Adding $targetDir failed"
	exit 1
fi

username=`git config --get user.name`

git commit -am "Site deployment by $username `date --rfc-2822`"
if [ $? != 0 ]; then
	echo "Commit failed"
	exit 1
fi

git push
if [ $? != 0 ]; then
	echo "Push failed"
	exit 1
fi


echo "Site deployment successful"

