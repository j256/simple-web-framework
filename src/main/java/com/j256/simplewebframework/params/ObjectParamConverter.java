package com.j256.simplewebframework.params;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Handles the conversion of an object to a parameter.
 * 
 * @author graywatson
 */
public class ObjectParamConverter implements ParamConverter {

	private final Class<?> clazz;
	private final Constructor<?> constructor;
	private final Method method;

	private ObjectParamConverter(Class<?> clazz, Constructor<?> constructor) {
		this.clazz = clazz;
		this.constructor = constructor;
		this.method = null;
	}

	private ObjectParamConverter(Class<?> clazz, Method method) {
		this.clazz = clazz;
		this.constructor = null;
		this.method = method;
	}

	public static ObjectParamConverter create(Class<?> clazz) {
		Constructor<?> constructor = null;
		try {
			// constructor that accepts a single String argument
			constructor = clazz.getConstructor(String.class);
			return new ObjectParamConverter(clazz, constructor);
		} catch (NoSuchMethodException e) {
			// ignore it
		}

		// static method named valueOf() or fromString() that accepts a single String argument
		Method method = findStringMethod(clazz, "valueOf");
		if (method == null) {
			method = findStringMethod(clazz, "fromString");
		}
		if (method == null) {
			return null;
		} else {
			return new ObjectParamConverter(clazz, method);
		}
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public Object convertString(String param) throws IOException {
		try {
			if (constructor == null) {
				return method.invoke(null, param);
			} else {
				return constructor.newInstance(param);
			}
		} catch (Exception e) {
			throw new IOException("could not instantiate an object for class " + clazz + " with param: " + param, e);
		}
	}

	@Override
	public Object convertStringArray(String[] params) {
		return null;
	}

	@Override
	public boolean isCanBeNull() {
		return true;
	}

	/**
	 * Find the appropriate static method that takes a string argument.
	 */
	private static Method findStringMethod(Class<?> clazz, String name) {
		Method method;
		try {
			method = clazz.getMethod(name, String.class);
		} catch (Exception e) {
			return null;
		}
		if (!Modifier.isStatic(method.getModifiers())) {
			return null;
		}
		if (method.getReturnType() == clazz) {
			return method;
		} else {
			return null;
		}

	}
}
