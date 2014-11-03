

..\..\..\..\swigwin-3.0.2\swig -java -IC:\projets\fgdbjava\FileGDB_API_VS2012_1_3\include -c++ -package org.fgdbapi.thindriver.swig  FGDBJNIWrapper.i

copy *.java ..\java\org\fgdbapi\thindriver\swig
del *.java

copy *.cxx ..\cxx

