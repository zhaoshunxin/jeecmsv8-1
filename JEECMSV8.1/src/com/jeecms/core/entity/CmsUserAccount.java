package com.jeecms.core.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.common.util.DateUtils;
import com.jeecms.core.entity.base.BaseCmsUserAccount;



public class CmsUserAccount extends BaseCmsUserAccount {
	private static final long serialVersionUID = 1L;
	
	public static final byte DRAW_WEIXIN=0;
	
	public static final byte DRAW_ALIPY=1;
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("contentTotalAmount", getContentTotalAmount());
		json.put("contentNoPayAmount", getContentNoPayAmount());
		json.put("contentYearAmount", getContentYearAmount());
		json.put("contentMonthAmount", getContentMonthAmount());
		json.put("contentDayAmount", getContentDayAmount());
		json.put("drawCount", getDrawCount());
		json.put("contentBuyCount", getContentBuyCount());
		json.put("drawAccount", getDrawAccount());
		json.put("user", getUser().getUsername());
		if(getLastDrawTime()!=null){
			json.put("lastDrawTime", DateUtils.parseDateToTimeStr(getLastDrawTime()));
		}else{
			json.put("lastDrawTime", "");
		}
		if(getLastBuyTime()!=null){
			json.put("lastBuyTime", DateUtils.parseDateToTimeStr(getLastBuyTime()));
		}else{
			json.put("lastBuyTime", "");
		}
		return json;
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsUserAccount () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsUserAccount (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsUserAccount (
		java.lang.Integer id,
		java.lang.Double contentYearAmount,
		java.lang.Double contentMonthAmount,
		java.lang.Double contentDayAmount,
		java.util.Date lastPaidTime) {

		super (
			id,
			contentYearAmount,
			contentMonthAmount,
			contentDayAmount,
			lastPaidTime);
	}

/*[CONSTRUCTOR MARKER END]*/


}