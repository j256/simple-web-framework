package com.j256.simplewebframework.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.j256.simplewebframework.displayer.FileResultDisplayer.FileInfo;
import com.j256.simplewebframework.displayer.ResultDisplayer;
import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.resource.FileLocator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Any HTML files in the system will be rendered using this wrapper which sends the files back as a FreeMarker template.
 * 
 * @author graywatson
 */
public class FreemarkerHtmlDisplayer implements ResultDisplayer {

	private static final Logger logger = LoggerFactory.getLogger(FreemarkerHtmlDisplayer.class);

	private Configuration templateConfig;
	private FileLocator fileLocator;

	@Override
	public Class<?>[] getHandledClasses() {
		return new Class[] { ModelView.class, FileInfo.class };
	}

	@Override
	public String[] getHandledMimeTypes() {
		return null;
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		ModelView modelView;
		FileInfo fileInfo;
		if (result instanceof ModelView) {
			modelView = (ModelView) result;
			fileInfo = fileLocator.findFile(modelView.getView());
			if (fileInfo == null) {
				return false;
			}
		} else if (result instanceof FileInfo) {
			fileInfo = (FileInfo) result;
			modelView = new ModelView(new HashMap<String, Object>(), fileInfo.getPath());
		} else {
			throw new IllegalArgumentException("Cannot render the result of type: " + result.getClass().getSimpleName());
		}
		render(fileInfo, modelView.getModel(), request, response, response.getWriter());
		return true;
	}

	private boolean render(FileInfo fileInfo, Map<String, Object> model, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, Writer writer) throws IOException {

		servletResponse.setContentType("text/html");

		String path = fileInfo.getPath();
		File localFile = fileInfo.getFile();
		if (model == null) {
			model = new HashMap<String, Object>();
		}

		model.put("request", servletRequest);

		Template template;
		try {
			template = templateConfig.getTemplate(path);
		} catch (IOException e) {
			String msg = "problems getting template from path '" + path + "'";
			logger.error(msg, e);
			throw new IOException(msg, e);
		}

		try {
			template.process(model, writer);
			return true;
		} catch (TemplateException e) {
			String msg = "could not render template in " + localFile;
			logger.error(msg, e);
			throw new IOException(msg, e);
		} finally {
			// don't close it here
			writer.flush();
		}
	}

	public void setTemplateConfig(Configuration templateConfig) {
		this.templateConfig = templateConfig;
	}

	public void setFileLocator(FileLocator fileLocator) {
		this.fileLocator = fileLocator;
	}
}
