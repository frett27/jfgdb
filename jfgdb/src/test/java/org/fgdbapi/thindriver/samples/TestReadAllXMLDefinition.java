package org.fgdbapi.thindriver.samples;

import java.io.File;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.VectorOfWString;
import org.junit.Test;

public class TestReadAllXMLDefinition {

  /**
   * this test explore all the geodatabase on f:\, and print for each geodatabase the featureclass
   * definitions
   *
   * @throws Exception
   */
  @Test
  public void testReadAll() throws Exception {

    File folder = new File("f:\\");

    File[] allFiles = folder.listFiles();
    for (int i = 0; i < allFiles.length; i++) {

      File f = allFiles[i];
      if (!f.isDirectory()) continue;

      if (!f.getName().endsWith(".gdb")) continue;

      System.out.println("Handling file :" + f);

      Geodatabase og = FGDBJNIWrapper.openGeodatabase(f.getAbsolutePath());
      try {
        VectorOfWString result = og.getChildDatasets("\\", "Feature Class");
        for (int j = 0; j < result.size(); j++) {
          String s = result.get(j);
          System.out.println("------- open :" + s);
          String definition = og.getDatasetDefinition(s, "Feature Class");
          System.out.println("Definition :" + definition);
        }
      } finally {
        FGDBJNIWrapper.CloseGeodatabase(og);
      }
    }
  }
}
