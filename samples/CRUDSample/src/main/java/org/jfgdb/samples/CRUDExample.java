package org.jfgdb.samples;

import java.io.File;

import org.fgdbapi.thindriver.SharedLibrariesInitializer;
import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Row;
import org.fgdbapi.thindriver.swig.Table;
import org.fgdbapi.thindriver.xml.EsriGeometryType;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;

public class CRUDExample {

	public static void createGeodataBaseAndTable() throws Exception {
		SharedLibrariesInitializer.initLibraries();

		System.out.println("java.library.path :" + System.getProperty("java.library.path"));
		System.out.println("Create Geodatabase");

		File f = File.createTempFile("tmp", ".gdb");
		f.delete();

		Geodatabase g = FGDBJNIWrapper.createGeodatabase(f.getAbsolutePath());

		System.out.println("Done, geodatabase created in " + f.getAbsolutePath());

		System.out.println("Creating a feature class in WGS84 SRS");

		TableHelper th = TableHelper.newFeatureClass("foo", EsriGeometryType.ESRI_GEOMETRY_POINT,
				TableHelper.constructW84SpatialReference());
		th.addIntegerField("integerf");
		th.addLongField("longfield");

		Table t = g.createTable(th.buildAsString(), "");
		try {
			t.setLoadOnlyMode(true);
			t.setWriteLock();

			System.out.println("table created, inserting geometries");
			long start = System.currentTimeMillis();
			for (int i = 0; i < 1000000; i++) {
				if (i % 100000 == 0) {
					System.out.println(" " + i + " objects inserted");
				}
				Row r = t.createRowObject();
				Point pt = new Point(Math.random(), Math.random());
				r.setGeometry(GeometryEngine.geometryToEsriShape(pt));
				r.setInteger("integerf", (int) (Math.random() * 1000));
				t.insertRow(r);
			}

			long time = (System.currentTimeMillis() - start);
			System.out.println("1000000 points created in " + time + " ms : Troughtput " + (1000000.0 / (time / 1000.0))
					+ " objects / s");

			t.freeWriteLock();

		} finally {
			System.out.println("closing the table, and constructing the spatial index");
			g.closeTable(t);
		}
		System.out.println("Done");

	}

	public static void main(String[] args) throws Exception {
		createGeodataBaseAndTable();
	}

}
