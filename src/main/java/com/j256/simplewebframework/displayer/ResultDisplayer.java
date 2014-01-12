package com.j256.simplewebframework.displayer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

/**
 * This defines one or many mime-types that are rendered by this class. Either mime-types can be specified
 * 
 * @author graywatson
 */
public interface ResultDisplayer {

	/**
	 * Return what classes returned by the service methods that are rendered by this class. This can be null if the
	 * mime-type array has entries.
	 */
	public Class<?>[] getHandledClasses();

	/**
	 * Return what mime-types are rendered by this class. This can be null if the classes array has entries. Classes are
	 * matched first.
	 */
	public String[] getHandledMimeTypes();

	/**
	 * Render the result parameter returned by the service methods to the response. This will not be called if the
	 * WebResult class was not specified in {@link #getHandledClasses()}.
	 * 
	 * <p>
	 * <b>NOTE:</b> The HttpServletResponse stream or writer (whichever is used) must be closed by the time this method
	 * returns.
	 * </p>
	 * 
	 * @return true if it was rendered otherwise false.
	 */
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException;
}
