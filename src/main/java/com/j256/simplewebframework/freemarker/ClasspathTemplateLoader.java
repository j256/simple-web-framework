package com.j256.simplewebframework.freemarker;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import freemarker.cache.TemplateLoader;

/**
 * Implementation of {@link TemplateLoader} that that does some looks for stuff on the classpath as opposed to the
 * file-system.
 * 
 * @author graywatson
 */
public class ClasspathTemplateLoader implements TemplateLoader {

	public String basePath;
	public long timeStartMillis = System.currentTimeMillis();

	public ClasspathTemplateLoader() {
		// for spring
	}

	public ClasspathTemplateLoader(String basePath) {
		this.basePath = basePath;
	}

	@Override
	public Object findTemplateSource(String name) {
		if (basePath == null) {
			return getClass().getResourceAsStream(name);
		} else {
			return getClass().getResourceAsStream(basePath + name);
		}
	}

	@Override
	public long getLastModified(Object templateSource) {
		return timeStartMillis;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) {
		return new InputStreamReader((InputStream) templateSource);
	}

	@Override
	public void closeTemplateSource(Object templateSource) {
		// noop
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
