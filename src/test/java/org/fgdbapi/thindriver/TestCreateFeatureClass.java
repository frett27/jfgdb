package org.fgdbapi.thindriver;

import java.io.File;

import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Table;
import org.fgdbapi.thindriver.xml.EsriGeometryType;

import junit.framework.TestCase;

public class TestCreateFeatureClass extends TestCase {

	public void test() throws Exception {

		File f = File.createTempFile("fgdb", ".gdb");
		f.delete();

		Geodatabase g = FGDBJNIWrapper.createGeodatabase(f.getAbsolutePath());
		try {
			
			String tableDefinition = TableHelper.newFeatureClass("mafc",
					EsriGeometryType.ESRI_GEOMETRY_POLYLINE,
					TableHelper.constructW84SpatialReference()).buildAsString();
			System.out.println("table definition :" + tableDefinition);
			
			Table table = g.createTable(tableDefinition, "");

			g.closeTable(table);
			
			System.out.println("FGDB created in " + f.getAbsolutePath());
			
		} finally {
			FGDBJNIWrapper.CloseGeodatabase(g);
		}

	}

}
