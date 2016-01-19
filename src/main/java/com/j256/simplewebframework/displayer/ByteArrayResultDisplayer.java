package com.j256.simplewebframework.displayer;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.IOUtils;

/**
 * Displayer that writes the data from a provided byte[] to the response.
 * 
 * @author graywatson
 */
public class ByteArrayResultDisplayer extends SingleClassResultDisplayer<byte[]> {

	public ByteArrayResultDisplayer() {
		super(byte[].class);
	}

	@Override
	protected boolean renderTypedResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			byte[] result) throws IOException {
		ServletOutputStream sos = null;
		try {
			sos = response.getOutputStream();
			sos.write(result);
			return true;
		} finally {
			IOUtils.closeQuietly(sos);
		}
	}
}
