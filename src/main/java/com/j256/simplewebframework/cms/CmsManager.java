package com.j256.simplewebframework.cms;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.util.FileUtils;
import com.j256.simplewebframework.util.IoUtils;
import com.j256.simplewebframework.util.StringUtils;

/**
 * CMS version downloader and manager.
 * 
 * @author graywatson
 */
// @JmxResource(domainName = "j256.dstamp", folderNames = { "web" }, description = "CMS downloader and manager")
public class CmsManager {

	private static final Logger logger = LoggerFactory.getLogger(CmsManager.class);

	private ContentZipSource contentZipSource;
	// @JmxAttributeField(description = "local directory we write the CMS stuff too")
	private File localDirectory;
	// @JmxAttributeField(description = "CMS path currently in service")
	private String livePath;

	private List<RevisionInfo> lastCmsUpateRevisions;
	@SuppressWarnings("unused")
	// @JmxAttributeField(description = "live revision CMS directory")
	private File liveRevisionDir;

	public void setContentZipSource(ContentZipSource contentZipSource) {
		this.contentZipSource = contentZipSource;
	}

	public void setLocalDirectory(File localDirectory) {
		this.localDirectory = localDirectory;
		this.localDirectory.mkdirs();
		if (!this.localDirectory.isDirectory()) {
			throw new IllegalArgumentException("Could not create localDirectory: " + localDirectory);
		}
	}

	public void setLivePath(String livePath) {
		this.livePath = livePath;
	}

	// @JmxAttributeMethod(description = "Revisions last updated")
	public String[] getLastCmsUpateRevisions() {
		List<RevisionInfo> lastUpdate = lastCmsUpateRevisions;
		if (lastUpdate == null) {
			return new String[] { "never" };
		}
		List<String> results = new ArrayList<String>();
		for (RevisionInfo info : lastUpdate) {
			results.add(info.toString());
		}

		return results.toArray(new String[results.size()]);
	}

	/**
	 * Update our CMS directory to the latest version.
	 */
	// @JmxAttributeOperation(description = "Update the CMS release")
	public void updateCms() throws IOException {
		List<RevisionInfo> revisionInfos = readRevisionInfos();
		lastCmsUpateRevisions = revisionInfos;

		if (revisionInfos.isEmpty()) {
			logAndThrow("No CMS revisions read from config file", null);
		}

		RevisionInfo liveRevision = null;
		for (RevisionInfo info : revisionInfos) {
			if (info.live) {
				if (liveRevision != null) {
					logAndThrow("Duplicate live CMS revisions are marked as line from config file", null);
				}
				liveRevision = info;
			}
		}
		if (liveRevision == null) {
			logAndThrow("None of the CMS revisions are marked as line from config file", null);
		}

		boolean downloaded = false;
		File newLiveRevisionDir = null;
		for (RevisionInfo info : revisionInfos) {
			File revisionDir = info.getRevisionDir(localDirectory);

			// see if the directory exists already, we assume it is whole
			if (!revisionDir.isDirectory()) {
				// we may need to create the parent dir
				File branchDir = info.getBranchDir(localDirectory);
				makeDir(branchDir);

				File tmpDir = info.getRevisionDirTmp(localDirectory);
				// just in case
				FileUtils.deleteDirectory(tmpDir);
				if (!downloadVersion(info, tmpDir)) {
					/*
					 * We may have invalid entries in the config.txt file.
					 */
					continue;
				}
				// rename into place
				tmpDir.renameTo(revisionDir);
				downloaded = true;
			}

			if (info.live) {
				newLiveRevisionDir = revisionDir;
			}
		}
		if (newLiveRevisionDir == null) {
			logAndThrow("no new live revision directory was found", null);
		}

		File liveFile = new File(livePath);
		/*
		 * Take a look at the existing symlink. If it is pointing to the right directory then we are all set.
		 */
		if (liveFile.exists() && newLiveRevisionDir.getCanonicalFile().equals(liveFile.getCanonicalFile())) {
			// we are already linked appropriately
			if (downloaded) {
				logger.info("cms is already linked to live directory: " + newLiveRevisionDir);
			}
		} else {
			// need to make the symlink
			File liveFileTmp = new File(livePath + ".t");
			makeLink(newLiveRevisionDir, liveFileTmp);
			// rename link into place
			liveFileTmp.renameTo(liveFile);
			logger.info("cms linked to live directory: " + newLiveRevisionDir);
		}
		liveRevisionDir = newLiveRevisionDir;

		// remove the directories that are not in the config file
		clearBranches(revisionInfos, liveFile);
		clearRevisions(revisionInfos, liveFile);
	}

