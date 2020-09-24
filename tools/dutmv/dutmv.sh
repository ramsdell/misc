#! /bin/sh
# This script assumes that the jar file is in the $RELDIR directory
# relative to the location this file.
RELDIR=.
COMMAND=`basename "$0"`
if test "$COMMAND" = "$0"
then
  COMMAND=`command -v "$0"`
else
  COMMAND="$0"
fi
DIR=`dirname "$COMMAND"`

exec java -jar "${DIR}/${RELDIR}/dutmv.jar" "$@"
