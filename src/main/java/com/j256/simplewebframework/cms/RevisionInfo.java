package com.j256.simplewebframework.cms;

import java.io.File;

import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.util.StringUtils;

/**
 * Class that holds information our revision information. See {@link #createRevisionInfo(String)} for details about the
 * file format.
 */
class RevisionInfo {

	private static final Logger logger = LoggerFactory.getLogger(RevisionInfo.class);

	private final boolean live;
	private final String branch;
	private final int revision;

	private RevisionInfo(boolean live, String branch, int revision) {
		this.live = live;
		this.branch = branch;
		this.revision = revision;
	}

	/**
	 * <p>
	 * Create a revision entry from a tab separated line.
	 * </p>
	 * 
	 * <pre>
	 * # lines that start with pound sign are comments and ignored along with blank lines
	 * #
	 * # valid line has at least 3 tab-separated fields:
	 * #
	 * # 1. Live boolean specified as true or false.  This is whether the entry is live or not.  Only one line at a time
	 * #    should be marked as live (true).
	 * # 2. Branch name.  This is just a name such as trunk or 1.1 or something that identifies the branch for matching
	 * #    up with a revision control system.
	 * # 3. Revision number.  This is an integer number that identifies which revision this is from.  If you make changes
	 * #    to the content and publish a new version, this revision number should increase.  This lets the CMS system
	 * #    know that it needs to upload and install it.      
	 * #
	 * # live   branch   revision-number
	 * true     trunk    123
	 * </pre>
	 */
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

	public boolean isLive() {
		return live;
	}

	public String getBranch() {
		return branch;
	}

	public int getRevision() {
		return revision;
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
