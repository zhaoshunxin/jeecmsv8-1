package com.jeecms.cms.entity.main;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.cms.entity.main.base.BaseContentCharge;
import com.jeecms.common.util.DateUtils;



public class ContentCharge extends BaseContentCharge {
	private static final long serialVersionUID = 1L;
	public static final Short MODEL_FREE=0;
	public static final Short MODEL_CHARGE=1;
	public static final Short MODEL_REWARD=2;
	
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("chargeAmount", getChargeAmount());
		json.put("totalAmount", getTotalAmount());
		json.put("yearAmount", getYearAmount());
		json.put("monthAmount", getMonthAmount());
		json.put("dayAmount", getDayAmount());
		json.put("chargeReward", getChargeReward());
		if(getLastBuyTime()!=null){
			json.put("lastBuyTime", DateUtils.parseDateToTimeStr(getLastBuyTime()));
		}else{
			json.put("lastBuyTime","");
		}
		json.put("contentId", getContent().getId());
		json.put("contentTitle", getContent().getTitle());
		return json;
	}
	
	public void init(){
		if(getChargeAmount()==null){
			setChargeAmount(0d);
		}
		if(getDayAmount()==null){
			setDayAmount(0d);
		}
		if(getMonthAmount()==null){
			setMonthAmount(0d);
		}
		if(getYearAmount()==null){
			setYearAmount(0d);
		}
		if(getTotalAmount()==null){
			setTotalAmount(0d);
		}
		if(getRewardRandomMax()==null){
			setRewardRandomMax(0d);
		}
		if(getRewardRandomMin()==null){
			setRewardRandomMin(0d);
		}
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ContentCharge () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ContentCharge (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public ContentCharge (
		java.lang.Integer id,
		java.lang.Double chargeAmount,
		java.lang.Double totalAmount,
		java.lang.Double yearAmount,
		java.lang.Double monthAmount,
		java.lang.Double dayAmount) {

		super (
			id,
			chargeAmount,
			totalAmount,
			yearAmount,
			monthAmount,
			dayAmount);
	}

/*[CONSTRUCTOR MARKER END]*/


}