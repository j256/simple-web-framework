package com.j256.simplewebframework.params;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.handler.RequestContext;
import com.j256.simplewebframework.util.StringUtils;

/**
 * Class which enumerates the source of values extracted from the request context.
 */
public enum ParamSource {

	QUERY {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) throws IOException {
			String value = request.getParameter(paramInfo.getName());
			return paramInfo.convertString(value, response);
		}
	},

	QUERY_ARRAY {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) throws IOException {
			String[] values = request.getParameterValues(paramInfo.getName());
			return paramInfo.convertStringArray(values);
		}
	},

	PATH {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) throws IOException {
			// /foo/{id}
			String[] parts = StringUtils.split(request.getPathInfo(), '/');
			if (paramInfo.getPathPartIndex() >= parts.length) {
				return null;
			} else {
				String value = parts[paramInfo.getPathPartIndex()];
				return paramInfo.convertString(value, response);
			}
		}
		@Override
		public boolean isDefaultValueAllowed() {
			return false;
		}
	},

	HEADER {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) throws IOException {
			String value = request.getHeader(paramInfo.getName());
			return paramInfo.convertString(value, response);
		}
	},

	COOKIE {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) throws IOException {
			for (Cookie cookie : request.getCookies()) {
				if (paramInfo.getName().equals(cookie.getName())) {
					if (paramInfo.getParamType() == Cookie.class) {
						return cookie;
					} else {
						String value = cookie.getValue();
						return paramInfo.convertString(value, response);
					}
				}
			}
			return null;
		}
		@Override
		public boolean isNeedsConverter() {
			// this returns false because it could be a cookie or another value
			return false;
		}
	},

	CONTEXT {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) {
			if (paramInfo.getParamType() == HttpServletRequest.class) {
				return request;
			} else if (paramInfo.getParamType() == HttpServletResponse.class) {
				return response;
			} else {
				return new RequestContext(baseRequest, request, response);
			}
		}
		@Override
		public boolean isNeedsConverter() {
			return false;
		}
	},

	AUTH_TYPE {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) {
			return request.getAuthType();
		}
		@Override
		public boolean isNeedsConverter() {
			return false;
		}
	},

	HTTP_SESSION {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) {
			request.getRemoteHost();
			return request.getSession();
		}
		@Override
		public boolean isNeedsConverter() {
			return false;
		}
	},

	MULTI_PART {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) throws IOException {
			try {
				return request.getParts();
			} catch (ServletException e) {
				// this is thrown if contentType != "multipart/form-data" or on parse errors
				throw new IOException("problem getting the multi-part information from the request", e);
			}
		}
		@Override
		public boolean isNeedsConverter() {
			return false;
		}
	},

	REMOTE_ADDR {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) {
			return request.getRemoteAddr();
		}
		@Override
		public boolean isNeedsConverter() {
			return false;
		}
	},

	CLIENT_ADDRS {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) {
			String header = request.getHeader("X-Forwarded-For");
			String remoteAddr = request.getRemoteAddr();
			if (header == null) {
				return new String[] { remoteAddr };
			}

			String[] parts = StringUtils.split(header, ',');
			String[] result = new String[parts.length + 1];
			for (int i = 0; i < parts.length; i++) {
				result[i] = parts[i].trim();
			}
			result[parts.length] = remoteAddr;
			return result;
		}
		@Override
		public boolean isNeedsConverter() {
			return false;
		}
	},

	REMOTE_USER {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) {
			return request.getRemoteUser();
		}
		@Override
		public boolean isNeedsConverter() {
			return false;
		}
	},

	REQUEST_ATTRIBUTE {
		@Override
		public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
				ParamInfo paramInfo) {
			return request.getAttribute(paramInfo.getName());
		}
	},

	// end
	;

	/**
	 * Extract the parameter for the name from the request.
	 */
	public abstract Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			ParamInfo paramInfo) throws IOException;

	/**
	 * Returns true if this source-type needs a converter otherwise false.
	 */
	public boolean isNeedsConverter() {
		return true;
	}

	/**
	 * Get the associated default value.
	 */
	public boolean isDefaultValueAllowed() {
		return true;
	}
}
