package com.j256.simplewebframework.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * IO utility methods copied from elsewhere.
 * 
 * @author graywatson
 */
public class IOUtils {

	/**
	 * Close a closeable and ignore any errors.
	 */
	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignored
			}
		}
	}
}
