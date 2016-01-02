

del *.java

..\..\..\..\..\..\swigwin-3.0.2\swig -java -IC:\projets\fgdbjava\FileGDB_API_VS2012_1_4\include -c++ -package org.fgdbapi.thindriver.swig  FGDBJNIWrapper.i

copy *.java ..\..\..\..\jfgdb\src\main\java\org\fgdbapi\thindriver\swig

copy *.cxx ..\cxx

pause

