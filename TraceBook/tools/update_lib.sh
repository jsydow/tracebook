#!/bin/bash

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

TARGET="../mapsforge"

svn checkout http://mapsforge.googlecode.com/svn/trunk/mapsforge $TARGET
cd $TARGET
ant clean
mapsforge-map-javadoc-create
ant mapsforge-map-jar-create
cd -
cp $TARGET/dist/mapsforge-map-0.2.2.jar lib/
