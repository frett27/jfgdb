package org.fgdbapi.thindriver.extractbinaries;

import org.fgdbapi.thindriver.SharedLibrariesInitializer;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;

public class TestExtractBinaries {

	public static void main(String[] args) {
		System.getProperties().list(System.out);
		SharedLibrariesInitializer.initLibraries();
		String osName = System.getProperty("os.name");
		
		FGDBJNIWrapper.openGeodatabase("C:\\temp\\raw.gdb");
		

	}

}
