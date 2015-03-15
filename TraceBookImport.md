# Introduction #

This is the developer information for the TraceBook import plugin. The user guide can be found [here](TraceBookPluginManual.md)


# Details #

First, obtain the source like displayed [here](TraceBookPluginManual#2._Building/Installing_from_source.md). This page also describes how to compile the source.
The source folder contains information that enables the developer to use the project in eclipse.
For building the plugin in eclipse, create an ant running configuration, select the included build.xml as building script and set "install" as the default build target.

## The TraceBookPlugin class ##
The main entry point for the plugin is the TraceBookPlugin class. It loads the FileImporter information from the TraceBookImporter class and adds it together with the correct file extension filter to the JOSM import functions list.

## The TraceBookImporter class ##
It handles the most file I/O actions which is loading the track xml and linking the media information to the Markers which are then added to a MarkerLayer.

## The `*`Marker classes ##
These classes define the media markers which are used by JOSM to display that there is additional media at a given location. Basically they all to the same: Open the media in the system's default viewer.