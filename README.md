# jfgdb - Java File Geodatabase thin driver

FileGeodatabase (GDB) is a very efficient file format for storing GIS Data.

This package provide a java library wrapper and ESRI FileGeodatabaseAPI (1.4) embedding to a simple use on java. 

This project bring a fast READ / WRITE of simple featureclasses for Java environment.



More informations on the FileGeodatabase API, supported plateform, can be found 

[https://github.com/Esri/file-geodatabase-api](https://github.com/Esri/file-geodatabase-api)

Using this library, you MUST agree to the ESRI (inc),  FileGeodatabase License conditions : 

Additional FileGeodatabase API usage conditions are explained here : https://blogs.esri.com/esri/arcgis/2010/12/13/file-geodatabase-api-details/



# Usage

This library has been compiled and tested on some Windows X64/x86, Linux x64. Extensive plateform tests could be done by the community.


Binaries are included for the windows plateform, for ease of use. Compilation on other plateforms are one command line far.

Binaries are also included in the jar file for moderns x64 Linux. Due to OS dependencies loading (dlopen primitive), see this note before running the jar.



# Gradle / Maven access

Gradle : 



Declare the repository

```
repositories {
    maven { url "http://jfgdb.s3-website-eu-west-1.amazonaws.com/maven2"}
}

```

And dependency in the project :

```
compile group: 'org.jfgdb', name: 'jfgdb', version:'0.1.4'
```



Maven :

```
   <dependency>
      <groupId>org.jfgdb</groupId>
      <artifactId>jfgdb</artifactId>
      <version>0.1.4</version>
    </dependency>
```




#How To Build the jar and native wrappers 


	git clone https://github.com/frett27/jfgdb

	set JAVA_HOME=[YOUR_PATH_TO_JDK1.7_OR_UP]
	set ESRI_FILE_GDB_HOME=[YOUR_PATH_TO_ESRIFGB1.3ROOTDIRECTORY]
	gradlew build -x test distZip


the result is in `build/distributions`



# Examples

See the samples folder for usage
