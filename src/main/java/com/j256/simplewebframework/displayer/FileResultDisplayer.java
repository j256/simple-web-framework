package com.j256.simplewebframework.displayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.IoUtils;

/**
 * Displayer that writes the data from a file to the response.
 * 
 * @author graywatson
 */
public class FileResultDisplayer implements ResultDisplayer {

	private final MimeTypes mimeTypes = new MimeTypes();

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { File.class, FileInfo.class };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {

		File file;
		if (result instanceof File) {
			file = (File) result;
		} else if (result instanceof FileInfo) {
			file = ((FileInfo) result).getFile();
		} else {
			throw new IllegalArgumentException("could not display result of class " + result.getClass().getName());
		}

		if (!file.exists()) {
			return false;
		}

		if (file.length() <= Integer.MAX_VALUE) {
			response.setContentLength((int) file.length());
		}

		// see if if-modified-since header is set
		long lastModified = file.lastModified();
		if (lastModified > 0) {
			long ifModified = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
			// we do / 1000 because the file system last-modified is in seconds
			if (ifModified > 0 && (lastModified / 1000) <= (ifModified / 1000)) {
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				IoUtils.closeQuietly(response.getOutputStream());
				return true;
			}
		}

		response.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModified);
		Buffer mime = mimeTypes.getMimeByExtension(file.getName());
		if (mime != null) {
			response.setContentType(mime.toString());
		}

		ServletOutputStream sos = null;
		FileInputStream fis = null;
		try {
			sos = response.getOutputStream();
			fis = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			while (true) {
				int numRead = fis.read(buffer);
				if (numRead < 0) {
					break;
				}
				sos.write(buffer, 0, numRead);
			}
			return true;
		} finally {
			IoUtils.closeQuietly(sos);
			IoUtils.closeQuietly(fis);
		}
	}

	/**
	 * Information about the file we are displaying.
	 */
	public static class FileInfo {

		private final String path;
		private final File file;

		public FileInfo(String path, File file) {
			this.path = path;
			this.file = file;
		}

		public String getPath() {
			return path;
		}

		public File getFile() {
			return file;
		}
	}
}
