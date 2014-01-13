package com.j256.simplewebframework.handler;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.util.IoUtils;

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
	 * Helper method to send an error back as the response.
	 * 
	 * @return True if it worked otherwise false if it threw an exception.
	 */
	public boolean sendError(HttpErrorCode errorCode, String message) {
		return sendError(errorCode.getHttpErrorCode(), message);
	}

	/**
	 * Helper method to send an error back as the response. For enumerated errors, see
	 * {@link #sendError(HttpErrorCode, String)}.
	 * 
	 * @return True if it worked otherwise false if it threw an exception.
	 */
	public boolean sendError(int error, String message) {
		try {
			if (message == null) {
				response.sendError(error);
			} else {
				response.sendError(error, message);
			}
			return true;
		} catch (IOException e) {
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
	 * Helper method to send an error back in the response object. For enumerated errors, see
	 * {@link #sendError(HttpErrorCode, String)}.
	 * 
	 * @return True if it worked otherwise false if it threw an exception.
	 */
	public static boolean sendError(HttpServletResponse response, int error, String message) {
		try {
			if (message == null) {
				response.sendError(error);
			} else {
				LoggingHandler.addExtraDetail("reason", message);
				response.sendError(error, message);
			}
			return true;
		} catch (IOException e) {
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
		CONTINUE(HttpServletResponse.SC_CONTINUE),
		SWITCHING_PROTOCOLS(HttpServletResponse.SC_SWITCHING_PROTOCOLS),
		// 2XX - ok
		OK(HttpServletResponse.SC_OK),
		CREATED(HttpServletResponse.SC_CREATED),
		ACCEPTED(HttpServletResponse.SC_ACCEPTED),
		NON_AUTHORITATIVE_INFORMATION(HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION),
		NO_CONTENT(HttpServletResponse.SC_NO_CONTENT),
		RESET_CONTENT(HttpServletResponse.SC_RESET_CONTENT),
		PARTIAL_CONTENT(HttpServletResponse.SC_PARTIAL_CONTENT),
		// 3XX - moved
		MULTIPLE_CHOICES(HttpServletResponse.SC_MULTIPLE_CHOICES),
		MOVED_PERMANENTLY(HttpServletResponse.SC_MOVED_PERMANENTLY),
		MOVED_TEMPORARILY(HttpServletResponse.SC_MOVED_TEMPORARILY),
		SEE_OTHER(HttpServletResponse.SC_SEE_OTHER),
		NOT_MODIFIED(HttpServletResponse.SC_NOT_MODIFIED),
		USE_PROXY(HttpServletResponse.SC_USE_PROXY),
		TEMPORARY_REDIRECT(HttpServletResponse.SC_TEMPORARY_REDIRECT),
		// 4XX - bad
		BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST),
		UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED),
		PAYMENT_REQUIRED(HttpServletResponse.SC_PAYMENT_REQUIRED),
		FORBIDDEN(HttpServletResponse.SC_FORBIDDEN),
		NOT_FOUND(HttpServletResponse.SC_NOT_FOUND),
		METHOD_NOT_ALLOWED(HttpServletResponse.SC_METHOD_NOT_ALLOWED),
		NOT_ACCEPTABLE(HttpServletResponse.SC_NOT_ACCEPTABLE),
		PROXY_AUTHENTICATION_REQUIRED(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED),
		REQUEST_TIMEOUT(HttpServletResponse.SC_REQUEST_TIMEOUT),
		CONFLICT(HttpServletResponse.SC_CONFLICT),
		GONE(HttpServletResponse.SC_GONE),
		LENGTH_REQUIRED(HttpServletResponse.SC_LENGTH_REQUIRED),
		PRECONDITION_FAILED(HttpServletResponse.SC_PRECONDITION_FAILED),
		REQUEST_ENTITY_TOO_LARGE(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE),
		REQUEST_URI_TOO_LONG(HttpServletResponse.SC_REQUEST_URI_TOO_LONG),
		UNSUPPORTED_MEDIA_TYPE(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE),
		REQUESTED_RANGE_NOT_SATISFIABLE(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE),
		EXPECTATION_FAILED(HttpServletResponse.SC_EXPECTATION_FAILED),
		// 5XX - internal
		INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
		NOT_IMPLEMENTED(HttpServletResponse.SC_NOT_IMPLEMENTED),
		BAD_GATEWAY(HttpServletResponse.SC_BAD_GATEWAY),
		SERVICE_UNAVAILABLE(HttpServletResponse.SC_SERVICE_UNAVAILABLE),
		GATEWAY_TIMEOUT(HttpServletResponse.SC_GATEWAY_TIMEOUT),
		HTTP_VERSION_NOT_SUPPORTED(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED),
		// end
		;

		private int httpErrorCode;

		private HttpErrorCode(int httpErrorCode) {
			this.httpErrorCode = httpErrorCode;
		}

		public int getHttpErrorCode() {
			return httpErrorCode;
		}
	}
}
