[Checkstyle](http://eclipse-cs.sourceforge.net/) is a code analysis tool that reveals spots where your code does not adhere to Java code standards. It is best installed as an [Eclipse plugin](http://eclipse-cs.sourceforge.net/downloads.html), as you can have it run the checks before each commit, e. g.

# XML file #

The [XML file](http://code.google.com/p/swp-dv-ws2010-osm-1/source/browse/trunk/TraceBook/checks.xml) we use is kind of a subset of the default rule set. It however is strict enough to pretty much enforce the same code layout standards for the whole project.

Manually setting up should not be necessary if you download the source code from our SVN repository and use Eclipse, because we have both the XML file and the Checkstyle configuration file for the project in there.