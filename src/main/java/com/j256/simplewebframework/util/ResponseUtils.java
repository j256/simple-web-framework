package com.j256.simplewebframework.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.j256.simplewebframework.handler.LoggingHandler;

/**
 * Response utilities.
 * 
 * @author graywatson
 */
public class ResponseUtils {

	/**
	 * Helper method to send an error back in the response object. This also closes the response output stream.
	 * 
	 * @return True if it worked otherwise false if it threw an exception.
	 */
	public static boolean sendError(HttpServletResponse response, HttpErrorCode errorCode) {
		return sendError(response, errorCode, null);
	}

	/**
	 * Helper method to send an error back in the response object. This also closes the response output stream.
	 * 
	 * @return True if it worked otherwise false if it threw an exception.
	 */
	public static boolean sendError(HttpServletResponse response, HttpErrorCode errorCode, String message) {
		try {
			if (message == null) {
				message = errorCode.defaultMessage;
			} else {
				// NOTE: we do this even if the logging handler isn't configured because it doesn't cost much
				LoggingHandler.addExtraDetail("reason", message);
			}
			response.sendError(errorCode.httpErrorCode, message);
			return true;
		} catch (IOException e) {
			response.setStatus(errorCode.httpErrorCode);
			// ignore the error
			return false;
		} finally {
			try {
				IoUtils.closeQuietly(response.getOutputStream());
			} catch (IOException e) {
				// ignore error if there is nothing to close
			}
		}
	}

	/**
	 * Enumeration of the error codes in {@link HttpServletResponse}.
	 */
	public enum HttpErrorCode {
		// 1XX
		CONTINUE(HttpServletResponse.SC_CONTINUE, "continue"),
		SWITCHING_PROTOCOLS(HttpServletResponse.SC_SWITCHING_PROTOCOLS, "switching protocols"),
		// 2XX - ok
		OK(HttpServletResponse.SC_OK, "ok"),
		CREATED(HttpServletResponse.SC_CREATED, "created"),
		ACCEPTED(HttpServletResponse.SC_ACCEPTED, "accepted"),
		NON_AUTHORITATIVE_INFORMATION(HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION,
				"non-authoritative information"),
		NO_CONTENT(HttpServletResponse.SC_NO_CONTENT, "no content"),
		RESET_CONTENT(HttpServletResponse.SC_RESET_CONTENT, "reset content"),
		PARTIAL_CONTENT(HttpServletResponse.SC_PARTIAL_CONTENT, "partial content"),
		// 3XX - moved
		MULTIPLE_CHOICES(HttpServletResponse.SC_MULTIPLE_CHOICES, "multiple choices"),
		MOVED_PERMANENTLY(HttpServletResponse.SC_MOVED_PERMANENTLY, "moved permanently"),
		MOVED_TEMPORARILY(HttpServletResponse.SC_MOVED_TEMPORARILY, "moved temporarily"),
		SEE_OTHER(HttpServletResponse.SC_SEE_OTHER, "see other"),
		NOT_MODIFIED(HttpServletResponse.SC_NOT_MODIFIED, "not modified"),
		USE_PROXY(HttpServletResponse.SC_USE_PROXY, "use proxy"),
		TEMPORARY_REDIRECT(HttpServletResponse.SC_TEMPORARY_REDIRECT, "temporary redirect"),
		// 4XX - bad
		BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "bad request"),
		UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "unauthorized"),
		PAYMENT_REQUIRED(HttpServletResponse.SC_PAYMENT_REQUIRED, "payment required"),
		FORBIDDEN(HttpServletResponse.SC_FORBIDDEN, "forbidden"),
		NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "not found"),
		METHOD_NOT_ALLOWED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "method not allowed"),
		NOT_ACCEPTABLE(HttpServletResponse.SC_NOT_ACCEPTABLE, "not acceptable"),
		PROXY_AUTHENTICATION_REQUIRED(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED,
				"proxy authentication required"),
		REQUEST_TIMEOUT(HttpServletResponse.SC_REQUEST_TIMEOUT, "request timeout"),
		CONFLICT(HttpServletResponse.SC_CONFLICT, "conflict"),
		GONE(HttpServletResponse.SC_GONE, "gone"),
		LENGTH_REQUIRED(HttpServletResponse.SC_LENGTH_REQUIRED, "length required"),
		PRECONDITION_FAILED(HttpServletResponse.SC_PRECONDITION_FAILED, "precondition failed"),
		REQUEST_ENTITY_TOO_LARGE(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "request entity too large"),
		REQUEST_URI_TOO_LONG(HttpServletResponse.SC_REQUEST_URI_TOO_LONG, "request uri too long"),
		UNSUPPORTED_MEDIA_TYPE(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "unsupported media type"),
		REQUESTED_RANGE_NOT_SATISFIABLE(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE,
				"requested range not satisfiable"),
		EXPECTATION_FAILED(HttpServletResponse.SC_EXPECTATION_FAILED, "expectation failed"),
		// 5XX - internal
		INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error"),
		NOT_IMPLEMENTED(HttpServletResponse.SC_NOT_IMPLEMENTED, "not implemented"),
		BAD_GATEWAY(HttpServletResponse.SC_BAD_GATEWAY, "bad gateway"),
		SERVICE_UNAVAILABLE(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "service unavailable"),
		GATEWAY_TIMEOUT(HttpServletResponse.SC_GATEWAY_TIMEOUT, "gateway timeout"),
		HTTP_VERSION_NOT_SUPPORTED(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED, "http version not supported"),
		// end
		;

		private int httpErrorCode;
		private String defaultMessage;

		private HttpErrorCode(int httpErrorCode, String defaultMessage) {
			this.httpErrorCode = httpErrorCode;
			this.defaultMessage = defaultMessage;
		}

		public int getHttpErrorCode() {
			return httpErrorCode;
		}

		public String getDefaultMessage() {
			return defaultMessage;
		}
	}
}
