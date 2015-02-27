package com.j256.simplewebframework.params;

import java.io.IOException;

/**
 * Converts the values for parameters to native types.
 * 
 * @author graywatson
 */
public interface ParamConverter {

	/**
	 * Return the default value associated with this parameter;
	 */
	public Object getDefaultValue();

	/**
	 * Convert a string parameter into a native type.
	 */
	public Object convertString(String param) throws IOException;

	/**
	 * Convert a string parameter into a native type.
	 */
	public Object convertStringArray(String[] params) throws IOException;

	/**
	 * Return true if the parameter can be null otherwise false to throw exception if it is null.
	 */
	public boolean isCanBeNull();
}
