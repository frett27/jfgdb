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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Created by use on 13/12/2014. */
public class TestTables {

  private Geodatabase g;
  private File gdbfile;

  @Before
  public void setup() throws Exception {

    File tempFolder = File.createTempFile("test", "folder");
    tempFolder.delete();
    tempFolder.mkdirs();

    gdbfile = new File(tempFolder, "fgdb.gdb");
    g = FGDBJNIWrapper.createGeodatabase(gdbfile.getAbsolutePath());

    System.out.println("database :" + gdbfile);
  }

  @After
  public void tearDown() throws Exception {
    gdbfile.delete();
  }

  @Test
  public void testCreateTable() throws Exception {

    String tableDefinition =
        TableHelper.newTable("matable")
            .addStringField("c1", 40)
            .addIntegerField("i1")
            .buildAsString();

    Table table = g.createTable(tableDefinition, "");
    g.closeTable(table);

    FGDBJNIWrapper.closeGeodatabase2(g);
  }

  private String alea() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append((char) (Math.random() * 255));
    }
    return sb.toString();
  }

  ArrayList<String> l = new ArrayList<>();

  @Test
  public void testCreateTableAndInsertStrings() throws Exception {

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

    for (int i = 0; i < 10_000; i++) {
      l.add(alea());
    }

    System.gc();
    System.out.println(Runtime.getRuntime().maxMemory());
    System.out.println(Runtime.getRuntime().freeMemory());
    System.out.println("------");

    System.out.println(tableDefinition);

    String string = tableDefinition.buildAsString();
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
                  r.setString("c" + ((int) (Math.random() * fields)), alea());

                  if (cpt++ % 10000 == 0) System.out.println(cpt);
                  table.insertRow(r);
                  r.delete();
                  for (int j = 0; j < 500; j++) l.remove(0);
                }
              }
            }
          });
    }

    tp.awaitTermination(100000, TimeUnit.DAYS); // forever

    table.freeWriteLock();
    table.setLoadOnlyMode(false);

    g.closeTable(table);

    FGDBJNIWrapper.closeGeodatabase2(g);
  }
}
