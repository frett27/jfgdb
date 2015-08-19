#jfgdb - Java File Geodatabase thin driver

FileGeodatabase (GDB) is a very efficient file format for storing GIS Data.

This package provide a java library to use the ESRI FileGeodatabaseAPI (1.3).

More informations on the FileGeodatabase API can be found here : [http://www.esri.com/software/arcgis/geodatabase/interoperability](http://www.esri.com/software/arcgis/geodatabase/interoperability)

This library has been compiled and tested on Windows X64/x86, Linux. 
Binaries are included for the windows plateform, for ease of use. Compilation on other plateforms are one command line far.


#How To Build the jar and native wrappers 


	git clone https://github.com/frett27/jfgdb

	set JAVA_HOME=[YOUR_PATH_TO_JDK1.7_OR_UP]
	set ESRI_FILE_GDB_HOME=[YOUR_PATH_TO_ESRIFGB1.3ROOTDIRECTORY]
	gradlew build -x test distZip


the result is in `build/distributions`


#How to use

2 - Place the given native FGDBJNIWrapper library, and FileGeodatabase API DLLs in the PATH or LD\_LIBRARY\_PATH (for linux)

3 - Use the jar file in you project

