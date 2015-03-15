Introduction
============

Our TraceBookTrack files contain osm data and media data which is linked
to files relative to the folder the \`\*\`.tbt files are stored in.

Details
=======

Each \`\*\`.tbt file has a standard xml header. The default encoding of
the file is UTF-8. example:

It is followed by the root node with the name "osm" which contains
information about the osm version that it conforms to, and which
generator tool created it. This makes the file readable for JOSM even
without our plugin, if you rename it to "\`\*\`.osm", with the
limitation that no media information can be displayed. example:

The root node may contain either nodes or ways. example

or

A node contains coordinates, a unique id and an editing version. A way
contains only the unique id and the editing version. Note that all nodes
must be before any way.

Like in OSM xml format, each node and way can contain tags, with
additional information for the special point or way. example:

` `

for a way or

` `

for a node. Additionally, each node can contain media links to provide
additional information for later editing. example:

Every way needs to have at least one node as child reference. example:


