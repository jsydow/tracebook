Project conventions
===================

Our project conventions are based on the official [Code Conventions for
the Java Programming
Language](http://www.oracle.com/technetwork/java/codeconv-138413.html).
In addition to these conventions, the following rules apply to all
project members.

Language and character encoding
-------------------------------

We use only English language in our project. Therefore all names of
packages, files, methods, variables as well as all comments, debug
output and commit messages must be only in English.

The Eclipse IDE comes with a built-in spell checker that should be used.
It can be activated via \`Window \> Preferences \> General \> Editors \>
Text Editors \> Spelling\`.

Please use UTF-8 character encoding for all source files, comments and
other written documentation.

Java specific conventions
-------------------------

### Code style

We work with the Eclipse IDE and use the integrated code formatter. Our
main Eclipse project contains a profile for the Java code formatter
named \_mapsforge\_. Before committing Java code to the repository this
code style must be applied to all modified source files.

The default shortcut to format a single file in Eclipse is
\`Shift+Ctrl+F\`. Right click on a selection of files or packages and
select \`Source \> Format\` if you want to format more than one file at
once.

Please do not modify the mapsforge profile without approval of the other
project members.

### Visibility

Please keep the visibility of all classes, methods and fields to a
minimum. Use \`public\` modifiers only if necessary.

### Javadoc

We use Javadoc for documentation purposes. All important classes,
methods and fields need a proper Javadoc comment. When writing a Javadoc
comment for a method, all parameters and the return value must be
documented. Please update Javadoc comments whenever the related code has
been changed.

In general you should follow the [Sun conventions for writing
documentation
comments](http://www.oracle.com/technetwork/java/javase/documentation/index-137868.html).

### Compiler warnings

Many common Java programming mistakes can be avoided by enabling
compiler checks. Malformed Javadoc comments or missing Javadoc tags are
also unnecessary. As we try to prevent erroneous Java code and bad
Javadoc comments, we enable all compiler warnings that enforce a higher
level of code quality and readability. You can check the compiler
settings of an Eclipse project via \`Properties \> Java Compiler \>
Errors/Warnings\` and \`Properties \> Java Compiler \> Javadoc\`.

If possible, all warnings should be solved by the originator. Ask other
developers for help if you don't know why a certain warning is raised or
how to solve it correctly.

Do not use \`@SupressWarnings\` annotations to hide warnings. Do not
commit code that triggers a lot of warnings. Never commit code that
cannot be compiled and thus renders the whole project unusable for all
other developers.

Please do not modify the compiler settings without approval of the other
project members.
