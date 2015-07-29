package com.j256.simplewebframework.logger;

import org.eclipse.jetty.server.Request;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.j256.simplewebframework.handler.LoggingHandler;
import com.j256.simplewebframework.util.WebEc2Utils;

/**
 * Local extension of the logging handler which handles EC2 addresses and pretty prints the date/time string.
 */
public class LocalLoggingHandler extends LoggingHandler {

	private static final DateTimeFormatter requestLogFormat = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z");

	@Override
	protected String getRemoteAddress(Request request) {
		return WebEc2Utils.getRemoteAddress(request);
	}

	@Override
	protected String getRequestDateTimeString(long dateTimeMillis) {
		return requestLogFormat.print(dateTimeMillis);
	}
}
