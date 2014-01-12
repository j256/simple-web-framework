package com.j256.simplewebframework.displayer;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.IoUtils;

/**
 * Displayer that writes the data from a provided input-stream to the response. The input-stream will be closed
 * afterwards.
 * 
 * @author graywatson
 */
public class InputStreamResultDisplayer implements ResultDisplayer {

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { InputStream.class };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		InputStream is = (InputStream) result;
		ServletOutputStream sos = null;
		try {
			sos = response.getOutputStream();
			byte[] buffer = new byte[4096];
			while (true) {
				int numRead = is.read(buffer);
				if (numRead < 0) {
					break;
				}
				sos.write(buffer, 0, numRead);
			}
			return true;
		} finally {
			IoUtils.closeQuietly(sos);
			IoUtils.closeQuietly(is);
		}
	}
}
