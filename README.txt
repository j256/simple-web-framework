Little Java Jetty web framework which uses the JSR annotations.

Right now the docs on this package are very minimal.  My apologies.  Drop me a line if you
are interested in using this and I'll get some docs, tests, and sample programs going.

----------------------------------------------------------------------------------------

Here's a little sample web program using this framework.  Working sample in
SampleWebProgram.java.

public class SampleWebProgram {

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(8080);
		connector.setReuseAddress(true);
		server.addConnector(connector);

		ServiceHandler serviceHandler = new ServiceHandler();
		serviceHandler.registerWebService(new OurService());
		serviceHandler.registerResultDisplayer(new StringResultDisplayer());

		server.setHandler(serviceHandler);
		server.start();
	}

	@WebService @Produces({ "text/html" })
	protected static class OurService {

		@WebMethod @Path("/") @GET
		public String root(@QueryParam("value")	String value) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>\n");
			sb.append("<h1> OurService Web Server </h1>\n");
			if (value != null) {
				sb.append("<p> value is passed in as '" + value + "' </p>\n");
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
