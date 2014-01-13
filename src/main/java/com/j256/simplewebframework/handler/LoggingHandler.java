package com.j256.simplewebframework.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.util.ResponseUtils.HttpErrorCode;
import com.j256.simplewebframework.util.ResponseUtils;
import com.j256.simplewebframework.util.StringUtils;

/**
 * A handler for Jetty that logs inbound requests to a slf4j Logger. This is designed to wrap around all other handlers
 * to be able to provide pageview and other logging.
 */
public class LoggingHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger("PAGEVIEW");

	private static final char SEPARATOR = '\t';
	private static final char BLANK_FIELD = '-';
	private static final char FIELD_VALUE_SEPARATOR = '=';
	private static final String EMPTY_VALUE = "";

	// constant used to ignore the request
	private static final FieldValue IGNORE_PAIR = new FieldValue("ignore", "me");

	private Handler handler;

	private static final ThreadLocal<List<FieldValue>> extraDetails = new ThreadLocal<List<FieldValue>>() {
		@Override
		protected List<FieldValue> initialValue() {
			return new ArrayList<FieldValue>();
		}
	};

	/**
	 * Send an error back to the response. This delegates to {@see ResponseUtils#sendError(HttpServletResponse,
	 * HttpErrorCode, String)}.
	 */
	public static void sendError(HttpServletResponse response, HttpErrorCode errorCode, String msg) {
		ResponseUtils.sendError(response, errorCode, msg);
	}

	/**
	 * Send an error back to the response. This delegates to {@see ResponseUtils#sendError(HttpServletResponse,
	 * HttpErrorCode)}.
	 */
	public static void sendError(HttpServletResponse response, HttpErrorCode errorCode) {
		ResponseUtils.sendError(response, errorCode);
	}

	/**
	 * Adds extra detail to the log message that will be printed to the pageview log after the request is finished being
	 * processed. This uses a {@link ThreadLocal} so it should be called only on the same thread as the request.
	 */
	public static void addExtraDetail(String key, Object value) {
		if (key == null) {
			return;
		}
		if (value == null) {
			value = EMPTY_VALUE;
		}
		extraDetails.get().add(new FieldValue(key, value.toString()));
	}

	/**
	 * Parses the extra detail string that was encoded in the log lines. This is for code that is parsing logs generated
	 * by this class.
	 */
	public static Map<String, String> parseExtraDetail(String extraDetailString) {
		String[] pairStrings = StringUtils.split(extraDetailString, ',');
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < pairStrings.length; i++) {
			String key;
			String value;
			// this allows the value to have a '=' in it
			int eqIndex = pairStrings[i].indexOf(FIELD_VALUE_SEPARATOR);
			if (eqIndex < 0) {
				key = unformatExtraDetails(pairStrings[i]);
				value = EMPTY_VALUE;
			} else {
				key = unformatExtraDetails(pairStrings[i].substring(0, eqIndex));
				value = unformatExtraDetails(pairStrings[i].substring(eqIndex + 1));
			}
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Adds extra detail to the log message that will be printed to the pageview log after the request is finished being
	 * processed. This uses a {@link ThreadLocal} so it should be called only on the same thread as the request.
	 */
	public static void ignoreRequest() {
		List<FieldValue> details = extraDetails.get();
		details.clear();
		details.add(IGNORE_PAIR);
	}

	@Override
	protected void doStart() throws Exception {
		handler.start();
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		handler.stop();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		long startTime = System.currentTimeMillis();
		// clear our extra details collection
		extraDetails.get().clear();
		boolean error = true;
		try {
			// call the delegate to perform the web request
			handler.handle(target, baseRequest, request, response);
			error = false;
		} catch (IOException e) {
			addExtraDetail("threw", getRootCauseMessage(e));
			throw e;
		} catch (ServletException e) {
			addExtraDetail("threw", getRootCauseMessage(e));
			throw e;
		} catch (Exception e) {
			addExtraDetail("threw", getRootCauseMessage(e));
			throw new IOException("Unable to execute request", e);
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			Response jettyResponse = (Response) response;
			String line = createLogEntry((Request) request, jettyResponse, duration, error);
			if (line != null) {
				if (error) {
					logger.error(line);
				} else {
					logger.info(line);
				}
			}
		}
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void setServer(Server server) {
		super.setServer(server);
		handler.setServer(server);
	}

	@Override
	public void destroy() {
		handler.destroy();
		super.destroy();
	}

	/**
	 * Returns the remote address of the request. This is protected so you can override it to extra the remote address
	 * from some sort of proxy header.
	 */
	protected String getRemoteAddress(Request request) {
		return request.getRemoteAddr();
	}

	/**
	 * Returns the remote address of the request. This is protected so you can override it to extra the remote address
	 * from some sort of proxy header.
	 */
	protected String getRequestDateTimeString(long dateTimeMillis) {
		return Long.toString(dateTimeMillis);
	}

	private String createLogEntry(Request request, Response response, long durationMillis, boolean isError) {
		List<FieldValue> details = extraDetails.get();
		if (details.size() > 0 && details.get(0) == IGNORE_PAIR) {
			return null;
		}

		StringBuilder line = new StringBuilder(256);
		long now = System.currentTimeMillis();
		line.append(durationMillis);
		line.append(SEPARATOR);
		line.append(getRemoteAddress(request));
		line.append(SEPARATOR);
		line.append(now);
		line.append(SEPARATOR);
		appendIfNotEmpty(line, request.getRemoteUser());
		line.append(SEPARATOR);
		line.append(getRequestDateTimeString(now));
		line.append(SEPARATOR);
		appendIfNotEmpty(line, request.getHeader("Host"));
		line.append(SEPARATOR);
		line.append(request.getMethod());
		line.append(SEPARATOR);
		line.append(request.getRequestURI());
		line.append(SEPARATOR);
		appendIfNotEmpty(line, request.getQueryString());
		line.append(SEPARATOR);
		line.append(request.getProtocol());
		line.append(SEPARATOR);
		if (isError) {
			line.append(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} else {
			line.append(((Response) response).getStatus());
		}
		line.append(SEPARATOR);
		appendIfNotEmpty(line, request.getHeader("Referer"));
		line.append(SEPARATOR);
		appendIfNotEmpty(line, request.getHeader("User-Agent"));
		line.append(SEPARATOR);
		if (details.isEmpty()) {
			line.append(BLANK_FIELD);
		} else {
			boolean first = true;
			for (FieldValue detail : details) {
				if (first) {
					first = false;
				} else {
					line.append(',');
				}
				String key = formatExtraDetails(detail.field);
				String value = formatExtraDetails(detail.value);
				line.append(key);
				line.append(FIELD_VALUE_SEPARATOR);
				line.append(value);
			}
		}
		line.append(SEPARATOR);
		line.append(((Response) response).getContentCount());
		return line.toString();
	}

	private void appendIfNotEmpty(StringBuilder sb, String value) {
		if (value == null || value.length() == 0) {
			sb.append(BLANK_FIELD);
		} else {
			// make sure the strings don't have the separator
			value = value.replace(SEPARATOR, ' ');
			sb.append(value);
		}
	}

	private static String formatExtraDetails(String str) {
		// need to do this before the ,
		str = StringUtils.replace(str, "%", "%25");
		str = StringUtils.replace(str, ",", "%2C");
		str = str.replace(SEPARATOR, ' ');
		return str;
	}

	private static String unformatExtraDetails(String str) {
		str = StringUtils.replace(str, "%25", "%");
		str = StringUtils.replace(str, "%2C", ",");
		return str;
	}

	private static String getRootCauseMessage(Throwable th) {
		while (th.getCause() != null) {
			th = th.getCause();
		}
		return th.getMessage();
	}

	/**
	 * Field and value to be added as extra details to a log line.
	 */
	public static class FieldValue {

		String field;
		String value;

		private FieldValue(String field, String value) {
			this.field = field;
			this.value = value;
		}
	}
}
