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

if [ "$*" == "" ]; then
	for style in `find . -name "*.style"`; do
		$0 $style
	done
	exit
fi

echo "Processing style $1"

PROJECT_ROOT=`readlink -f "$PROJECT_ROOT"`


file_header=0
source $1

if [ -z "$firstline_prefix" ]; then
	firstline_prefix=$line_prefix
fi
if [ -z "$firstline_suffix" ]; then
	firstline_suffix=$line_suffix
fi
if [ -z "$lastline_prefix" ]; then
	lastline_prefix=$line_prefix
fi
if [ -z "$lastline_suffix" ]; then
	lastline_suffix=$line_suffix
fi
if [ -z "$ignore_pattern" ]; then
	ignore_pattern="$^"
fi

preamble_lines=`echo -n "$preamble" | wc -l`

peek=$((file_header + preamble_lines + 1))



firstline_rep="$firstline_prefix"'\0'"$firstline_suffix"
line_rep="$line_prefix"'\0'"$line_suffix"
lastline_rep="$lastline_prefix"'\0'"$lastline_suffix"

FILES=`git ls-files "$PROJECT_ROOT" | egrep "$file_pattern" | egrep -v "$ignore_pattern"`

LICENSE_FILE="license-header.txt"

license_firstline_expect=`head -n 1 license-header.txt | sed -re 's^.*^'"$firstline_rep"'^g'`

for i in $FILES; do
	if [ ! -f "$i" ]; then
		echo "Warning: File $i exists in git tree, but not on filesystem! Ignoring ..."
	else
		header_found=`head -n $peek "$i" | tail -n 1 | grep -c "$license_firstline_expect"`
		if [ "$header_found" -lt "1" ]; then
			echo "File $i misses license header, fixing ..."
			head -n $file_header "$i" >.tmp
			echo -n "$preamble" >>.tmp
			head -n 1 "$LICENSE_FILE" | sed -re 's^.*^'"$firstline_rep"'^g' >>.tmp
			tail -n +2 "$LICENSE_FILE" | head -n -1 | sed -re 's^.*^'"$line_rep"'^g' >>.tmp
			tail -n 1 "$LICENSE_FILE" | sed -re 's^.*^'"$lastline_rep"'^g' >>.tmp
			echo -n "$postamble" >>.tmp
			skip=$((file_header + 1))
			tail -n +$skip "$i" >>.tmp
			mv .tmp "$i"
		fi
	fi

#	license=`head -n 1 "$i" | grep -c "/* Copyright (C)"`
#	if [ "$license" != "1" ]; then
#		echo "File $i misses license header, fixing"
#		cat license-header.txt "$i" >.tmp
#		mv .tmp "$i"
#	fi
done

