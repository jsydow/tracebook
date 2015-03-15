Tags (more or less key value pairs of meta data on a node) are an
essential part of OSM, and for users to have a common basis of those key
value pairs to work with, the OSM wiki provides a [list with common map
features](http://wiki.openstreetmap.org/wiki/Map_Features).

A current working copy of our XML files containing information about
those tags can be found in the [SVN
repository](http://code.google.com/p/swp-dv-ws2010-osm-1/source/browse/trunk/TraceBook/res/raw).

Acquisition
===========

There unfortunately is no normalized representation of all available
tags so far, and even though JOSM has a rather [comprehensive XML
file](http://josm.openstreetmap.de/browser/josm/trunk/data/defaultpresets.xml),
it lacks localization. Therefore, we chose to parse the [OSM wiki page
on map features](http://wiki.openstreetmap.org/wiki/Map_Features) in
several languages.

Tool for parsing
----------------

Ruby scripts for parsing can be found in the [SVN
repository](http://code.google.com/p/swp-dv-ws2010-osm-1/source/browse/trunk/OSMtags),
as well. As of March 18th, they lack comments etc., so please use them
with caution.

A Java version of that script might be available some time in the
future.

XML schema
----------

For validating the generated XML files, there is an XML schema that can
be downloaded [from our SVN
repository](http://swp-dv-ws2010-osm-1.googlecode.com/svn/trunk/OSMtags/OSM_tags.xsd).

### DTD

The DTD for our XML files looks like this:

Processing
==========

Tags are key elements when it comes to recording meta data about nodes
(or sets of nodes, such as streets and areas). We currently provide two
ways to use tags.

Entering pairs manually
-----------------------

When you want to add a tag to a tracked object of yours, you will be
shown two text areas first. Those fields are auto-completing fields for
keys and values accordingly. Currently, this feature makes use of the
XML files, reading possible values for completion there.

Searching for tags
------------------

Furthermore, you can search an SQLite database that contains data about
tags and their descriptions in several different languages, extracted
from the appropriate XML files. The database is supposed to provide
time-efficient searching for user input; we do not assume to be even
remotely as fast if we were to traverse the information extracted from
the XML file for this task.
