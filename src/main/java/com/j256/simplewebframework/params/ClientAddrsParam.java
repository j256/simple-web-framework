package com.j256.simplewebframework.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method parameters which is set on a String[] param to show any X-Forwarded-For addreses and the final
 * remote-address.
 * 
 * @author graywatson
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientAddrsParam {
	// marker interface
}
