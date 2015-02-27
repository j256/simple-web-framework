package com.j256.simplewebframework.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method parameters which show the remote-user that has authenticated for the request or null if none.
 * 
 * @author graywatson
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteUserParam {
	// marker interface
}
