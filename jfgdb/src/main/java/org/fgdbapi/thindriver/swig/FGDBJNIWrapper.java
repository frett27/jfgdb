/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.fgdbapi.thindriver.swig;

public class FGDBJNIWrapper {
  public static void setMaxOpenFiles(int maxOpenFiles) {
    FGDBJNIWrapperJNI.setMaxOpenFiles(maxOpenFiles);
  }

  public static Geodatabase createGeodatabase(String path) {
    long cPtr = FGDBJNIWrapperJNI.createGeodatabase(path);
    return (cPtr == 0) ? null : new Geodatabase(cPtr, true);
  }

  public static Geodatabase openGeodatabase(String path) {
    long cPtr = FGDBJNIWrapperJNI.openGeodatabase(path);
    return (cPtr == 0) ? null : new Geodatabase(cPtr, true);
  }

  public static String getSpatialReferenceWkt(int srid) {
    return FGDBJNIWrapperJNI.getSpatialReferenceWkt(srid);
  }

  public static int CloseGeodatabase(Geodatabase geodatabase) {
    return FGDBJNIWrapperJNI.CloseGeodatabase(Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int DeleteGeodatabase(String path) {
    return FGDBJNIWrapperJNI.DeleteGeodatabase(path);
  }

  public static int createGeodatabase2(String path, Geodatabase geodatabase) {
    return FGDBJNIWrapperJNI.createGeodatabase2(path, Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int openGeodatabase2(String path, Geodatabase geodatabase) {
    return FGDBJNIWrapperJNI.openGeodatabase2(path, Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int closeGeodatabase2(Geodatabase geodatabase) {
    return FGDBJNIWrapperJNI.closeGeodatabase2(Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int deleteGeodatabase2(String path) {
    return FGDBJNIWrapperJNI.deleteGeodatabase2(path);
  }

}
