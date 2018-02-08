#!/bin/bash

LICENSE_BODY_TPL='license-body.txt'

IFS=$'\n'

for r in `grep -n "LearnLib is free software" "$@" | gsed -re 's@^([^\:]+)\:([^\:]+)\:(.*)LearnLib is free software; you can redistribute it and/or(.*)$@\1:\2:\3:\4@g'`; do
	file=`echo $r | awk -F: '{print $1}'`
	line_start=`echo $r | awk -F: '{print $2}'`
	pre=`echo $r | awk -F: '{print $3}'`
	post=`echo $r | awk -F: '{print $4}'`
	line_end=`grep -nr 'lgpl.en.html' "$file" | awk -F: '{print $2}'`

	mod_license_body=`gmktemp`
	gsed -re 's@^(.*)$@'"$pre"'\1'"$post"'@g' <"$LICENSE_BODY_TPL" >"$mod_license_body"
	old_head=`gmktemp`
	ghead -n $((line_start - 1)) <"$file" >"$old_head"
	old_tail=`gmktemp`
	gtail -n +$((line_end + 1)) <"$file" >"$old_tail"
	cat "$old_head" "$mod_license_body" "$old_tail" >"$file"
done
