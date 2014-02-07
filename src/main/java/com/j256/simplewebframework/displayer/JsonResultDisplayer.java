package com.j256.simplewebframework.displayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.google.gson.Gson;
import com.j256.simplewebframework.util.IOUtils;

/**
 * Json displayer that uses the optional {@link Gson} package (com.google.gson). If you are using this displayer then
 * you need to import the Gson jars into your project.
 * 
 * @author graywatson
 */
public class JsonResultDisplayer implements ResultDisplayer {

	private static final Pattern MSIE_PATTERN = Pattern.compile(".* MSIE ([\\d.]+);.*");

	private Gson gson = new Gson();

	@Override
	public Class<?>[] getHandledClasses() {
		return null;
	}

	@Override
	public String[] getHandledMimeTypes() {
		return new String[] { "application/json" };
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent != null) {
			Matcher matcher = MSIE_PATTERN.matcher(userAgent);
			if (matcher.matches()) {
				try {
					float version = Float.parseFloat(matcher.group(1));
					if (version < 10.0) {
						response.setContentType("text/plain");
					}
				} catch (NumberFormatException e) {
					// ignored
				}
			}
		}
		PrintWriter writer = response.getWriter();
		try {
			gson.toJson(result, writer);
			return true;
		} catch (Exception e) {
			throw new IOException("could not write XML document to response", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
}
