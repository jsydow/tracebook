Introduction
============

!TraceBook is an application that allows the user to record data for the
!OpenStreetMap-project.

!OpenStreetMap data description
===============================

First of all one has to know how the data are stored in
[OpenStreetMap](http://www.openstreetmap.org/). It is necessary for the
user to know this, as !TraceBook works with them.

!OpenStreetMap has 2 primitives:

`* single points (also called points of interest (POI))`\
`* a list of points resembling`\
`  * ways or`\
`  * areas`

Areas are ways where the first an last way point are the same.

All primitive data have tags attached which describe what this datum
represents. A tag consists of a key and a value. The possible values can
be seen [here](http://wiki.openstreetmap.org/wiki/Map_Features). A road
for example may have following tags:

` * highway = residential`\
` * name = Lushington Road`

Which marks this way as a road named "Lushington Road" which is s
residential road.

General Introduction
====================

!TraceBook is able to record data in the !OpenStreetMap (abbr. OSM) data
format. The data structure in !TraceBook is the following:

All data are stored in \_Tracks\_. Tracks are like sessions. They are a
container of all data recorded in one use of the application.

A track can contain \_POIs\_ and \_Pointslist\_. Pointslists are the
ways and areas. Each POI and pointslist can contain tags and media.
Media are not used in OSM but are only as memos for the user to remember
what the datum was, he recorded. Media can be textnotes, audio- and
video-recordings and photos.

The data recorded with !TraceBook are meant to be edited afterwards
using JOSM. JOSM will then let you upload your data to OSM. This editing
step is necessary for a better quality of OSM. The GPS signal is not
precise enough to record straight lines. The user must therefor manually
make a more precise way afterwards.

If you start !TraceBook and click yourself around a bit you will notice
a green bar at the top of the screen. This bar is called the status bar.
It shows where you are. Clicking on the text on the left will open a
dialog with a small description of the screen yuo see. Clicking on the
right gear button the preference menu will open.

The screens are called activities in Android. Here is an activitymap for
a general overview:

` * [StartActivityManual Start] : The welcome screen. Let you start or load a track`\
`   * "New Track" opens _Map_`\
`   * "Load Track" opens _!LoadTrack_`\
` * [LoadTrackActivityManual LoadTrack] : Lets you choose a track to load. Deleting, renaming tracks and viewing track information is also possible.`\
`   * "Load Track" opens _Map_`\
` * [MapsActivityManual Map] : This is the central activity for recording. It shows a map. It lets you add new data and bugs.`\
`   * "New POI", "Edit way/area" opens _!EditObject_`\
`   * "List" open _!ListData_`\
`   * "Info" opens _!TrackInfo_`\
` * [AddPointActivityManual EditObject] : Lets you edit a datum (POI, pointslist). Tags and Media can be added and edited.`\
`   * "Add" opens _!AddTag_`\
`   * "Media" opens _!ListMedia_`\
` * [AddPointMetaActivity AddTag] : For editing a single tag. Provides different possibilities to enter a tag.`\
`   * "Search" opens _!TagSearch_`\
` * [FullTextSearchActivityManuel TagSearch] : Provides a full text search in the database of all tags.`\
` * `[`ListData`](http://code.google.com/p/tracebook/wiki/MapsActivityManual#List)` : List all recorded data.`\
` * `[`TrackInfo`](http://code.google.com/p/tracebook/wiki/MapsActivityManual#Info)` : Shows information on the current track.`\
` * `[`ListMedia`](http://code.google.com/p/tracebook/wiki/AddPointActivityManual#Media)` : Shows all media recorded for that datum.`
