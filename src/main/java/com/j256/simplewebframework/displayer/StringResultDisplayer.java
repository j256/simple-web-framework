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
public class StringResultDisplayer implements ResultDisplayer {

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { String.class };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		String str = (String) result;
		response.setContentLength(str.length());
		PrintWriter writer = response.getWriter();
		writer.append(str);
		writer.close();
		return true;
	}
}