	private void clearBranches(List<RevisionInfo> revisionInfos, File liveFile) {
		for (File branchDir : localDirectory.listFiles()) {
			if (branchDir.equals(liveFile)) {
				// skip the symlink
				continue;
			}
			if (branchDir.isFile()) {
				// skip files
				continue;
			}
			if (branchDir.getName().startsWith(".")) {
				// skip dot files
				continue;
			}
			boolean matched = false;
			for (RevisionInfo info : revisionInfos) {
				if (info.getBranchDir(localDirectory).equals(branchDir)) {
					matched = true;
				}
			}
			if (!matched) {
				try {
					FileUtils.deleteDirectory(branchDir);
					logger.info("deleted old branch dir: {}", branchDir);
				} catch (IOException e) {
					// ignore it
					logger.error("could not remove old branch dir: " + branchDir, e);
				}
			}
		}
	}

	private void clearRevisions(List<RevisionInfo> revisionInfos, File liveFile) {
		// accumulate all of the branch directories
		for (File branchDir : localDirectory.listFiles()) {
			if (branchDir.equals(liveFile)) {
				// skip the symlink
				continue;
			}
			if (!branchDir.isDirectory()) {
				continue;
			}
			for (File revisionDir : branchDir.listFiles()) {
				if (revisionDir.isFile()) {
					// skip files
					continue;
				}
				if (revisionDir.getName().startsWith(".")) {
					// skip dot files
					continue;
				}
				boolean matched = false;
				for (RevisionInfo info : revisionInfos) {
					if (info.getRevisionDir(localDirectory).equals(revisionDir)) {
						matched = true;
					}
				}
				if (!matched) {
					try {
						FileUtils.deleteDirectory(revisionDir);
						logger.info("deleted old revision dir: {}", revisionDir);
					} catch (IOException e) {
						// ignore it
						logger.error("could not remove old revision dir: " + revisionDir, e);
					}
				}
			}
		}
	}

	private boolean downloadVersion(RevisionInfo info, File downloadDir) throws IOException {
		// String cmsZipPath = s3CmsPrefix + "/" + info.branch + "/" + info.revision + ".zip";
		InputStream is = contentZipSource.getRevisionInputStream(info.revision);
		if (is == null) {
			logger.warn("Could not find cms revision {}", info);
			return false;
		}
		ZipInputStream zipStream = null;
		try {
			downloadDir.mkdirs();
			zipStream = new ZipInputStream(new BufferedInputStream(is));
			is = null;
			while (true) {
				ZipEntry entry = zipStream.getNextEntry();
				if (entry == null) {
					break;
				}
				String fileName = entry.getName();
				if (entry.isDirectory()) {
					if (fileName.endsWith("/")) {
						fileName = fileName.substring(0, fileName.length() - 1);
					}
					File dir = new File(downloadDir, fileName);
					makeDir(dir);
				} else {
					File file = new File(downloadDir, fileName);
					readInFile(zipStream, entry, file);
				}
			}
			logger.info("downloaded revision {} to {}", info.revision, downloadDir);
			return true;
		} finally {
			IoUtils.closeQuietly(zipStream);
			IoUtils.closeQuietly(is);
		}
	}

