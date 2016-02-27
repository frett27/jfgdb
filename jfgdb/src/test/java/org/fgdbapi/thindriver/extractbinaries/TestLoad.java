package org.fgdbapi.thindriver.extractbinaries;

import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;

public class TestLoad {

	public static void main(String[] args) {

		Geodatabase gdb = FGDBJNIWrapper.openGeodatabase("C:\\temp\\raw.gdb");

	}

}
