#!/bin/bash

set -e

VERSION_MODE=$1
# "major": 1,
# "minor": 0,
# "patch": 0,
if [ "$VERSION_MODE" = "" ] ; then
  VERSION_MODE="patch"
fi

#get highest tag number
VERSION=`git describe --match "[0-9]*" --abbrev=0 --tags`

#replace . with space so can split into an array
VERSION_BITS=(${VERSION//./ })
#get number parts and increase last one by 1
VNUM1=${VERSION_BITS[0]}
if [[ "$VNUM1" = "" ]] ; then
  VNUM1=0;
fi
VNUM2=${VERSION_BITS[1]}
if [[ "$VNUM2" = "" ]]; then
  VNUM2=0;
fi
VNUM3=${VERSION_BITS[2]}
if [[ "$VNUM3" = "" ]]; then
  VNUM3=0;
fi

case $VERSION_MODE in
  "major")
       VNUM1=$((VNUM1+1))
       VNUM2=0
       VNUM3=0
       ;;
  "minor")
       VNUM2=$((VNUM2+1))
       VNUM3=0
       ;;
  "patch")
       VNUM3=$((VNUM3+1))
       ;;
esac

#create new tag
NEW_TAG="$VNUM1.$VNUM2.$VNUM3"
echo "Last tag version $VERSION;  New tag will be $NEW_TAG"