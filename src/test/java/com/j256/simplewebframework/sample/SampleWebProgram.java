package com.j256.simplewebframework.sample;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import com.j256.simplewebframework.displayer.StringResultDisplayer;
import com.j256.simplewebframework.handler.ServiceHandler;

/**
 * Little sample program which starts a test web-server up on port 8080 which demonstrates some of the features of the
 * SimpleWebFramework.
 * 
 * @author graywatson
 */
public class SampleWebProgram {

	/** default web port that we will server jetty results on */
	private static final int DEFAULT_WEB_PORT = 8080;

	public static void main(String[] args) throws Exception {
		// create jetty server
		Server server = new Server();
		// create the connector which receives HTTPD connections
		SelectChannelConnector connector = new SelectChannelConnector();
		// start it on the default port
		connector.setPort(DEFAULT_WEB_PORT);
		connector.setReuseAddress(true);
		server.addConnector(connector);

		// create a service handler
		ServiceHandler serviceHandler = new ServiceHandler();
		// register our service
		serviceHandler.registerWebService(new OurService());
		// register a displayer of String results
		serviceHandler.registerResultDisplayer(new StringResultDisplayer());

		// this could be a collection of handlers or ...
		server.setHandler(serviceHandler);
		server.start();

		// keeps on running because of the jetty threads so you will need to kill it
	}

	/**
	 * Small web service which presents a simple form to the user and displays the results if any.
	 */
	@WebService
	@Produces({ "text/html" })
	protected static class OurService {

		@Path("/")
		@GET
		@WebMethod
		public String root(//
				@QueryParam("value")//
				String value) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>\n");
			sb.append("<h1> OurService Web Server </h1>\n");
			if (value != null) {
				sb.append("<p> value is '" + value + "' </p>\n");
			}
			sb.append("<p><form>\n");
			sb.append("Please enter value: <input name='value' type='text'");
			if (value != null) {
				sb.append(" value='").append(value).append("'");
			}
			sb.append(" />");
			sb.append("<input type='submit' />\n");
			sb.append("</form></p>\n");
			sb.append("</body></html>\n");
			return sb.toString();
		}
	}
}
