package com.j256.simplewebframework.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utilities around cookies.
 * 
 * @author graywatson
 */
public class CookieUtils {

	private static final int SESSION_COOKIE_EXPIRE_SECS = -1;

	/**
	 * Drop a cookie.
	 * 
	 * @param expireSecs
	 *            Number of seconds until the cookie will expire. Use
	 *            {@link #dropSessionCookie(HttpServletResponse, String, String, String, String)} to drop a cookie that
	 *            is removed when the browser quits.
	 * @param path
	 *            The path for the cookie. Set to "/" for the whole domain.
	 */
	public static void dropCookie(HttpServletResponse response, String domain, String name, String value,
			int expireSecs, String path) {
		Cookie cookie = new Cookie(name, value);
		cookie.setDomain(domain);
		cookie.setMaxAge(expireSecs);
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	/**
	 * Drop a cookie that will be cleared when the browser quits.
	 * 
	 * @param path
	 *            The path for the cookie. Set to "/" for the whole domain.
	 */
	public static void dropSessionCookie(HttpServletResponse response, String domain, String name, String value,
			String path) {
		dropCookie(response, domain, name, value, SESSION_COOKIE_EXPIRE_SECS, path);
	}

	/**
	 * Clear a cookie from the repsonse.
	 */
	public static void clearCookie(HttpServletResponse response, String domain, String cookieName, String path) {
		dropCookie(response, domain, cookieName, "", 0 /* 0 age deletes the cookie */, path);
	}

	/**
	 * Return the cookie associated with name or null if not found.
	 */
	public static Cookie getCookie(HttpServletRequest request, String cookieName) {
		for (Cookie cookie : request.getCookies()) {
			if (cookieName.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}

	/**
	 * Return the cookie value associated with name or null if not found.
	 */
	public static String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie cookie = getCookie(request, cookieName);
		if (cookie == null) {
			return null;
		} else {
			return cookie.getValue();
		}
	}
}
