package com.j256.simplewebframework.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.params.ParamInfo;
import com.j256.simplewebframework.util.ResponseUtils;
import com.j256.simplewebframework.util.ResponseUtils.HttpErrorCode;
import com.j256.simplewebframework.util.StringUtils;

/**
 * Wrapper around a particular method-call. The encapsulates a particular method call with the parameter arguments. It
 * takes care of the the processing of the request with the
 * {@link #processRequest(Request, HttpServletRequest, HttpServletResponse)} method.
 * 
 * @author graywatson
 */
public class MethodWrapper {

	private static final Logger logger = LoggerFactory.getLogger(MethodWrapper.class);

	private final Object webService;
	private final Method method;
	private final boolean returnsVoid;

	private final int numParams;
	private final ParamInfo[] paramInfos;
	private final String contentType;
	private final RequestType[] allowedRequestTypes;
	private final String fullPath;
	private final boolean pathParam;

	public MethodWrapper(Object webService, String defaultContentType, Method method, String handlerPathPrefix,
			String classPathPrefix) {

		this.webService = webService;
		this.method = method;
		this.returnsVoid = (method.getReturnType() == void.class);

		// build our path
		String methodPath = "";
		Path path = method.getAnnotation(Path.class);
		if (path != null) {
			methodPath = path.value();
		}

		String fullPath = handlerPathPrefix + classPathPrefix + methodPath;
		if (fullPath.length() == 0) {
			throw new IllegalArgumentException("@Path annotation must be specified for method " + method.getName()
					+ " and/or class " + webService.getClass().getSimpleName());
		}

		Class<?>[] types = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		if (types.length != annotations.length) {
			throw new IllegalStateException("Type array length should match annotations array length");
		}

		// get the web-service class level annotation
		Produces produces = method.getAnnotation(Produces.class);
		if (produces == null) {
			contentType = defaultContentType;
		} else {
			String contentTypes[] = produces.value();
			if (contentTypes != null && contentTypes.length > 0) {
				contentType = contentTypes[0];
			} else {
				contentType = defaultContentType;
			}
		}

		// look for an instances of /{...}/ for @PathParam
		String[] pathParts = null;
		boolean pathParam = false;
		if (fullPath.indexOf("/{") >= 0) {
			pathParts = StringUtils.split(fullPath, '/');
			StringBuilder sb = new StringBuilder();
			for (String part : pathParts) {
				if (part.startsWith("{") && part.endsWith("}")) {
					pathParam = true;
					break;
				}
				if (part.length() > 0) {
					sb.append("/").append(part);
				}
			}
			fullPath = sb.toString();
		}
		this.pathParam = pathParam;
		this.fullPath = fullPath;

		this.numParams = types.length;
		this.paramInfos = new ParamInfo[this.numParams];
		for (int i = 0; i < this.numParams; i++) {
			this.paramInfos[i] = new ParamInfo(webService, method, i, types[i], annotations[i], pathParts);
		}

		List<RequestType> requestTypes = new ArrayList<RequestType>(0);
		for (Annotation anno : method.getAnnotations()) {
			if (anno instanceof GET) {
				requestTypes.add(RequestType.GET);
			} else if (anno instanceof HEAD) {
				requestTypes.add(RequestType.HEAD);
			} else if (anno instanceof POST) {
				requestTypes.add(RequestType.POST);
			} else if (anno instanceof PUT) {
				requestTypes.add(RequestType.PUT);
			} else if (anno instanceof DELETE) {
				requestTypes.add(RequestType.DELETE);
			}
		}
		if (requestTypes.size() == 0) {
			this.allowedRequestTypes = null;
		} else {
			this.allowedRequestTypes = requestTypes.toArray(new RequestType[requestTypes.size()]);
		}
	}

	/**
	 * Return true if this HTTP method is allowed by this parameter. If the method specifies any of {@code @GET},
	 * {@code @HEAD}, {@code @POST}, {@code @PUT}, and {@code @DELETE} then the HTTP method must match.
	 */
	public boolean isAllowedRequest(HttpServletRequest request) {
		if (allowedRequestTypes == null) {
			return true;
		}
		for (RequestType type : allowedRequestTypes) {
			if (type.name().equals(request.getMethod())) {
				return true;
			}
		}
		return false;
	}

	public boolean isReturnsVoid() {
		return returnsVoid;
	}

	public String getFullPath() {
		return fullPath;
	}

	public RequestType[] getAllowedRequestTypes() {
		return allowedRequestTypes;
	}

	public boolean isPathParam() {
		return pathParam;
	}

	/**
	 * Process our request and return the result object returned by the web-service method.
	 */
	public Object processRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		try {
			return doProcessRequest(baseRequest, request, response);
		} catch (Exception e) {
			if (!response.isCommitted()) {
				ResponseUtils.sendError(response, HttpErrorCode.INTERNAL_SERVER_ERROR, "unable to process request");
			}
			logger.error(e, "Request to method {} for class {} threw", method.getName(), webService.getClass()
					.getSimpleName());
			return null;
		}
	}

	@Override
	public String toString() {
		return "MethodWrapper [types=" + Arrays.toString(allowedRequestTypes) + ", path=" + fullPath + "]";
	}

	private Object doProcessRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Object[] params = new Object[numParams];
		for (int i = 0; i < numParams; i++) {
			params[i] = paramInfos[i].extractValue(baseRequest, request, response);
			// could have been an error
			if (response.isCommitted()) {
				baseRequest.setHandled(true);
				return null;
			}
		}

		Object result = method.invoke(webService, params);
		if (result != null && contentType != null && response.getContentType() == null) {
			response.setContentType(contentType);
		}
		return result;
	}

	/**
	 * Type of HTTP request.
	 */
	public enum RequestType {
		GET,
		HEAD,
		POST,
		PUT,
		DELETE,
		// end
		;
	}
}
