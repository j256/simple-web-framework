package com.j256.simplewebframework.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxResource;
import com.j256.simplewebframework.displayer.FileResultDisplayer.FileInfo;
import com.j256.simplewebframework.displayer.ResultDisplayer;
import com.j256.simplewebframework.util.ResponseUtils;
import com.j256.simplewebframework.util.ResponseUtils.HttpErrorCode;

/**
 * A special ResourceHandler that finds files locally and returns them as resources.
 * 
 * @author gwatson
 */
@JmxResource(domainName = "j256.simpleweb", folderNames = { "web" }, description = "Resource handler for disk files")
public class LocalResourceHandler extends AbstractHandler {

	private Map<String, ResultDisplayer> fileExtensionDisplayers = new HashMap<String, ResultDisplayer>();
	private ResultDisplayer defaultDisplayer;

	@SuppressWarnings("unused")
	@JmxAttributeField(description = "Number of head requests")
	private int headRequestsCount = 0;
	@SuppressWarnings("unused")
	@JmxAttributeField(description = "Number of not-modified responses")
	private int notModifiedResponsesCount = 0;
	@SuppressWarnings("unused")
	@JmxAttributeField(description = "Number of files requested")
	private int invalidRequestsCount = 0;
	@SuppressWarnings("unused")
	@JmxAttributeField(description = "Number of invalid paths")
	private int invalidPathsCount = 0;
	@SuppressWarnings("unused")
	@JmxAttributeField(description = "Number of unknown paths")
	private int unknownPathsCount = 0;
	private FileLocator fileLocator;

	@Override
	public void handle(String target, Request request, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws IOException {

		// this is a resource end-point
		if (request.isHandled()) {
			return;
		}
		request.setHandled(true);

		if (!HttpMethods.GET.equals(servletRequest.getMethod()) && !HttpMethods.HEAD.equals(servletRequest.getMethod())) {
			invalidRequestsCount++;
			ResponseUtils.sendError(servletResponse, HttpErrorCode.BAD_REQUEST,
					"invalid request for local resource, only GET and HEAD is accepted");
			return;
		}

		String path = servletRequest.getPathInfo();
		if (path == null) {
			invalidPathsCount++;
			ResponseUtils.sendError(servletResponse, HttpErrorCode.NOT_FOUND, "null path");
			return;
		}
		if (!path.startsWith("/")) {
			invalidPathsCount++;
			ResponseUtils.sendError(servletResponse, HttpErrorCode.BAD_REQUEST,
					"Invalid request, path doesn't start with /");
			return;
		}
		path = path.substring(1);
		handlePath(path, null, request, servletRequest, servletResponse);
	}

	/**
	 * Handle the request for a certain path.
	 */
	private void handlePath(String path, Map<String, Object> model, Request request, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws IOException {

		FileInfo fileInfo = fileLocator.findFile(path);
		if (fileInfo == null) {
			invalidPathsCount++;
			ResponseUtils.sendError(servletResponse, HttpErrorCode.NOT_FOUND);
			return;
		}

		String extension = findRequestExtension(fileInfo);
		if (extension == null) {
			invalidPathsCount++;
			ResponseUtils.sendError(servletResponse, HttpErrorCode.NOT_FOUND, "no file extension");
			return;
		}

		ResultDisplayer displayer = fileExtensionDisplayers.get(extension);
		if (displayer == null) {
			displayer = defaultDisplayer;
			if (displayer == null) {
				invalidPathsCount++;
				ResponseUtils.sendError(servletResponse, HttpErrorCode.NOT_FOUND, "unknown file type");
				return;
			}
		}

		displayer.renderResult(request, servletRequest, servletResponse, fileInfo);
	}

	private String findRequestExtension(FileInfo fileInfo) {
		String extension = findExtension(fileInfo.getFile().getPath());
		if (extension == null) {
			return findExtension(fileInfo.getPath());
		} else {
			return extension;
		}
	}

	private String findExtension(String path) {
		int dotIndex = path.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == path.length() - 1) {
			return null;
		} else {
			return path.substring(dotIndex + 1);
		}
	}

	public void setFileExtensionDisplayers(Map<String, ResultDisplayer> fileExtensionDisplayers) {
		this.fileExtensionDisplayers = fileExtensionDisplayers;
	}

	public void setDefaultDisplayer(ResultDisplayer defaultDisplayer) {
		this.defaultDisplayer = defaultDisplayer;
	}

	public void setFileLocator(FileLocator fileLocator) {
		this.fileLocator = fileLocator;
	}
}
