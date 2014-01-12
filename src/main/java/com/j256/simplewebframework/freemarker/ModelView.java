package com.j256.simplewebframework.freemarker;

import java.util.HashMap;
import java.util.Map;

/**
 * Model and view to be returned by web-service.
 * 
 * @author graywatson
 */
public class ModelView {

	private final Map<String, Object> model;
	private final String view;

	public ModelView(String view) {
		this.model = new HashMap<String, Object>();
		this.view = view;
	}

	public ModelView(Map<String, Object> model, String view) {
		this.model = model;
		this.view = view;
	}

	public Map<String, Object> getModel() {
		return model;
	}
	
	public String getView() {
		return view;
	}
}
