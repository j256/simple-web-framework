package com.j256.simplewebframework.params;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameter types and conversions.
 * 
 * NOTE: this is package because classes outside of this package should refer to {@link ParamConverter}.
 */
enum ParamType implements ParamConverter {

	BOOLEAN(boolean.class) {
		@Override
		public Object getDefaultValue() {
			return false;
		}
		@Override
		public Object convertStringArray(String[] params) {
			boolean[] result = new boolean[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Boolean) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) {
			if (param == null) {
				return false;
			} else if (param.length() == 0) {
				// if we just say &flag& then we assume that it should be true
				return true;
			} else if (param.equalsIgnoreCase("true") || param.equalsIgnoreCase("yes") || param.equals("1")
					|| param.equalsIgnoreCase("y") || param.equalsIgnoreCase("t") || param.equalsIgnoreCase("on")) {
				return true;
			} else {
				return false;
			}
		}
		@Override
		public boolean isCanBeNull() {
			// this is true if an argument is not specified then it will become false
			return true;
		}
	},

	BOOLEAN_OBJ(Boolean.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			Boolean[] result = new Boolean[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Boolean) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			if (param == null) {
				return null;
			} else {
				return BOOLEAN.convertString(param);
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	CHAR(char.class) {
		@Override
		public Object getDefaultValue() {
			return (char) 0;
		}
		@Override
		public Object convertStringArray(String[] params) {
			char[] result = new char[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Character) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) {
			if (param == null) {
				return null;
			} else if (param.length() == 0) {
				return (char) 0;
			} else {
				return param.charAt(0);
			}
		}
		@Override
		public boolean isCanBeNull() {
			return false;
		}
	},

	CHARACTER(Character.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) {
			Character[] result = new Character[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Character) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) {
			if (param == null) {
				return null;
			} else if (param.length() == 0) {
				return null;
			} else {
				return param.charAt(0);
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	BYTE(byte.class) {
		@Override
		public Object getDefaultValue() {
			return (byte) 0;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			byte[] result = new byte[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Byte) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			try {
				return Byte.parseByte(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return false;
		}
	},

	BYTE_OBJ(Byte.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			Byte[] result = new Byte[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Byte) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			if (param == null) {
				return null;
			}
			try {
				return Byte.parseByte(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	SHORT(short.class) {
		@Override
		public Object getDefaultValue() {
			return (short) 0;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			short[] result = new short[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Short) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			try {
				return Short.parseShort(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return false;
		}
	},

	SHORT_OBJ(Short.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			Short[] result = new Short[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Short) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			if (param == null) {
				return null;
			}
			try {
				return Short.parseShort(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	INT(int.class) {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			int[] result = new int[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Integer) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			try {
				return Integer.parseInt(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return false;
		}
	},

	INTEGER(Integer.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			Integer[] result = new Integer[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Integer) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			if (param == null) {
				return null;
			}
			try {
				return Integer.parseInt(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	LONG(long.class) {
		@Override
		public Object getDefaultValue() {
			return (long) 0;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			long[] result = new long[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Long) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			try {
				return Long.parseLong(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert parameter value '" + param + "' + to " + this);
			}
		}
		@Override
		public boolean isCanBeNull() {
			return false;
		}
	},

	LONG_OBJ(Long.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			Long[] result = new Long[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Long) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			if (param == null) {
				return null;
			}
			try {
				return Long.parseLong(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	FLOAT(float.class) {
		@Override
		public Object getDefaultValue() {
			return (float) 0;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			float[] result = new float[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Float) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			try {
				return Float.parseFloat(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return false;
		}
	},

	FLOAT_OBJ(Float.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			Float[] result = new Float[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Float) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			if (param == null) {
				return null;
			}
			try {
				return Float.parseFloat(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	DOUBLE(double.class) {
		@Override
		public Object getDefaultValue() {
			return (double) 0;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			double[] result = new double[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Double) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			try {
				return Double.parseDouble(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return false;
		}
	},

	DOUBLE_OBJ(Double.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) throws IOException {
			Double[] result = new Double[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = (Double) convertString(params[i]);
			}
			return result;
		}
		@Override
		public Object convertString(String param) throws IOException {
			if (param == null) {
				return null;
			}
			try {
				return Double.parseDouble(param);
			} catch (NumberFormatException e) {
				throw new IOException("could not convert " + this + " parameter value '" + param + "'");
			}
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	STRING(String.class) {
		@Override
		public Object getDefaultValue() {
			return null;
		}
		@Override
		public Object convertStringArray(String[] params) {
			String[] result = new String[params.length];
			for (int i = 0; i < params.length; i++) {
				result[i] = params[i];
			}
			return result;
		}
		@Override
		public Object convertString(String param) {
			return param;
		}
		@Override
		public boolean isCanBeNull() {
			return true;
		}
	},

	// end
	;

	private static final Map<Class<?>, ParamConverter> classParams = new HashMap<Class<?>, ParamConverter>();

	static {
		for (ParamType paramType : ParamType.values()) {
			classParams.put(paramType.clazz, paramType);
		}
	}

	private Class<?> clazz;

	private ParamType(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public abstract Object getDefaultValue();

	@Override
	public abstract Object convertString(String param) throws IOException;

	/**
	 * WARNING: We really can refactor this because the primitive types cannot convert from an Object into a primitive
	 * array slot without specific casting.
	 */
	@Override
	public abstract Object convertStringArray(String[] params) throws IOException;

	/**
	 * Lookup the parameter converter by class.
	 * 
	 * NOTE: this is here to save on the small utility class to do so.
	 */
	public static ParamConverter classToConverter(Class<?> clazz) {
		ParamConverter converter = classParams.get(clazz);
		if (converter == null) {
			converter = ObjectParamConverter.create(clazz);
		}
		return converter;
	}

	@Override
	public String toString() {
		return clazz.getSimpleName() + " type";
	}
}
