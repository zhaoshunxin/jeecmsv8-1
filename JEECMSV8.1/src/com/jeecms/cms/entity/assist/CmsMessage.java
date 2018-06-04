package com.jeecms.cms.entity.assist;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.cms.entity.assist.base.BaseCmsMessage;
import com.jeecms.common.util.DateUtils;
import com.jeecms.common.util.StrUtils;



public class CmsMessage extends BaseCmsMessage {
	private static final long serialVersionUID = 1L;
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("msgTitle", getMsgTitle());
		json.put("msgContent", getMsgContent());
		json.put("sendTime", DateUtils.parseDateToTimeStr(getSendTime()));
		json.put("msgStatus", getMsgStatus());
		json.put("msgBox", getMsgBox());
		if(getMsgReceiverUser()!=null){
			json.put("msgReceiverUser", getMsgReceiverUser().getUsername());
		}
		if(getMsgSendUser()!=null){
			json.put("msgSendUser", getMsgSendUser().getUsername());
		}
		return json;
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsMessage () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsMessage (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsMessage (
		java.lang.Integer id,
		com.jeecms.core.entity.CmsUser msgReceiverUser,
		com.jeecms.core.entity.CmsUser msgSendUser,
		com.jeecms.core.entity.CmsSite site,
		java.lang.String msgTitle,
		java.lang.Boolean msgStatus,
		java.lang.Integer msgBox) {

		super (
			id,
			msgReceiverUser,
			msgSendUser,
			site,
			msgTitle,
			msgStatus,
			msgBox);
	}
	public String getTitleHtml() {
		return StrUtils.txt2htm(getMsgTitle());
	}
	public String getContentHtml() {
		return StrUtils.txt2htm(getMsgContent());
	}


/*[CONSTRUCTOR MARKER END]*/


}