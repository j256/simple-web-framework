package com.j256.simplewebframework.displayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.IOUtils;

/**
 * Displayer that writes the data from a provided {@link Reader} to the response. The reader will be closed afterwards.
 * 
 * NOTE: we don't have to use a {@link BufferedReader} in this case because we will be reading from the reader in large
 * chunks.
 * 
 * @author graywatson
 */
public class ReaderResultDisplayer extends SingleClassResultDisplayer<Reader> {

	private static final int BUFFER_SIZE = 4096;

	public ReaderResultDisplayer() {
		super(Reader.class);
	}

	@Override
	protected boolean renderTypedResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Reader result) throws IOException {
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
			char[] buffer = new char[BUFFER_SIZE];
			while (true) {
				int numRead = result.read(buffer);
				if (numRead < 0) {
					break;
				}
				pw.write(buffer, 0, numRead);
			}
			return true;
		} finally {
			IOUtils.closeQuietly(pw);
			IOUtils.closeQuietly(result);
		}
	}
}
