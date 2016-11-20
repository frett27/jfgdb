package org.fgdbapi.thindriver;

/**
 * this class permit to unpack the shared DLL, 
 * this is mandatory prior to the usage on linux OS, 
 * due to DLOpen OS primitive, so dependencies can't be loaded
 * if not declared in LD_LIBRARY_PATH 
 * 
 * @author pfreydiere
 *
 */
public class DependenciesUnpacker {

	/**
	 * main procedure to unpack the fgdb libraries
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Unpacking the sharedLibraries, if not present");
		SharedLibrariesInitializer.initLibraries();
		
		System.out.println("dependencies unpacked, place them in the LD_LIBRARY_PATH for linux os");
		
	}

}
