#!/bin/bash
SOURCE=supported-source-extensions.txt

# This script is intended to generate the go code snippet for constants.go
# - step 1: edit $SOURCE
# - step 2: run this script and copy the output
# - step 3: replace the DefaultZipAllowedFilePatterns declaration in constants.go

cd `dirname $0`

cat - <<EOF
// DefaultZipAllowedFilePatterns - Define file patterns to include in zip file.
// These patterns are considered as source code to be scanned.
var DefaultZipAllowedFilePatterns = []string{
EOF

RESPONSE=""
SRC=`cat $SOURCE`
while read line ; do
  SRCLANG=`echo $line | cut -d : -f 1`
  SRCEXTLIST=`echo $line | cut -d : -f 2`
  RESPONSE="$RESPONSE    "
  for i in $SRCEXTLIST ; do
    RESPONSE="$RESPONSE\"$i\", "
  done
  RESPONSE="$RESPONSE/* $SRCLANG */"$'\n'
done <<< "$SRC"

echo -n "$RESPONSE" | head -n -1  # all but last line
echo -n "$RESPONSE" | tail -1 | sed 's/\(.*\),/\1}/'  # last line
