package com.jeecms.cms.entity.main;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.cms.entity.main.base.BaseContentBuy;
import com.jeecms.common.util.DateUtils;



public class ContentBuy extends BaseContentBuy {
	private static final long serialVersionUID = 1L;
	
	public static final Integer PAY_METHOD_WECHAT=1;
	public static final Integer PAY_METHOD_ALIPAY=2;
	
	//订单号错误
	public static final Integer PRE_PAY_STATUS_ORDER_NUM_ERROR=2;
	//订单成功
	public static final Integer PRE_PAY_STATUS_SUCCESS=1;
	//订单金额不足以购买内容
	public static final Integer PRE_PAY_STATUS_ORDER_AMOUNT_NOT_ENOUGH=3;
	
	public boolean getUserHasPaid(){
		if(StringUtils.isNotBlank(getOrderNumWeiXin())
				||StringUtils.isNotBlank(getOrderNumAliPay())){
			return true;
		}else{
			return false;
		}
	}
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("chargeAmount", getChargeAmount());
		json.put("buyTime", DateUtils.parseDateToTimeStr(getBuyTime()));
		json.put("orderNumber", getOrderNumber());
		json.put("chargeReward", getChargeReward());
		json.put("contentId", getContent().getId());
		json.put("contentTitle", getContent().getTitle());
		if(getBuyUser()!=null){
			json.put("buyUser", getBuyUser().getUsername());
		}else{
			json.put("buyUser", "");
		}
		json.put("authorUser", getAuthorUser().getUsername());
		return json;
	}
	
	public int getPrePayStatus() {
		return prePayStatus;
	}

	public void setPrePayStatus(int prePayStatus) {
		this.prePayStatus = prePayStatus;
	}

	private int prePayStatus;
	
	
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public ContentBuy () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ContentBuy (java.lang.Long id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public ContentBuy (
		java.lang.Long id,
		com.jeecms.cms.entity.main.Content content,
		com.jeecms.core.entity.CmsUser buyUser,
		com.jeecms.core.entity.CmsUser authorUser,
		java.lang.Double chargeAmount,
		java.lang.Double authorAmount,
		java.lang.Double platAmount,
		java.util.Date buyTime,
		boolean hasPaidAuthor) {

		super (
			id,
			content,
			buyUser,
			authorUser,
			chargeAmount,
			authorAmount,
			platAmount,
			buyTime,
			hasPaidAuthor);
	}

/*[CONSTRUCTOR MARKER END]*/


}