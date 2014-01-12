package com.j256.simplewebframework.resource;

import java.io.File;

import com.j256.simplewebframework.displayer.FileResultDisplayer.FileInfo;

/**
 * Utility that locates files on our local file-system.
 * 
 * @author graywatson
 */
public class FileLocator {

	// @JmxAttributeField(description = "Local directory for storing files")
	private File localDirectory;
	// @JmxAttributeField(description = "Welcome files that we lookup if a directory is accessed")
	private String[] welcomeFiles = new String[0];

	public FileLocator() {
		// for spring
	}

	public FileLocator(File localDirectory, String[] welcomeFiles) {
		this.localDirectory = localDirectory;
		this.welcomeFiles = welcomeFiles;
	}

	/**
	 * Find the file that we are to be rendering on the file-system.
	 * 
	 * @return A file-info object or null if the file was found.
	 */
	public FileInfo findFile(String pathInfo) {

		// create our local file path
		File localFile = new File(localDirectory, pathInfo);
		if (!localFile.exists()) {
			return null;
		}

		// if a directory then bail, not sure this will ever happen
		if (!localFile.isDirectory()) {
			return new FileInfo(pathInfo, localFile);
		}

		// check welcome paths
		for (String welcomeFileName : welcomeFiles) {
			String welcomePath = pathInfo + welcomeFileName;
			File welcomeFile = new File(localDirectory, welcomePath);
			if (welcomeFile.exists()) {
				return new FileInfo(welcomePath, welcomeFile);
			}
		}

		return null;
	}

	public void setLocalDirectory(File localDirectory) {
		localDirectory.mkdirs();
		if (!localDirectory.exists()) {
			throw new IllegalArgumentException("Local directory " + localDirectory + " does not exist");
		}
		if (!localDirectory.isDirectory()) {
			throw new IllegalArgumentException("Local directory " + localDirectory + " is not a directory");
		}
		this.localDirectory = localDirectory;
	}

	public void setWelcomeFiles(String[] welcomeFiles) {
		this.welcomeFiles = welcomeFiles;
	}
}
