# Introduction #

Here you will see the structural design for the project.


# Details #

The project is divided in different packages which correspondent to different parts of the application logic.

Here a list of the package names and their usage:

  * core
    * data
      * Definition of data model
      * Storage of data
      * Saving and loading data
      * db
        * Stores the history of used tags
        * Stores the description of tags so that the user can search for tags.
    * logger
      * Provides a service that logs the gps data
      * Serves partly as interface between data model and gui
    * media
      * Provides methods to record and store media (photo, video, audio)

  * gui
    * activity
      * Contains all activities (GUI)
    * adapter
      * Provides a generic adapter which can be used for a ListActivity to store any data in a ListItem
  * util
    * view
      * Provides a HelpWebView to display html files as help