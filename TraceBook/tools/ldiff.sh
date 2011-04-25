#!/bin/sh

########################################################################
 #
 # This file is part of TraceBook.
 #
 # TraceBook is free software: you can redistribute it and/or modify it
 # under the terms of the GNU General Public License as published by the
 # Free Software Foundation, either version 3 of the License, or (at
 # your option) any later version.
 #
 # TraceBook is distributed in the hope that it will be useful, but
 # WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 # General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with TraceBook. If not, see <http://www.gnu.org/licenses/>.
 #
########################################################################

LANGUAGES="de en fr gr pl tr"

PROJECTDIR="$HOME/development/TraceBook"
BASEFILE="res/values/strings.xml"

if [ "x$USER" != "xdd" ]
then
    PROJECTDIR="."
fi

if [ ! -r "$PROJECTDIR/$BASEFILE" ]
then
    echo "$PROJECTDIR/$BASEFILE" does not exist.
    exit 1
fi

mksort() {
    BASEFILE=$1
    TMPFILE=$2

    if [ -r "$BASEFILE" ]
    then
        grep 'string name="' "$BASEFILE" | sed -e 's/>.*//g' -e 's/.*name=//g' -e 's/"//g' | sort -f | uniq > $TMPFILE
    fi
}

BASETMP=`mktemp`

mksort "$PROJECTDIR/$BASEFILE" "$BASETMP"
for LANG in $LANGUAGES
do
    NEWFILE="$PROJECTDIR/res/values-$LANG/strings.xml"

    if [ -r $NEWFILE ]
    then
        NEWTMP=`mktemp`

        mksort "$NEWFILE" "$NEWTMP"

        echo "For $NEWFILE:"
        diff -w -U 0 "$NEWTMP" "$BASETMP" | grep -v '\-\-\-' | grep -v '+++' | grep ^\[+-\] | sed -e 's/^-/OBSOLETE: /g' -e 's/^+/MISSING:  /g'
        echo

        rm -f $NEWTMP
    fi
done

rm -f $BASETMP
