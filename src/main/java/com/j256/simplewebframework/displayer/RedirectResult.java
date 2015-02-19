package com.j256.simplewebframework.displayer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.j256.simplewebframework.util.ResponseUtils;

/**
 * Redirect result which will redirect to another web-page either on a remote server or to a different relative path on
 * the current server.
 * 
 * @author graywatson
 */
public class RedirectResult {

	private final String fullUrl;
	private final String relativePath;

	private RedirectResult(String fullUrl, String relativePath) {
		this.fullUrl = fullUrl;
		this.relativePath = relativePath;
	}

	/**
	 * Create a redirect result with a full URL which includes home and path parts.
	 */
	public static RedirectResult withFullUrl(String fullUrl) {
		return new RedirectResult(fullUrl, null);
	}

	/**
	 * Create a redirect result with a relative path (and query-string) which will use the host and port of the current
	 * request.
	 */
	public static RedirectResult withRelativePath(String relativePath) {
		return new RedirectResult(null, relativePath);
	}

	/**
	 * Send a redirect using this result.
	 */
	public void sendResponseRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (fullUrl == null) {
			ResponseUtils.sendRelativeRedirect(request, response, relativePath);
		} else {
			ResponseUtils.sendRedirect(response, fullUrl);
		}
	}
}
