package com.j256.simplewebframework.displayer;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.IOUtils;

/**
 * Displayer that writes the data from a provided input-stream to the response. The input-stream will be closed
 * afterwards.
 * 
 * @author graywatson
 */
public class InputStreamResultDisplayer extends SingleClassResultDisplayer<InputStream> {

	private static final int BUFFER_SIZE = 4096;

	public InputStreamResultDisplayer() {
		super(InputStream.class);
	}

	@Override
	protected boolean renderTypedResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			InputStream result) throws IOException {
		ServletOutputStream sos = null;
		try {
			sos = response.getOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				int numRead = result.read(buffer);
				if (numRead < 0) {
					break;
				}
				sos.write(buffer, 0, numRead);
			}
			return true;
		} finally {
			IOUtils.closeQuietly(sos);
			IOUtils.closeQuietly(result);
		}
	}
}
