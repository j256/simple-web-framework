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
public class HttpErrorCodeResultDisplayer extends SingleClassResultDisplayer<HttpErrorCode> {

	public HttpErrorCodeResultDisplayer() {
		super(HttpErrorCode.class);
	}

	@Override
	protected boolean renderTypedResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			HttpErrorCode result) {
		ResponseUtils.sendError(response, result);
		return true;
	}
}
