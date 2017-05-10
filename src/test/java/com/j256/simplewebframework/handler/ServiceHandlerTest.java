package com.j256.simplewebframework.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.easymock.EasyMock;
import org.eclipse.jetty.server.Request;
import org.junit.Test;

public class ServiceHandlerTest {

	@Test
	public void testBasic() throws Exception {
		ServiceHandler handler = new ServiceHandler();
		Service service = new Service();
		handler.registerWebService(service);

		Request baseRequest = EasyMock.createMock(Request.class);
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);

		expect(baseRequest.isHandled()).andReturn(false);
		expect(request.getMethod()).andReturn("GET");
		expect(request.getPathInfo()).andReturn("/");
		expect(request.getParameter("bar")).andReturn("bar2");
		expect(request.getParameter("baz")).andReturn(null);
		expect(request.getHeader("Content-Length")).andReturn("103");
		expect(response.isCommitted()).andReturn(false).anyTimes();
		baseRequest.setHandled(true);

		EasyMock.replay(baseRequest);
		replay(request, response);
		handler.handle(null, baseRequest, request, response);
		EasyMock.verify(baseRequest);
		verify(request, response);
	}

	@WebService
	protected class Service {
		@Path("/")
		@WebMethod
		public void foo(//
				@QueryParam("bar") //
				String bar, //
				@QueryParam("baz") @DefaultValue("100") //
				int baz, //
				@HeaderParam("Content-Length") //
				int contentLength) {
			System.out.println("foo(): bar = " + bar + ", baz = " + baz + ", len = " + contentLength);
		}
	}
}
