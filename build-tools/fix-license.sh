#!/bin/bash

LICENSE_HEADER="./src/main/resources/license-header.txt"

if [ "$*" == "" ]; then
	find .. -name '*.java' | xargs "$0"
	exit
fi

for i in "$@"; do
	license=`head -n 1 "$i" | grep -c "/* Copyright (C) 2013"`
	if [ "$license" != "1" ]; then
		echo "File $i misses license header, fixing"
		cp "$LICENSE_HEADER" .tmp
		oldlicense=`head -n 1 "$i" | grep -c "/* Copyright (C) 2012"`
		if [ "$oldlicense" == "1" ]; then
			echo "Stripping old license from $i"
			tail -n +17 "$i" >>.tmp
		else
			cat "$i" >>.tmp
		fi
		mv .tmp "$i"
	fi
done

