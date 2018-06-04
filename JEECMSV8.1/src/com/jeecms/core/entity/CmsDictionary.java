package com.jeecms.core.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.core.entity.base.BaseCmsDictionary;



public class CmsDictionary extends BaseCmsDictionary {
	private static final long serialVersionUID = 1L;
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("value", getValue());
		json.put("name", getName());
		return json;
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsDictionary () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsDictionary (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsDictionary (
		java.lang.Integer id,
		java.lang.String name,
		java.lang.String value,
		java.lang.String type) {

		super (
			id,
			name,
			value,
			type);
	}

/*[CONSTRUCTOR MARKER END]*/


}