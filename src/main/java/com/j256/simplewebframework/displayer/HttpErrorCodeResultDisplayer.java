package com.j256.simplewebframework.displayer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.ResponseUtils;
import com.j256.simplewebframework.util.ResponseUtils.HttpErrorCode;

/**
 * Displayer that handles a direct error code result. It is more typical for a method to return some other result and
 * set the error code directly using the
 * {@link ResponseUtils#send.sendError(HttpServletResponse, HttpErrorCode, String)} method.
 * 
 * @author graywatson
 */
public class HttpErrorCodeResultDisplayer implements ResultDisplayer {

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { HttpErrorCode.class };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) {
		HttpErrorCode errorCode = (HttpErrorCode) result;
		ResponseUtils.sendError(response, errorCode);
		return true;
	}
}
