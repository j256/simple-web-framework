package com.j256.simplewebframework.util;

import javax.servlet.http.HttpServletRequest;

/**
 * EC2 utilities that are web specific.
 * 
 * @author graywatson
 */
public class WebEc2Utils {

	private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

	/**
	 * Gets the remote address for the specified inbound request. If we are running in EC2, it has probably been routed
	 * through the load balancer, this will return the IP address of the origin rather than the load balancer.
	 */
	public static String getRemoteAddress(HttpServletRequest request) {
		String remoteAddr = request.getHeader(HEADER_X_FORWARDED_FOR);
		if (!StringUtils.isEmpty(remoteAddr)) {
			String[] tokens = StringUtils.split(remoteAddr, ',');
			if (tokens.length == 0) {
				remoteAddr = request.getRemoteAddr();
			} else {
				remoteAddr = tokens[tokens.length - 1].trim();
			}
		} else {
			remoteAddr = request.getRemoteAddr();
		}
		return remoteAddr;
	}
}
