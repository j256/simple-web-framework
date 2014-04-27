package com.j256.simplewebframework.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Response utilities that handle errors, redirects, and other changes to the response object.
 * 
 * @author graywatson
 */
public class RequestUtils {

	/**
	 * Return the server-name and optional :server-port string.
	 */
	public static String getServerHostPort(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder(request.getServerName());
		if (request.getServerPort() != 80) {
			sb.append(':').append(request.getServerPort());
		}
		return sb.toString();
	}
}
