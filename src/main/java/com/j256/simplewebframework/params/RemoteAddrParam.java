package com.j256.simplewebframework.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method parameters which show the remote-ip of the user in question.
 * 
 * @author graywatson
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteAddrParam {
	// marker interface
}
