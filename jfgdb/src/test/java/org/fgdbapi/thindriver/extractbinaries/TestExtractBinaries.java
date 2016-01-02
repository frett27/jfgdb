package org.fgdbapi.thindriver.extractbinaries;

import org.fgdbapi.thindriver.SharedLibrariesInitializer;

public class TestExtractBinaries {

	public static void main(String[] args) {
		System.getProperties().list(System.out);
		SharedLibrariesInitializer.initLibraries();
		String osName = System.getProperty("os.name");
		
		
		

	}

}
