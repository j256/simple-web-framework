package com.j256.simplewebframework.displayer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

/**
 * Displayer that writes the resulting string to the HTTP response
 * 
 * @author graywatson
 */
public class StringResultDisplayer extends SingleClassResultDisplayer<String> {

	public StringResultDisplayer() {
		super(String.class);
	}

	@Override
	protected boolean renderTypedResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			String result) throws IOException {
		response.setContentLength(result.length());
		PrintWriter writer = response.getWriter();
		writer.append(result);
		writer.close();
		return true;
	}
}
