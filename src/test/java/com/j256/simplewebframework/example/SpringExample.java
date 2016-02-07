package com.j256.simplewebframework.example;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.j256.simplewebframework.freemarker.ModelView;

/**
 * Similar to the {@link FreemarkerExample} but this uses spring to configure the server, displayers, and handlers. The
 * test server runs on port 8080 which demonstrates some static files as well as service objects supported by the
 * SimpleWebFramework.
 * 
 * @author graywatson
 */
public class SpringExample {

	private static final String[] SPRING_CONFIG_FILES =
			new String[] { "classpath:/com/j256/simplewebframework/example/spring.xml" };

	public static void main(String[] args) throws Exception {
		new SpringExample().doMain(args);
	}

	private void doMain(String[] args) throws Exception {

		// load our context
		ClassPathXmlApplicationContext mainContext = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILES);
		mainContext.registerShutdownHook();

		System.out.println("web-server running probably on port 8080");
	}

	/**
	 * Small web service which presents a simple form to the user and displays the results if any.
	 */
	@WebService
	@Produces({ "text/html" })
	protected static class OurService {

		private static final String SERVICE_VIEW = "service.html";

		@Path("/service")
		@GET
		@WebMethod
		public ModelView root(//
				@QueryParam("value") //
				String value) {
			// build our model for the view
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("value", value);
			// display our freemarker view
			return new ModelView(model, SERVICE_VIEW);
		}
	}
}
