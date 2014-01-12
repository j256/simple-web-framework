package com.j256.simplewebframework.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testBasic() {
		String first = "1";
		char sep = '.';
		String second = "2";
		String third = "3";
		String match = first + sep + second + sep + third;
		String[] parts = StringUtils.split(match, sep);
		assertEquals(3, parts.length);
		assertEquals(first, parts[0]);
		assertEquals(second, parts[1]);
		assertEquals(third, parts[2]);

		parts = StringUtils.split(match, sep, true);
		assertEquals(3, parts.length);
		assertEquals(first, parts[0]);
		assertEquals(second, parts[1]);
		assertEquals(third, parts[2]);
	}

	@Test
	public void testNoSep() {
		String first = "1";
		char sep = '.';
		String[] parts = StringUtils.split(first, sep);
		assertEquals(1, parts.length);
		assertEquals(first, parts[0]);
	}

	@Test
	public void testStartSep() {
		String first = "1";
		char sep = '.';
		String match = sep + first;
		String[] parts = StringUtils.split(match, sep);
		assertEquals(1, parts.length);
		assertEquals(first, parts[0]);

		parts = StringUtils.split(match, sep, true);
		assertEquals(2, parts.length);
		assertEquals(0, parts[0].length());
		assertEquals(first, parts[1]);
	}

	@Test
	public void testEndSep() {
		String first = "1";
		char sep = '.';
		String match = first + sep;
		String[] parts = StringUtils.split(match, sep);
		assertEquals(1, parts.length);
		assertEquals(first, parts[0]);

		parts = StringUtils.split(match, sep, true);
		assertEquals(2, parts.length);
		assertEquals(first, parts[0]);
		assertEquals(0, parts[1].length());
	}

	@Test
	public void testDoubleSep() {
		String first = "1";
		char sep = '.';
		String second = "2";
		String match = first + sep + sep + second;
		String[] parts = StringUtils.split(match, sep);
		assertEquals(2, parts.length);
		assertEquals(first, parts[0]);
		assertEquals(second, parts[1]);

		parts = StringUtils.split(match, sep, true);
		assertEquals(3, parts.length);
		assertEquals(first, parts[0]);
		assertEquals(0, parts[1].length());
		assertEquals(second, parts[2]);
	}

	@Test
	public void testNull() {
		assertNull(StringUtils.split(null, '.'));
	}

	@Test
	public void testEmpty() {
		assertEquals(0, StringUtils.split("", '.').length);
	}
}
