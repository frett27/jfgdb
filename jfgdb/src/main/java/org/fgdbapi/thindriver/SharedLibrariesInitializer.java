package org.fgdbapi.thindriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

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

		String basePath = resourcePath.substring(0,resourcePath.lastIndexOf("/"));
		assert !basePath.endsWith("/");
		
		String libraryResourcePath = basePath + "/" + filename;
		InputStream is = SharedLibrariesInitializer.class
				.getResourceAsStream(libraryResourcePath);
		if (is == null)
			throw new Exception("resource " + libraryResourcePath + " not found");

		System.out.println("write " + filename);
		try {
			File outputFile = new File(outputDir, filename);

			if (outputFile.exists())
				return;

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

			String osArch = System.getProperty("os.arch");
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

			String resourcesFilesPath = "/sharedlibraries/" + version + "/"
					+ osName + "/" + osArch + "/files";
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
				for (String filename : files) {
					uncompressFile(resourcesFilesPath, filename, outputDir);

					if (filename.endsWith(".dll")) {
						String loadingDll = outputDir + "\\"
								+ filename;
						System.out.println("loading dll :" + loadingDll);
						System.load(loadingDll);
					}

				}

			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}

	}

}
