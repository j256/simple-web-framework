package com.j256.simplewebframework.displayer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

/**
 * Convenience class which implements the methods in ResultDisplay for classes which render a single, specific result
 * class.
 * 
 * @author graywatson
 */
public abstract class SingleClassResultDisplayer<T> implements ResultDisplayer {

	private final Class<T> displayedClass;

	public SingleClassResultDisplayer(Class<T> displayedClass) {
		this.displayedClass = displayedClass;
	}

	/**
	 * Same as {@link #renderResult(Request, HttpServletRequest, HttpServletResponse, Object)} but with a specific typed
	 * object.
	 */
	protected abstract boolean renderTypedResult(Request baseRequest, HttpServletRequest request,
			HttpServletResponse response, T result) throws IOException;

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { displayedClass };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean canRender(Class<?> resultClass, String mimeType) {
		return false;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		@SuppressWarnings("unchecked")
		T castResult = (T) result;
		return renderTypedResult(baseRequest, request, response, castResult);
	}
}
