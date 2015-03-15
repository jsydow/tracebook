Introduction
============

Core.data contains the data model of the tracking application. All
tracked data are stored here an can be retrieved, changed, saved to
SD-card and loaded from SD-card.

Details
=======

First of all read the wiki article [Data Data].

Data model
----------

The data model is a like a tree:

` * !DataStorage contains:`\
`  * !DataTrack contains:`\
`   * !DataNode (POIs) contains:`\
`    * tags`\
`    * !DataMedia`\
`   * !DataPointsList (ways and areas) contains:`\
`    * !DataMedia`\
`    * !DataNode contains:`\
`     * tags`\
`     * !DataMedia`\
`    * tags`\
`   * !DataMedia`

Data operations
---------------

There are 4 operations that can be done on a datum:

` * create: create a new datum and append to parent object`\
` * delete: delete the datum (operation is done on the parent object)`\
` * update: update data of the datum`\
` * query: if the parent object has a list of data, query (the parent) to find the specific datum.`

They are equivalent to the 4 SQL statements INSERT, DELETE, UPDATE and
SELECT.

All of the following classes implement those operations. So we only
describe the differences of the classes:

!DataStorage
------------

!DataStorage is the class that has all other data in it. It is therefore
like a root-object. Since all other parts of the application should only
work on one "database" of the tracking data this class is modeled as
Singleton. There is at most one instance of !DataStorage.

There is a special reference to a loaded track called currentWay. The
current way is the way that is currently edited. There is at most one
current way.

!DataMediaHolder
----------------

As seen in the data model tree above a !DataTrack, !DataNode and
!DataPointsList can have media attached. Therefore !DataMediaHolder is a
super class of those 3 classes and implements functionality to handle
media.

!DataMapObject
--------------

Also as seen in the data model tree above a !DataNode and
!DataPointsList can have tags. See
[OpenStreetMap-tags](http://wiki.openstreetmap.org/wiki/Tags).
!DataMapObject provides the functionality to handle tags. All
!DataMapObject are also !DataMediaHolder so that !DataMediaHolder is the
super class of !DataMapObject.

!DataNode
---------

Every !DataNode has a location (GPS coordinates). This is the only type
of data that has such a location. A location must not be existent the
time the node is created because the application may still wait for a
GPS fix.

!DataTrack
----------

This is the most complex class. It has to take care of the serialisation
of the data it contains.

At this point we should describe how the data are stored on the SD-card:

### Storing data on the SD-card

In the root directory of the SD card there is a folder !TraceBook. In it
there folders for each track. Each track has exactly one folder. The
name of the track is equal to the name of the folder.

In the folder of the track all media files are stored (Saved after
recording). The actual tracking data are stored in an .xml-file
"track.tbt". It has the extension .tbt because the JOSM plugin can then
easier differentiate between our XML-file and an XML-file of
!OpenStreetMap data. This .tbt-file has the same structure as an OSM
XML-file with one exception: The .tbt file contains references to the
media stored for the Track. The track.tbt file is created when the track
is serialised.

There is also a file called info.xml in the track folder. It is created
with the track.tbt file together and contains all the meta information
of a track (creation time of the track, comment, number of POIs, ways
and media). This XML-file is relatively small and therefore fast to
parse. This way it does not take long to deserialize the !TrackInfo for
a list of tracks. The !TrackInfo object stores the meta information of
the info.xml file.

!DataPointList
--------------

A !DataPointsList represents a object in OSM that consists of a series
of nodes like areas and ways.

!DataMedia
----------

!DataMedia is a reference to a medium. It stores the path to the medium
and its type.
