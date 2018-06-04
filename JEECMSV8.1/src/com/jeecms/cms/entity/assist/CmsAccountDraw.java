package com.jeecms.cms.entity.assist;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.cms.entity.assist.base.BaseCmsAccountDraw;
import com.jeecms.common.util.DateUtils;



public class CmsAccountDraw extends BaseCmsAccountDraw {
	private static final long serialVersionUID = 1L;
	
	public static final Short CHECKING = 0;
	public static final Short CHECKED_SUCC = 1;
	public static final Short CHECKED_FAIL = 2;
	public static final Short DRAW_SUCC = 3;
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("applyAmount", getApplyAmount());
		json.put("applyStatus", getApplyStatus());
		if(getApplyTime()!=null){
			json.put("applyTime", DateUtils.parseDateToTimeStr(getApplyTime()));
		}else{
			json.put("applyTime", "");
		}
		json.put("drawUser", getDrawUser().getUsername());
		return json;
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsAccountDraw () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsAccountDraw (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsAccountDraw (
		java.lang.Integer id,
		com.jeecms.core.entity.CmsUser drawUser,
		java.lang.String applyAccount,
		java.lang.Double applyAmount,
		java.lang.Short applyStatus,
		java.util.Date applyTime) {

		super (
			id,
			drawUser,
			applyAccount,
			applyAmount,
			applyStatus,
			applyTime);
	}

/*[CONSTRUCTOR MARKER END]*/


}