package com.j256.simplewebframework.cms;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Source for content archive that will be unpacked and served as a content bundle.
 * 
 * @author graywatson
 */
public interface ContentSource {

	/**
	 * Return an input stream associated with the text revision information. See
	 * {@link RevisionInfo#createRevisionInfo(String)} for information about file-format that this call should output.
	 * 
	 * @return null if the config information is not found.
	 */
	public InputStream getRevisionConfigInputStream() throws IOException;

	/**
	 * Return an input stream associated with the content revision in question. The revision content is assumed to be in
	 * ZIP format appropriate to be wrapped in a {@link ZipInputStream}. This method will close the stream when it is
	 * done reading or on any errors.
	 * 
	 * @param branch
	 *            Name of the branch that we are getting content information for.
	 * @param revision
	 *            Revision of the content that we are looking for.
	 * 
	 * @return null if the revision is not found.
	 */
	public InputStream getContentZipInputStream(String branch, int revision) throws IOException;
}
