#jfgdb - Java File Geodatabase thin driver

The Filegeodatabase is a very efficient file format for storing GIS Data.

This package and native library permit to use the ESRI FileGeodatabaseAPI (1.3).

More informations on the FileGeodatabase API can be found here : [http://www.esri.com/software/arcgis/geodatabase/interoperability](http://www.esri.com/software/arcgis/geodatabase/interoperability)

This library has been compiled and partially tested on Windows X64/x86, but should also compile on MacOS, Linux.
Binaries are included for the windows plateform, for ease of use.

Schema are defined using the geodatabase xsd XMLSchema, wrappers are included in the driver permitting to not having to generate XML by hand.


#How to use

1 - Download the FileGeodatabase API from the above link

2 - Place the given native FGDBJNIWrapper library, and FileGeodatabase API DLLs in the path

3 - Use the jar file in you project