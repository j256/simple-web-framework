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
public class RedirectDisplayer extends SingleClassResultDisplayer<RedirectResult> {

	public RedirectDisplayer() {
		super(RedirectResult.class);
	}

	@Override
	protected boolean renderTypedResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			RedirectResult result) throws IOException {
		result.sendResponseRedirect(request, response);
		return true;
	}
}
