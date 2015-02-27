package com.j256.simplewebframework.displayer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

/**
 * Class which displays a particular result object returned by a service handler method. The class either specifies an
 * array of classes that it can display or an array of mime-types that it can handle.
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
	 * Return true if this class can render this result otherwise will return false. This will never be called if the
	 * classes or mime-types are specified instead. Classes and then mime-types are matched before this.
	 */
	public boolean canRender(Class<?> resultClass, String mimeType);

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
