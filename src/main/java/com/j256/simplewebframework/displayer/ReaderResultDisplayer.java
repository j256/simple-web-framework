package com.j256.simplewebframework.displayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.IoUtils;

/**
 * Displayer that writes the data from a provided reader to the response. The reader will be closed afterwards.
 * 
 * @author graywatson
 */
public class ReaderResultDisplayer implements ResultDisplayer {

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { Reader.class };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		Reader reader = (Reader) result;
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
			char[] buffer = new char[4096];
			while (true) {
				int numRead = reader.read(buffer);
				if (numRead < 0) {
					break;
				}
				pw.write(buffer, 0, numRead);
			}
			return true;
		} finally {
			IoUtils.closeQuietly(pw);
			IoUtils.closeQuietly(reader);
		}
	}
}
