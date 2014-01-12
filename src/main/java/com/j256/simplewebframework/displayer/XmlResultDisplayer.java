package com.j256.simplewebframework.displayer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * XML displayer that uses the optional {@link Serializer} from the SimpleFramework XML package.
 * 
 * @author graywatson
 */
public class XmlResultDisplayer implements ResultDisplayer {

	private Serializer serializer = new Persister();

	@Override
	public Class<?>[] getHandledClasses() {
		return null;
	}

	@Override
	public String[] getHandledMimeTypes() {
		return new String[] { "text/xml" };
	}

	@Override
	public boolean renderResult(Request baseRequest, HttpServletRequest request, HttpServletResponse response,
			Object result) throws IOException {
		PrintWriter writer = response.getWriter();
		try {
			serializer.write(result, writer);
			return true;
		} catch (Exception e) {
			throw new IOException("could not write XML document to response", e);
		} finally {
			writer.close();
		}
	}
}
