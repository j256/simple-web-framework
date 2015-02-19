package com.j256.simplewebframework.displayer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

/**
 * Displayer that handles a direct redirect result.
 * 
 * @author graywatson
 */
public class RedirectDisplayer implements ResultDisplayer {

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { RedirectResult.class };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		RedirectResult redirectResult = (RedirectResult) result;
		redirectResult.sendResponseRedirect(request, response);
		return true;
	}
}
