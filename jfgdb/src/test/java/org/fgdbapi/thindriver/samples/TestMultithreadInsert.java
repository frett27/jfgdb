package org.fgdbapi.thindriver.samples;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Row;
import org.fgdbapi.thindriver.swig.Table;
import org.fgdbapi.thindriver.xml.EsriGeometryType;

public class TestMultithreadInsert {

  private static ArrayList<String> l = new ArrayList<String>();

  private static String alea() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append((char) (Math.random() * 255));
    }
    return sb.toString();
  }

  public static void main(String[] args) throws Exception {

    File tempFolder = File.createTempFile("test", "folder");
    tempFolder.delete();
    tempFolder.mkdirs();

    TableHelper tableDefinition =
        TableHelper.newFeatureClass(
            "matable",
            EsriGeometryType.ESRI_GEOMETRY_POINT,
            TableHelper.constructW84SpatialReference());

    final int fields = 255;
    for (int i = 0; i < fields; i++) {
      tableDefinition = tableDefinition.addStringField("c" + i, fields);
    }

    // .addIntegerField("i1").buildAsString();

    System.gc();
    System.out.println(Runtime.getRuntime().maxMemory());
    System.out.println(Runtime.getRuntime().freeMemory());
    System.out.println("------");

    for (int i = 0; i < 30_000; i++) {
      l.add(alea());
    }

    System.gc();
    System.out.println(Runtime.getRuntime().maxMemory());
    System.out.println(Runtime.getRuntime().freeMemory());
    System.out.println("------");

    System.out.println(tableDefinition);

    String string = tableDefinition.buildAsString();

    File gdbfile = new File(tempFolder, "fgdb.gdb");

    Geodatabase g = FGDBJNIWrapper.createGeodatabase(gdbfile.getAbsolutePath());
    System.out.println("database :" + gdbfile);

    final Table table = g.createTable(string, "");

    table.setWriteLock();
    table.setLoadOnlyMode(true);

    ExecutorService tp = Executors.newFixedThreadPool(5);
    for (int i = 0; i < 5; i++) {
      tp.submit(
          new Runnable() {

            @Override
            public void run() {

              int cpt = 0;

              for (int i = 0; i < 10000000; i++) {
                synchronized (TestTables.class) {
                  for (int j = 0; j < 500; j++) l.add(alea());

                  Row r = table.createRowObject();
                  for (int k = 0; k < 20; k++) {

                    int rfield = (int) (Math.random() * fields);

                    if (k < 4) {
                      r.setNull("c" + rfield);
                    } else {

                      r.setString("c" + rfield, alea());
                    }
                  }

                  if (cpt++ % 10000 == 0)
                    System.out.println(Thread.currentThread().getId() + "- > " + cpt);
                  table.insertRow(r);
                  // r.delete();
                  for (int j = 0; j < 500; j++) l.remove(0);
                }
              }
            }
          });
    }

    tp.awaitTermination(100000, TimeUnit.DAYS); // forever

    table.setLoadOnlyMode(false);
    table.freeWriteLock();

    g.closeTable(table);

    FGDBJNIWrapper.closeGeodatabase2(g);
  }
}
