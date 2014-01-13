package com.j256.simplewebframework.handler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.ResponseUtils;
import com.j256.simplewebframework.util.ResponseUtils.HttpErrorCode;

/**
 * Special parameter type that if specified passes in the request and response context to the web-service method.
 * 
 * @author graywatson
 */
public class RequestContext {

	private final Request baseRequest;
	private final HttpServletRequest request;
	private final HttpServletResponse response;

	public RequestContext(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		this.baseRequest = baseRequest;
		this.request = request;
		this.response = response;
	}

	public Request getBaseRequest() {
		return baseRequest;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * Helper method to add a cookie to the response.
	 */
	public void addCookie(Cookie cookie) {
		response.addCookie(cookie);
	}

	/**
	 * Helper method to add a cookie to the response.
	 */
	public void addCookie(String name, String value) {
		response.addCookie(new Cookie(name, value));
	}

	/**
	 * Helper method to send an error back as the response. This also closes the response output stream.
	 * 
	 * @return True if it worked otherwise false if it threw an exception.
	 */
	public boolean sendError(HttpErrorCode errorCode) {
		return ResponseUtils.sendError(response, errorCode);
	}

	/**
	 * Helper method to send an error back as the response. This also closes the response output stream.
	 * 
	 * @return True if it worked otherwise false if it threw an exception.
	 */
	public boolean sendError(HttpErrorCode errorCode, String message) {
		return ResponseUtils.sendError(response, errorCode, message);
	}
}
