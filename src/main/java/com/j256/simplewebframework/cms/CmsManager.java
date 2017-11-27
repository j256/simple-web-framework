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

import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxAttributeMethod;
import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;
import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.util.FileUtils;
import com.j256.simplewebframework.util.IOUtils;

/**
 * CMS version downloader and manager.
 * 
 * <p>
 * <b>NOTE:</b> You may need to subclass this class and override the {@link #makeLink(File, File)} method to make a
 * "symbolic-link" or alias appropriately on your architecture.
 * </p>
 * 
 * @author graywatson
 */
@JmxResource(domainName = "j256.simpleweb", description = "CMS downloader and manager")
public class CmsManager {

	/**
	 * This can be set for tests or other situations when you have the CMS hierarchy locally and don't want to download
	 * the from the content source.
	 */
	private static final String LOCAL_CONTENT_DIR_PROPERTY = "j256.simpleWeb.cmsManager.localContentDir";
	protected static final Logger logger = LoggerFactory.getLogger(CmsManager.class);

	private ContentSource contentSource;
	@JmxAttributeField(description = "local directory we write the CMS stuff too")
	private File localDirectory;
	@JmxAttributeField(description = "CMS path currently in service")
	private File liveFile;

	private List<RevisionInfo> lastCmsUpateRevisions;
	@JmxAttributeField(description = "live revision CMS directory")
	private File liveRevisionDir;

	public void setContentSource(ContentSource contentSource) {
		this.contentSource = contentSource;
	}

	public void setLocalPath(String localPath) {
		this.localDirectory = new File(localPath);
		this.localDirectory.mkdirs();
		if (!this.localDirectory.isDirectory()) {
			throw new IllegalArgumentException("Could not create localDirectory: " + localPath);
		}
	}

	public void setLivePath(String livePath) {
		this.liveFile = new File(livePath);
	}

	@JmxAttributeMethod(description = "Revisions last updated")
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
	@JmxOperation(description = "Update the CMS release")
	public void updateCms() throws IOException {

		String localContentDir = System.getProperty(LOCAL_CONTENT_DIR_PROPERTY);
		if (localContentDir != null) {
			// shortcut when we are testing locally and don't want to pull down from AWS
			maybeUpdateLiveLink(false, new File(localContentDir));
			return;
		}

		List<RevisionInfo> revisionInfos = readRevisionInfos();
		lastCmsUpateRevisions = revisionInfos;

		if (revisionInfos.isEmpty()) {
			logAndThrow("No CMS revisions read from config file", null);
		}

		RevisionInfo liveRevision = null;
		for (RevisionInfo info : revisionInfos) {
			if (info.isLive()) {
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

			if (info.isLive()) {
				newLiveRevisionDir = revisionDir;
			}
		}
		if (newLiveRevisionDir == null) {
			logAndThrow("no new live revision directory was found", null);
		}

		maybeUpdateLiveLink(downloaded, newLiveRevisionDir);

		// remove the directories that are not in the config file
		clearBranches(revisionInfos);
		clearRevisions(revisionInfos);
	}

	/**
	 * Make a "symbolic-link" or "alias" from an existing file to a link file. This is very operating system specific
	 * with the default working on Unix, OSX, and maybe Windows using mklink built-in. You may need to override this
	 * method to do this properly on your architecture or to call future versions of Java which support symlinks.
	 */
	protected void makeLink(File existingFile, File linkFile) throws IOException {
		Process process;
		String unixLnPath = "/bin/ln";
		if (new File(unixLnPath).canExecute()) {
			process =
					Runtime.getRuntime().exec(
							new String[] { unixLnPath, "-s", existingFile.getPath(), linkFile.getPath() });
		} else {
			process =
					Runtime.getRuntime().exec(
							new String[] { "cmd", "/c", "mklink", "/j", linkFile.getPath(), existingFile.getPath() });
		}
		int errorCode;
		try {
			errorCode = process.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Link operation was interrupted", e);
		}
		if (errorCode != 0) {
			logAndThrow("Could not create symlink from " + linkFile + " to " + existingFile, null);
		}
	}

	/**
	 * Take a look at the existing symlink. If it is pointing to the right directory then we are all set.
	 */
	private void maybeUpdateLiveLink(boolean downloaded, File newLiveRevisionDir) throws IOException {
		if (liveFile.exists() && newLiveRevisionDir.getCanonicalFile().equals(liveFile.getCanonicalFile())) {
			// we are already linked appropriately
			if (downloaded) {
				logger.info("cms is already linked to live directory: " + newLiveRevisionDir);
			}
		} else {
			// need to make the symlink
			File liveFileTmp = new File(liveFile.getPath() + ".t");
			liveFileTmp.delete();
			makeLink(newLiveRevisionDir, liveFileTmp);
			// rename link into place
			liveFileTmp.renameTo(liveFile);
			logger.info("cms linked to live directory: " + newLiveRevisionDir);
		}
		liveRevisionDir = newLiveRevisionDir;
	}

	private void clearBranches(List<RevisionInfo> revisionInfos) {
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
					logger.error(e, "could not remove old branch dir: " + branchDir);
				}
			}
		}
	}

	private void clearRevisions(List<RevisionInfo> revisionInfos) {
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
						logger.error(e, "could not remove old revision dir: {}", revisionDir);
					}
				}
			}
		}
	}

	private boolean downloadVersion(RevisionInfo info, File downloadDir) throws IOException {
		InputStream is = contentSource.getContentZipInputStream(info.getBranch(), info.getRevision());
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
					if (fileName.endsWith("/") || fileName.endsWith(File.separator)) {
						fileName = fileName.substring(0, fileName.length() - 1);
					}
					File dir = new File(downloadDir, fileName);
					makeDir(dir);
				} else {
					File file = new File(downloadDir, fileName);
					readInFile(zipStream, entry, file);
				}
			}
			logger.info("downloaded revision {} to {}", info.getRevision(), downloadDir);
			return true;
		} finally {
			IOUtils.closeQuietly(zipStream);
			IOUtils.closeQuietly(is);
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
			IOUtils.closeQuietly(fos);
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
		// download the config file
		InputStream is = contentSource.getRevisionConfigInputStream();
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
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(is);
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

	private void logAndThrow(String msg, Exception e) throws IOException {
		if (e == null) {
			logger.error(msg);
			throw new IOException(msg);
		} else {
			logger.error(e, msg);
			throw new IOException(msg, e);
		}
	}
}
