package com.j256.simplewebframework.displayer;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.displayer.BinaryResultDisplayer.BinaryResult;
import com.j256.simplewebframework.util.IOUtils;

/**
 * Displayer that writes the data from a {@link BinaryResult} object.
 * 
 * @author graywatson
 */
public class BinaryResultDisplayer extends SingleClassResultDisplayer<BinaryResult> {

	public BinaryResultDisplayer() {
		super(BinaryResult.class);
	}

	@Override
	protected boolean renderTypedResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			BinaryResult result) throws IOException {
		ServletOutputStream sos = null;
		try {
			if (result.contentType != null) {
				response.setContentType(result.contentType);
			}
			sos = response.getOutputStream();
			sos.write(result.bytes);
			return true;
		} finally {
			IOUtils.closeQuietly(sos);
		}
	}

	/**
	 * Binary result which is a bunch of bytes and a content-type.
	 */
	public static class BinaryResult {

		private final String contentType;
		private final byte[] bytes;

		public BinaryResult(String contentType, byte[] bytes) {
			this.contentType = contentType;
			this.bytes = bytes;
		}

		public String getContentType() {
			return contentType;
		}

		public byte[] getBytes() {
			return bytes;
		}
	}
}
