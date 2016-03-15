package org.fgdbapi.thindriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;

/**
 * init and uncompress shared libraries
 * 
 * @author pfreydiere
 * 
 */
public class SharedLibrariesInitializer {

	static boolean initialized = false;

	private static void uncompressFiles(String resourcePath,
			String[] listFiles, File outputDir) {
		assert resourcePath != null;
		assert !resourcePath.endsWith("/");

		if (listFiles == null || listFiles.length == 0)
			return;

		for (String filename : listFiles) {

			InputStream is = SharedLibrariesInitializer.class
					.getResourceAsStream(resourcePath + "");
			if (is == null) {
				System.out.println("WARN resource " + filename + " not found");
				continue;
			} else {
				System.out.println("write " + filename);
				try {
					File outputFile = new File(outputDir, filename);

					if (outputFile.exists())
						continue;

					FileOutputStream fos = new FileOutputStream(outputFile);
					try {
						byte[] buffer = new byte[1024];
						int cpt = 0;
						while ((cpt = is.read(buffer)) != -1) {
							fos.write(buffer, 0, cpt);
						}

					} finally {
						fos.close();
						is.close();
					}
				} catch (IOException ex) {
					System.err.println("could not write file " + filename);
					ex.printStackTrace(System.err);
				}
			}
		}

	}

	private static void uncompressFile(String resourcePath, String filename,
			File outputDir) throws Exception {
		assert resourcePath != null;
		assert !resourcePath.endsWith("/");

		if (filename == null)
			return;

		String basePath = resourcePath.substring(0,
				resourcePath.lastIndexOf("/"));
		assert !basePath.endsWith("/");

		String libraryResourcePath = basePath + "/" + filename;
		InputStream is = SharedLibrariesInitializer.class
				.getResourceAsStream(libraryResourcePath);
		if (is == null)
			throw new Exception("resource \"" + libraryResourcePath
					+ "\" not found");

		try {
			File outputFile = new File(outputDir, filename);

			if (outputFile.exists())
				return;

			System.out.println("write " + filename);
			FileOutputStream fos = new FileOutputStream(outputFile);
			try {
				byte[] buffer = new byte[1024];
				int cpt = 0;
				while ((cpt = is.read(buffer)) != -1) {
					fos.write(buffer, 0, cpt);
				}

			} finally {
				fos.close();
				is.close();
			}
		} catch (IOException ex) {
			System.err.println("could not write file " + filename);
			ex.printStackTrace(System.err);
			throw ex;
		}

	}

	public static void initLibraries() {

		if (initialized)
			return;

		synchronized (SharedLibrariesInitializer.class) {
			if (initialized)
				return;

			System.out.println("initialize the shared libraries");

			File tmpFile = new File(System.getProperty("java.io.tmpdir"));
			if (!tmpFile.exists() && !tmpFile.isDirectory())
				throw new RuntimeException("java.io.tmpdir does not exists");

			String osName = System.getProperty("os.name");
			System.out.println("OS :" + osName);

			String osArch = Platform.RESOURCE_PREFIX;
			System.out.println("Architecture :" + osArch);

			InputStream fgdbversionProperties = SharedLibrariesInitializer.class
					.getResourceAsStream("/fgdbversion.properties");
			if (fgdbversionProperties == null)
				throw new RuntimeException(
						"fgdbversion.properties file not found in the build,  incorrect compile");

			String version = null;
			Properties properties = new Properties();
			try {
				properties.load(fgdbversionProperties);
				version = properties.getProperty("version");

			} catch (Exception ex) {
				throw new RuntimeException(
						"failed to load fgdbversion.properties :"
								+ ex.getMessage(), ex);
			}
			if (version == null)
				throw new RuntimeException(
						"fgdbversion.properties does not contain version key, incorrect build");

			String resourcesFilesPath = "/sharedlibraries/" + version 
					+ "/" + osArch + "/files";
			InputStream resourceAsStream = SharedLibrariesInitializer.class
					.getResourceAsStream(resourcesFilesPath);
			if (resourceAsStream == null)
				throw new RuntimeException(
						"architecture is not supported yet : "
								+ resourcesFilesPath);
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(
						resourceAsStream, "UTF-8");
				StringBuilder sb = new StringBuilder();
				int b;
				while ((b = inputStreamReader.read()) != -1) {
					sb.append((char) b);
				}
				File outputDir = new File(tmpFile, "fgdbsharedlibs/" + version);
				outputDir.mkdirs();
				String[] files = sb.toString().split(",");

				String wrapperLibraryPath = null;

				for (String filename : files) {

					filename = sanitizeFileName(filename);
					if (filename == null || filename.isEmpty())
						continue;

					filename = filename.trim();

					System.out.println("checking " + filename);

					String libPath = System.getProperty("java.library.path")
							+ System.getProperty("path.separator")
							+ outputDir.getAbsolutePath();

					System.setProperty("jna.library.path", libPath);

					uncompressFile(resourcesFilesPath, filename, outputDir);

					if (filename.toLowerCase().contains("wrapper")) {
						String loadingDll = new File(outputDir, filename)
								.getAbsolutePath();
						wrapperLibraryPath = loadingDll;
					}

					if (filename.endsWith(".dll") || filename.endsWith(".so")) { // sanity

						String loadingDll = new File(outputDir, filename)
								.getAbsolutePath();
						System.out.println("loading sharedlibrary :"
								+ loadingDll);
						
						if (Platform.isWindows())
						{
							// load the library
							NativeLibrary.getInstance(loadingDll);
						}

//						{
//
//							// System.load(loadingDll); // don't work with
//							// dependencies
//
//							Map options = Collections.EMPTY_MAP;
//							if (Platform.isLinux()) {
//
//								if (libc == null) {
//									System.out
//											.println("setting LD_LIBRARY_PATH to "
//													+ outputDir
//															.getAbsolutePath());
//									libc = (LibC) Native.loadLibrary("c",
//											LibC.class);
//
//									int status = libc.setenv("LD_LIBRARY_PATH",
//											outputDir.getAbsolutePath(), 1);
//
//									System.out.println("setenv returned "
//											+ status);
//
//								}
//
//								if (libdl == null) {
//									libdl = (DL) Native.loadLibrary("c",
//											DL.class);
//								}
//
//								int returned = libdl.dlopen(
//										outputDir.getAbsolutePath(),
//										0x0100 | 0x00002);
//								System.out.println("dlopen returned "
//										+ returned);
//
//								// } else {
//
//							}
//						}

						// if (filename.toLowerCase().contains("wrapper")) {
						// System.loadLibrary("FGDBJNIWrapper");
						// }

						System.out.println("successfully loaded");
					}

				}

				if (wrapperLibraryPath == null) {
					throw new Exception(
							"wrapper library has not been extracted");
				}

				try {
					System.load(wrapperLibraryPath);
				} catch (Exception ex) {
					System.out.println("LIBRARY " + wrapperLibraryPath
							+ " cannot be loaded");
					ex.printStackTrace();

					if (Platform.isLinux()) {
						System.out
								.println("please launch \"export LD_LIBRARY_PATH="
										+ new File(wrapperLibraryPath)
												.getParentFile()
												.getAbsolutePath()
										+ "\" before running the program");
					}
				}

			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}

	}

	private static String sanitizeFileName(String filename) {

		if (filename == null)
			return null;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < filename.length(); i++) {
			char c = filename.charAt(i);
			switch (c) {
			case '\n':
			case '\t':
			case '\r':
				continue;
			default:
				break;
			}
			sb.append(c);
		}

		return sb.toString();

	}
}
