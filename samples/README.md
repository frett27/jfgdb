# Samples for jfgdb api



This folder contains samples for using the filegeodatabase api using the java jfgdb wrapper

If you've not read the prerequist:

- check you have a x64 jdk in path


- install the redist VC++ 2012 x64 on windows x64
- put the extracted libraries folder in the LD_LIBRARY_PATH env variable on linux x64



to compile examples :

```
gradlew build
```

launch examples  :

```
gradlew --rerun-tasks test -i
```

## CRUD Example

Create a FileGeodatabse

Create a FeatureClass

Insert Point geometries, in loadonly mode

Benchmark performance

