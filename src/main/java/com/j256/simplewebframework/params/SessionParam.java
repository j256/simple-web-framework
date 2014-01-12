package com.j256.simplewebframework.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpSession;

/**
 * Annotation for method parameters which sets the session variable to a {@link HttpSession} parameter.
 * 
 * @author graywatson
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionParam {
	// marker interface
}
