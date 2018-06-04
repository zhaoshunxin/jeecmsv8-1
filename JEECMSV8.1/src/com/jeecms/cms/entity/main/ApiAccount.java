package com.jeecms.cms.entity.main;

import com.jeecms.cms.entity.main.base.BaseApiAccount;



public class ApiAccount extends BaseApiAccount {
	private static final long serialVersionUID = 1L;
	

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ApiAccount () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ApiAccount (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public ApiAccount (
		java.lang.Integer id,
		java.lang.String appId,
		java.lang.String appKey,
		java.lang.Boolean disabled) {

		super (
			id,
			appId,
			appKey,
			disabled);
	}

/*[CONSTRUCTOR MARKER END]*/


}