	private void readInFile(ZipInputStream zipStream, ZipEntry entry, File zipFile) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(zipFile);
			byte[] buffer = new byte[4096];
			while (true) {
				int readCount = zipStream.read(buffer, 0, buffer.length);
				if (readCount < 0) {
					break;
				}
				fos.write(buffer, 0, readCount);
			}
		} finally {
			IoUtils.closeQuietly(fos);
		}
		zipFile.setReadable(true, false);
		zipFile.setWritable(true, true);
	}

	private void makeDir(File dir) throws IOException {
		dir.mkdirs();
		dir.setExecutable(true, false);
		dir.setReadable(true, false);
		dir.setWritable(true, true);
		if (!dir.isDirectory()) {
			logAndThrow("Could not create directory: " + dir, null);
		}
	}

	private List<RevisionInfo> readRevisionInfos() throws IOException {
		// String s3Path = s3CmsPrefix + "/" + configPath;
		// download the config file
		InputStream is = contentZipSource.getRevisionConfigInputStream();
		if (is == null) {
			logAndThrow("Could not find revision configuration file", null);
		}
		BufferedReader reader = null;
		List<String> configLines;
		try {
			reader = new BufferedReader(new InputStreamReader(is));
			is = null;
			// read it in
			configLines = new ArrayList<String>();
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				configLines.add(line);
			}
		} finally {
			IoUtils.closeQuietly(reader);
			IoUtils.closeQuietly(is);
		}

		List<RevisionInfo> revisionInfos = new ArrayList<RevisionInfo>();
		for (String line : configLines) {
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}

			RevisionInfo info = RevisionInfo.createRevisionInfo(line);
			if (info != null) {
				revisionInfos.add(info);
			}
		}
		return revisionInfos;
	}

	private void makeLink(File existingFile, File linkFile) throws IOException {
		Process process =
				Runtime.getRuntime().exec(new String[] { "/bin/ln", "-s", existingFile.getPath(), linkFile.getPath() });
		int errorCode;
		try {
			errorCode = process.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Link operation was interrupted");
		}
		if (errorCode != 0) {
			logAndThrow("Could not create symlink from " + existingFile + " to " + linkFile, null);
		}
	}

	private void logAndThrow(String msg, Exception e) throws IOException {
		logger.error(msg, e);
		throw new IOException(msg, e);
	}

	/**
	 * Class that holds information our revision config file.
	 */
	private static class RevisionInfo {

		final boolean live;
		final String branch;
		final int revision;

		private RevisionInfo(boolean live, String branch, int revision) {
			this.live = live;
			this.branch = branch;
			this.revision = revision;
		}

		public static RevisionInfo createRevisionInfo(String line) {
			String[] parts = StringUtils.split(line, '\t');
			if (parts.length < 3) {
				logger.error("CMS config file line invalid: " + line);
				return null;
			}

			boolean live = Boolean.parseBoolean(parts[0]);
			String branch = parts[1];
			if (branch == null || branch.length() == 0) {
				logger.error("CMS config file line invalid branch: " + line);
				return null;
			}
			int revision;
			try {
				revision = Integer.parseInt(parts[2]);
			} catch (NumberFormatException e) {
				logger.error("CMS config file line invalid revision: " + line);
				return null;
			}

			return new RevisionInfo(live, branch, revision);
		}

		public File getBranchDir(File localDirectory) {
			return new File(localDirectory, branch);
		}

		public File getRevisionDir(File localDirectory) {
			return new File(getBranchDir(localDirectory), Integer.toString(revision));
		}

		public File getRevisionDirTmp(File localDirectory) {
			return new File(getBranchDir(localDirectory), Integer.toString(revision) + ".t");
		}

		@Override
		public String toString() {
			return "branch " + branch + ", rev " + revision + ", live " + live;
		}
	}
}
