package com.j256.simplewebframework.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method parameters which sets the parameter to a Part[] which has the multi-part data in it.
 * 
 * <p>
 * <b>NOTE</b> If the request is not of the right type of multi-part request then using this will cause a bad-request
 * error (400) to be returned.
 * </p>
 * 
 * @author graywatson
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiPartParam {
	// marker interface
}
