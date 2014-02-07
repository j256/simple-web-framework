package com.j256.simplewebframework.util;

import java.io.File;
import java.io.IOException;

/**
 * File level utility methods copied and modified from elsewhere.
 * 
 * @author graywatson
 */
public class FileUtils {

	/**
	 * Deletes a directory recursively.
	 */
	public static void deleteDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(directory + " is not a directory");
		}

		if (!isSymlink(directory)) {
			for (File file : directory.listFiles()) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					if (!file.delete()) {
						throw new IOException("Unable to delete file: " + file);
					}
				}
			}
		}

		if (!directory.delete()) {
			throw new IOException("Unable to delete directory: " + directory);
		}
	}

	/**
	 * Determines whether the specified file is a symlink as opposed to an actual file.
	 */
	public static boolean isSymlink(File file) throws IOException {
		File fileInCanonicalDir;
		File parentFile = file.getParentFile();
		if (parentFile == null) {
			fileInCanonicalDir = file;
		} else {
			fileInCanonicalDir = new File(parentFile.getCanonicalFile(), file.getName());
		}

		if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		} else {
			return true;
		}
	}
}
