package com.j256.simplewebframework.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Local string utils so we don't have to have dependencies.
 * 
 * @author graywatson
 */
public class StringUtils {

	private final static String[] EMPTY_STRINGS = new String[0];

	/**
	 * Split a string by a separator character. Empty parts will be preserved.
	 */
	public static String[] split(String str, char separatorChar) {
		return split(str, separatorChar, false);
	}

	/**
	 * Split a string by a separator character.
	 */
	public static String[] split(String str, char separatorChar, boolean preserveAllTokens) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return EMPTY_STRINGS;
		}
		List<String> list = new ArrayList<String>();
		int argStart = 0;
		for (int i = 0; i < len; i++) {
			if (str.charAt(i) == separatorChar) {
				if (preserveAllTokens || i > argStart) {
					list.add(str.substring(argStart, i));
				}
				argStart = i + 1;
			}
		}
		if (preserveAllTokens || argStart < len) {
			list.add(str.substring(argStart, len));
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Replaces a String with another String inside a larger String, for the first {@code max} values of the search
	 * String.
	 */
	public static String replace(String text, String searchString, String replacement) {
		if (text == null || text.length() == 0 || searchString == null || searchString.length() == 0
				|| replacement == null) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end < 0) {
			return text;
		}
		int replLength = searchString.length();
		// we assume that we will find 16 of these to replace
		int increase = (replacement.length() - replLength) * 8;
		if (increase < 0) {
			increase = 0;
		}
		StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end >= 0) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}
}
