Introduction
============

The TraceBook import plugin for JOSM enables JOSM to read TraceBookTrack
(\`\*\`.tbt) files which are created by our TraceBook application for
android mobile devices. This is the user manual page. The developer
information can be obtained [TraceBookImport here]

Known Bugs and Limitations
==========================

The plugin can not:

`* save a modified track`\
`* manipulate media information`\
`* delete track information/whole tracks`

Use the TraceBook app on your device for those actions or delete folders
manually.

Download/Installation
=====================

1. Installing the precompiled jar
---------------------------------

`* Download the jar from our `<a href="http://code.google.com/p/swp-dv-ws2010-osm-1/downloads/list">`Download Page`</a>\
`* Windows users: Copy the jar file to "_yourusername_\Appdata\Roaming\JOSM\plugins"`\
`* Linux users: Copy the jar file to "/home/_yourusername_/.JOSM/plugins"`\
`* when in doubt refer to the JOSM Plugin installation guide for your os`

2. Building/Installing from source
----------------------------------

\_(advanced users only!)\_

`* Checkout the !TraceBookImport source from `<a href="http://code.google.com/p/swp-dv-ws2010-osm-1/source/checkout">`here`</a>\
`* for compiling you need at least: java6, svn command line tool, ant`\
`* if you wish to build for a newer version of JOSM (>3966) replace the josm-tested.jar in "lib" with the newer one`\
`* if you meet all the requirements, navigate to the location where you checked out the source and type "ant install". The plugin will be automatically build and installed into your JOSM installation.`

Final steps:
------------

`* Start up JOSM`\
`* open the menu "Edit -> Preferences"`

<img src="http://swp-dv-ws2010-osm-1.googlecode.com/svn/wiki/img/josm_preferences.png" height="30%" width="30%"/>

`* open the tab "Plugins"`\
`* select !TraceBookPlugin`

<img src="http://swp-dv-ws2010-osm-1.googlecode.com/svn/wiki/img/josm_select_plugin.png" height="30%" width="30%"/>

`* click "OK" and restart JOSM`

Usage
=====

After successful installation, just bring up the file open dialog. In
the file extension filter select "TraceBookTrack (\`\*\`.tbt)" and
navigate to the place where your tracks are stored (in most cases the
Phone or SDcard). Open the folder of the track you want to open, select
the \`\*\`.tbt file and click "open". After a short progress dialog
,depending how big your track is, you should see a standard osm layer
view and an additional layer with the media you attached. The osm layer
behaves just like the usual JOSM osm layer, with the limitations that
currently changes can not be saved to the file. You can still manipulate
and upload your data though. The media layer is read only too, and can
be used as reference when tagging the osm data.

<img src="http://swp-dv-ws2010-osm-1.googlecode.com/svn/wiki/img/josm_sample_view.png" height="30%" width="30%"/>

The text, audio and video markers will be opened by your default system
viewer.

`` * For audio comments you need a player on your system that is capable and linked to playing `*`.m4a files ``\
`` * For video comments you need a player on your system that is capable and linked to playing `*`.mp4 files ``\
`` * For text comments you need a player on your system that is capable and linked to playing `*`.txt files ``
