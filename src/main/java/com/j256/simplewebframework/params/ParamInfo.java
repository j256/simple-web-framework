package com.j256.simplewebframework.params;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.handler.RequestContext;
import com.j256.simplewebframework.util.ResponseUtils;
import com.j256.simplewebframework.util.ResponseUtils.HttpErrorCode;

/**
 * Information about the parameter in terms of where we get it from and how we convert it.
 * 
 * @author graywatson
 */
public class ParamInfo {

	private final Object webService;
	private final Method method;
	private final int paramNum;
	private final ParamConverter converter;
	private final boolean isArray;
	private final Class<?> paramType;
	private String name;
	private String defaultValue;
	private ParamSource paramSource;
	private int pathPartIndex;

	public ParamInfo(Object webService, Method method, int paramNum, Class<?> type, Annotation[] annotations,
			String[] pathParts) {

		this.webService = webService;
		this.method = method;
		this.paramNum = paramNum;
		this.paramType = type;

		if (type.isArray()) {
			this.isArray = true;
			type = type.getComponentType();
		} else {
			this.isArray = false;
		}

		// special types that automatically get context information
		if (type == RequestContext.class || type == HttpServletRequest.class || type == HttpServletResponse.class) {
			this.paramSource = ParamSource.CONTEXT;
		}

		for (Annotation anno : annotations) {
			if (anno instanceof QueryParam) {
				QueryParam queryParam = (QueryParam) anno;
				this.name = queryParam.value();
				if (this.isArray) {
					this.paramSource = ParamSource.QUERY_ARRAY;
				} else {
					this.paramSource = ParamSource.QUERY;
				}
			} else if (anno instanceof HeaderParam) {
				HeaderParam headerParam = (HeaderParam) anno;
				this.name = headerParam.value();
				this.paramSource = ParamSource.HEADER;
			} else if (anno instanceof PathParam) {
				if (pathParts == null) {
					throw new IllegalArgumentException("@PathParam annotation but no {...} in the path for " + this);
				}
				PathParam pathParam = (PathParam) anno;
				this.name = pathParam.value();
				this.paramSource = ParamSource.PATH;
				String match = "{" + this.name + "}";
				for (int partC = 0; partC < pathParts.length; partC++) {
					if (pathParts[partC].equals(match)) {
						this.pathPartIndex = partC;
					}
				}
			} else if (anno instanceof CookieParam) {
				CookieParam cookieParam = (CookieParam) anno;
				this.name = cookieParam.value();
				this.paramSource = ParamSource.COOKIE;
			} else if (anno instanceof DefaultValue) {
				if (this.isArray) {
					throw new IllegalArgumentException("Cannot specify a default value for an array type for " + this);
				} else {
					this.defaultValue = ((DefaultValue) anno).value();
				}
			} else if (anno instanceof RemoteAddrParam) {
				// annotation is just a marker annotation so no name
				if (type == String.class) {
					this.paramSource = ParamSource.REMOTE_ADDR;
				} else {
					throw new IllegalArgumentException("@RemoteAddrParam class must be of type String for " + this);
				}
			} else if (anno instanceof AuthTypeParam) {
				// annotation is just a marker annotation so no name
				if (type == String.class) {
					this.paramSource = ParamSource.AUTH_TYPE;
				} else {
					throw new IllegalArgumentException("@AuthTypeParam class must be of type String for " + this);
				}
			} else if (anno instanceof RequestAttribute) {
				RequestAttribute attribute = (RequestAttribute) anno;
				this.name = attribute.value();
				// annotation is just a marker annotation so no name
				this.paramSource = ParamSource.REQUEST_ATTRIBUTE;
			} else if (anno instanceof RemoteUserParam) {
				// annotation is just a marker annotation so no name
				if (type == String.class) {
					this.paramSource = ParamSource.REMOTE_USER;
				} else {
					throw new IllegalArgumentException("@RemoteUserParam class must be of type String for " + this);
				}
			} else if (anno instanceof SessionParam) {
				// annotation is just a marker annotation so no name
				if (type == HttpSession.class) {
					this.paramSource = ParamSource.HTTP_SESSION;
				} else {
					throw new IllegalArgumentException("@SessionParam class must be of type HttpSession for " + this);
				}
			} else if (anno instanceof MultiPartParam) {
				// annotation is just a marker annotation so no name
				if (Collection.class.isAssignableFrom(type)) {
					this.paramSource = ParamSource.MULTI_PART;
				} else {
					throw new IllegalArgumentException(
							"@MultiPartParam class must be of type Collection<javax.servlet.http.Part> for " + this);
				}
			} else if (anno instanceof ClientAddrsParam) {
				// annotation is just a marker annotation so no name
				if (type == String[].class) {
					this.paramSource = ParamSource.CLIENT_ADDRS;
				} else {
					throw new IllegalArgumentException("@ClientAddrsParam class must be of type String[] for " + this);
				}
			} else if (anno instanceof FormParam) {
				FormParam formParam = (FormParam) anno;
				this.name = formParam.value();
				if (this.isArray) {
					this.paramSource = ParamSource.QUERY_ARRAY;
				} else {
					this.paramSource = ParamSource.QUERY;
				}
			}
		}

		if (this.paramSource == null) {
			throw new IllegalArgumentException(
					"Must have some sort of @QueryParam, @HeaderParam, @PathParam, ... annotation for " + this);
		}

		if (this.paramSource.isNeedsConverter()) {
			this.converter = ParamType.classToConverter(type);
			if (this.converter == null) {
				throw new IllegalArgumentException("Unknown parameter type " + type + " for " + this);
			}
		} else {
			this.converter = ParamType.getNoopConverter();
		}
	}

