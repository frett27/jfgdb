package org.fgdbapi.thindriver.samples;

import java.io.File;

import junit.framework.TestCase;

import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.VectorOfWString;
import org.fgdbapi.thindriver.xml.EsriDatasetType;

public class TestReadAllXMLDefinition extends TestCase {

	public void testReadAll() throws Exception {

		File folder = new File("f:\\");

		File[] allFiles = folder.listFiles();
		for (int i = 0; i < allFiles.length; i++) {

			File f = allFiles[i];
			if (!f.isDirectory())
				continue;

			if (!f.getName().endsWith(".gdb"))
				continue;

			System.out.println("Handling file :" + f);

			Geodatabase og = FGDBJNIWrapper
					.openGeodatabase(f.getAbsolutePath());
			try {
				VectorOfWString result = og.getChildDatasets("\\",
						"Feature Class");
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
