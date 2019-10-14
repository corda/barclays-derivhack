#!/bin/bash
jar fx ../lib/dependencies/cdm-2.5.11.jar
find org/isda/cdm -type f -iname "*.class" | sort -u > output1.txt

cat output1.txt | while read -r line
do
   echo "${line%.*}" | sed "s/.*\///" >> output2.txt
done
awk '{ print $0 "::class.java," }' < output2.txt > output3.txt
cat output3.txt
