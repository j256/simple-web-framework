package com.j256.simplewebframework.freemarker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

/**
 * Implementation of {@link TemplateLoader} that that does some things specific to our environment.
 * {@link FileTemplateLoader} was used as a reference implementation but it used the canonical name of the base-dir
 * which caused problems with the CMS symbolic link both at start up and when we roll to a new version.
 * 
 * @author graywatson
 */
public class LocalFileTemplateLoader implements TemplateLoader {

	public File baseDir;

	public LocalFileTemplateLoader() {
		// for spring
	}

	public LocalFileTemplateLoader(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public Object findTemplateSource(String name) {
		File source = new File(baseDir, name);
		if (source.isFile()) {
			return source;
		} else {
			return null;
		}
	}

	@Override
	public long getLastModified(Object templateSource) {
		if (templateSource instanceof File) {
			return new Long(((File) templateSource).lastModified());
		} else {
			throw new IllegalArgumentException("templateSource is an unknown type: " + templateSource.getClass());
		}
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException {
		if (templateSource instanceof File) {
			return new InputStreamReader(new FileInputStream((File) templateSource), encoding);
		} else {
			throw new IllegalArgumentException("templateSource is an unknown type: " + templateSource.getClass());
		}
	}

	@Override
	public void closeTemplateSource(Object templateSource) {
		// noop
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
		// it may not exist yet because CMS is going to download and create it
	}
}