	/**
	 * Extract the string value from the request and convert it into a native object according to its source and
	 * converter information.
	 */
	public Object extractValue(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		try {
			Object value = paramSource.extractValue(baseRequest, request, response, this);
			if (value != null) {
				return value;
			} else if (response.isCommitted()) {
				// in case the response has already been committed
				return null;
			} else if (paramSource.isDefaultValueAllowed()) {
				return getDefaultValue();
			} else {
				ResponseUtils.sendError(response, HttpErrorCode.BAD_REQUEST, "missing value for " + this.getWebError());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!response.isCommitted()) {
				ResponseUtils.sendError(response, HttpErrorCode.BAD_REQUEST,
						"unable to process value for " + this.getWebError() + ": " + e.getMessage());
			}
			return null;
		}
	}

	/**
	 * Convert a string parameter into a native value.
	 */
	Object convertString(String value, HttpServletResponse response) throws IOException {
		if (value == null) {
			return null;
		} else {
			return converter.convertString(value);
		}
	}

	/**
	 * Convert a string array parameter into a native value.
	 */
	Object convertStringArray(String[] values) throws IOException {
		return converter.convertStringArray(values);
	}

	String getName() {
		return name;
	}

	public Class<?> getParamType() {
		return paramType;
	}

	int getPathPartIndex() {
		return pathPartIndex;
	}

	@Override
	public String toString() {
		if (name == null) {
			return "param #" + paramNum + ", method '" + method.getName() + "', class '"
					+ webService.getClass().getSimpleName() + "'";
		} else {
			return "param '" + name + "' (#" + paramNum + "), method '" + method.getName() + "', class '"
					+ webService.getClass().getSimpleName() + "'";
		}
	}

	/**
	 * Return information about the parameter that is suitable for external use.
	 */
	private String getWebError() {
		if (name == null) {
			return "method " + method.getName() + "(param #" + paramNum + ")";
		} else {
			return "method " + method.getName() + "(param '" + name + "')";
		}
	}

	private Object getDefaultValue() throws IOException {
		if (isArray) {
			return null;
		} else if (defaultValue == null) {
			return converter.getDefaultValue();
		} else {
			return converter.convertString(defaultValue);
		}
	}
}
