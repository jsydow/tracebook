# Introduction #

Core.data.db contains a database with 2 tables:

  * The tag database contains a list of all tags (key, value pairs) and there name, description and image as described [here](http://wiki.openstreetmap.org/wiki/Map_Features). Using this table the user can search for the correct tag if he does not know the key-value-pair by heart.
  * The tag history database stores a usage statistic of all tags already used by the user. I can be used to show a history of most recently used tags or a history of most used tags.


# Details #

The easiest class first:

## TagSearchResult ##

A TagSearchResult object has member fields for each column of the tag database. It is used to store a result row of a query to the tag database.

## TagDbOpenHelper ##

See [SQLiteOpenHelper](http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html). The TagDbOpenHelper helps accessing the database and provides a bit more comfort on using the databases.

## TagDb ##

The TagDb class encapsulates the tag database. It provides easy access to the database. The operations are:

  * getTag: Given a keyword, get all TagSearchResults where the keywords appears somewhere in the description.
  * getRowCountForLanguage: Pretty much self-explaining. It retrieves the number of rows/entries of a language.
  * getDetails: Given a tag (key-value-pair) get the description of a it.
  * initDbWithFile: Fills the database with tags from a map features XML file.

### Map features XML file ###

The XML-file that contains all the information for all tags is parsed from the OSM wiki ([link](http://wiki.openstreetmap.org/wiki/Map_Features)) using a ruby script. This script saves the gathered information in the XML file. The structure of the XML file is described in the article MapFeatures.

## HistoryDb ##

The HistoryDb encapsulates the history database. It provides the two operations needed for interaction with the database, namely:
  * Retrieve the history
  * Update the usage of a tag