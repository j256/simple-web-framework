package com.j256.simplewebframework.handler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.j256.simplewebframework.displayer.ResultDisplayer;
import com.j256.simplewebframework.handler.MethodWrapper.RequestType;
import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.util.ResponseUtils;
import com.j256.simplewebframework.util.ResponseUtils.HttpErrorCode;

/**
 * Handler that wraps one or many {@link WebService} classes and takes the requests and makes method calls to the
 * configured web-services.
 * 
 * @author graywatson
 */
public class ServiceHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

	private String handlerPathPrefix = "";

	private final Map<String, Map<String, MethodWrapper>> typePathMaps =
			new HashMap<String, Map<String, MethodWrapper>>();
	private final Map<Class<?>, ResultDisplayer> displayerClassMap = new HashMap<Class<?>, ResultDisplayer>();
	private final Map<String, ResultDisplayer> displayerMimeTypeMap = new HashMap<String, ResultDisplayer>();
	private final List<ResultDisplayer> runtimeMatchDisplayers = new ArrayList<ResultDisplayer>();
	private boolean pathParam;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		if (baseRequest.isHandled()) {
			return;
		}
		String pathInfo = request.getPathInfo();
		MethodWrapper methodWrapper = null;
		Map<String, MethodWrapper> pathMap = typePathMaps.get(request.getMethod());
		if (pathMap != null) {
			methodWrapper = pathMap.get(pathInfo);
		}
		if (methodWrapper == null) {
			if (pathParam) {
				for (Map.Entry<String, MethodWrapper> entry : pathMap.entrySet()) {
					String methodPath = entry.getKey();
					/*
					 * TODO: is this right? Shouldn't we match up to the dynamic parameter or something? Need to test
					 * this.
					 */
					if (pathInfo.startsWith(methodPath) && pathInfo.length() > methodPath.length()
							&& pathInfo.charAt(methodPath.length()) == '/') {
						methodWrapper = entry.getValue();
						break;
					}
				}
			}
			if (methodWrapper == null) {
				return;
			}
		}

		// ignore the request if it does not match @GET, @POST, etc.
		if (!methodWrapper.isAllowedRequest(request)) {
			return;
		}

		Object result;
		try {
			result = methodWrapper.processRequest(baseRequest, request, response);
		} finally {
			if (response.isCommitted()) {
				baseRequest.setHandled(true);
			}
		}
		if (result == null) {
			// if the process method returns void and we did not throw then we have handled the request we guess.
			if (methodWrapper.isReturnsVoid()) {
				baseRequest.setHandled(true);
			}
			return;
		}

		// we need to take a look at the class here and find a displayer
		Class<? extends Object> resultClass = result.getClass();
		String resultMimeType = response.getContentType();
		ResultDisplayer displayer = displayerClassMap.get(resultClass);
		if (displayer == null) {
			// after we look up the class returned, we check the content-type
			displayer = displayerMimeTypeMap.get(resultMimeType);

			// if we did not find a specific class or specific content-type then check the runtime-match displayers
			if (displayer == null) {
				for (ResultDisplayer matchedDisplayer : runtimeMatchDisplayers) {
					if (matchedDisplayer.canRender(resultClass, resultMimeType)) {
						displayer = matchedDisplayer;
						break;
					}
				}
			}
		}

		if (displayer == null) {
			/*
			 * Result was returned but cannot be displayed so it is ignored and the request may not be marked as
			 * handled.
			 */
			logger.debug("Could not display result of class {}, mime-type {}", resultClass, resultMimeType);
		} else {
			if (displayer.renderResult(baseRequest, request, response, result)) {
				baseRequest.setHandled(true);
			} else {
				/*
				 * Displayer was not able to render the result so the result is ignored and the request may not be
				 * marked as handled.
				 */
				ResponseUtils.sendError(response, HttpErrorCode.INTERNAL_SERVER_ERROR);
			}
		}
	}

	/**
	 * Register a web-service with the service handler. Services can also be injected on the class using
	 * {@link #setWebServices(Object[])}.
	 */
	public void registerWebService(Object webService) {
		if (!webService.getClass().isAnnotationPresent(WebService.class)) {
			throw new IllegalArgumentException("Expected @WebService annotation on class "
					+ webService.getClass().getSimpleName());
		}

		String classPathPrefix = "";
		Path servicePath = webService.getClass().getAnnotation(Path.class);
		if (servicePath != null) {
			classPathPrefix = servicePath.value();
		}

		Produces produces = webService.getClass().getAnnotation(Produces.class);
		String webServiceContentType = null;
		if (produces != null) {
			String contentTypes[] = produces.value();
			if (contentTypes != null && contentTypes.length > 0) {
				webServiceContentType = contentTypes[0];
			}
		}

		// now process the class' methods all the way up the object chain
		for (Class<?> clazz = webService.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {

			// process the methods on the class looking for @WebMethod
			processMethods(webService, classPathPrefix, webServiceContentType, clazz);

			// process the class interfaces as well
			for (Class<?> interfaceClass : clazz.getInterfaces()) {
				processMethods(webService, classPathPrefix, webServiceContentType, interfaceClass);
			}
		}
	}

	/**
	 * Register a result displayer with this service handler.
	 */
	public void registerResultDisplayer(ResultDisplayer resultDisplayer) {
		boolean runtimeMatch = true;
		if (resultDisplayer.getHandledClasses() != null) {
			for (Class<?> clazz : resultDisplayer.getHandledClasses()) {
				displayerClassMap.put(clazz, resultDisplayer);
			}
			runtimeMatch = false;
		}
		if (resultDisplayer.getHandledMimeTypes() != null) {
			for (String mimeType : resultDisplayer.getHandledMimeTypes()) {
				displayerMimeTypeMap.put(mimeType, resultDisplayer);
			}
			runtimeMatch = false;
		}
		if (runtimeMatch) {
			runtimeMatchDisplayers.add(resultDisplayer);
		}
	}

	public void setPathPrefix(String pathPrefix) {
		this.handlerPathPrefix = pathPrefix;
	}

	public void setWebServices(Object[] webServices) {
		for (Object webService : webServices) {
			registerWebService(webService);
		}
	}

	public void setResultDisplayers(ResultDisplayer[] resultDisplayers) {
		for (ResultDisplayer resultDisplayer : resultDisplayers) {
			registerResultDisplayer(resultDisplayer);
		}
	}

	private void processMethods(Object webService, String classPathPrefix, String webServiceContentType, Class<?> clazz) {
		for (Method method : clazz.getMethods()) {

			// we only care about methods with @WebMethod
			if (!method.isAnnotationPresent(WebMethod.class)) {
				continue;
			}

			MethodWrapper wrapper =
					new MethodWrapper(webService, webServiceContentType, method, handlerPathPrefix, classPathPrefix);
			for (RequestType type : wrapper.getAllowedRequestTypes()) {
				Map<String, MethodWrapper> pathMap = typePathMaps.get(type.name());
				if (pathMap == null) {
					pathMap = new HashMap<String, MethodWrapper>();
					typePathMaps.put(type.name(), pathMap);
				}
				pathMap.put(wrapper.getFullPath(), wrapper);
			}
			if (wrapper.isPathParam()) {
				pathParam = true;
			}
		}
	}
}
