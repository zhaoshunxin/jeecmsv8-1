package com.jeecms.cms.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ContentBuy;
import com.jeecms.cms.entity.main.ContentCharge;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.cms.manager.main.ContentBuyMng;
import com.jeecms.cms.manager.main.ContentChargeMng;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.WebErrors;

@Controller
public class ContentOrderApiAct {
	private static final int OPERATOR_BUY=1;
	private static final int OPERATOR_ORDER=2;
	private static final int OPERATOR_CHARGELIST=3;
	
	/**
	 * 内容打赏购买记录
	 * @param contentId  内容ID 必选 
	 * @param type 1购买记录  2打赏记录 非必选 默认2
	 * @param first 非必选 默认0
	 * @param count 非必选 默认10
	 */
	@RequestMapping(value = "/api/order/getByContent.jspx")
	public void getOrderListByContent(
			Integer contentId,Short type,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if(type==null){
			type=ContentCharge.MODEL_REWARD;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		List<ContentBuy>list;
		if(contentId!=null){
			list=contentBuyMng.getListByContent(contentId,
					type, first, count);
			JSONArray jsonArray=new JSONArray();
			if(list!=null&&list.size()>0){
				for(int i=0;i<list.size();i++){
					jsonArray.put(i, list.get(i).convertToJson());
				}
			}
			body=jsonArray.toString();
			message=Constants.API_MESSAGE_SUCCESS;
			status=Constants.API_STATUS_SUCCESS;
		}else{
			message=Constants.API_MESSAGE_PARAM_ERROR;
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 我消费的记录和我的内容被打赏记录
	 * @param orderNum 订单号 非必选
	 * @param orderType 类型  1我消费的记录 2我的内容被打赏记录 默认1
	 * @param appId      appid  必选
	 * @param sessionKey 用户会话  必选
	 * @param first 非必选 默认0
	 * @param count 非必选 默认10
	 */
	@RequestMapping(value = "/api/order/myorders.jspx")
	public void myOrderList(String orderNum,Integer orderType,
			String appId,String sessionKey,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		getMyInfoList(orderType, orderNum, appId, sessionKey, 
				first, count, request, response);
	}
	
	/**
	 * 我的内容收费统计
	 * @param orderNum 订单号 非必选
	 * @param appId      appid  必选
	 * @param sessionKey 用户会话  必选
	 * @param first 非必选 默认0
	 * @param count 非必选 默认10
	 */
	@RequestMapping(value = "/api/order/chargelist.jspx")
	public void chargeList(String orderNum,
			String appId,String sessionKey,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		getMyInfoList(OPERATOR_CHARGELIST, orderNum, appId, sessionKey, 
				first, count, request, response);
	}
	
	private void getMyInfoList(Integer operate,String orderNum,
			String appId,String sessionKey,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException{
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if(operate==null){
			operate=OPERATOR_BUY;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors,appId,
				sessionKey);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证APPID
			errors=ApiValidate.validateApiAccount(request, errors,apiAccount);
			//apiAccount可能获取不到，需要再次判断
			if(!errors.hasErrors()){
				String aesKey=apiAccount.getAesKey();
				user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
				//验证用户
				if(user==null){
					errors.addErrorString(Constants.API_MESSAGE_USER_NOT_LOGIN);
				}
			}
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			JSONArray jsonArray=new JSONArray();
			List<ContentBuy>list = null;
			List<ContentCharge>chargeList=null;
			if(OPERATOR_BUY==operate){
				list=contentBuyMng.getList(orderNum,
						user.getId(), null, null, first, count);
			}else if(OPERATOR_ORDER==operate){
				list=contentBuyMng.getList(orderNum,
						null, user.getId(), null, first, count);
			}else if(OPERATOR_CHARGELIST==operate){
				chargeList=contentChargeMng.getList(null, user.getId(),
						null, null, 1, first, count);
			}
			if(list!=null&&list.size()>0){
				for(int i=0;i<list.size();i++){
					jsonArray.put(i, list.get(i).convertToJson());
				}
			}
			if(chargeList!=null&&chargeList.size()>0){
				for(int i=0;i<chargeList.size();i++){
					jsonArray.put(i, chargeList.get(i).convertToJson());
				}
			}
			body=jsonArray.toString();
			status=Constants.API_STATUS_SUCCESS;
			message=Constants.API_MESSAGE_SUCCESS;
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
	@Autowired
	private ContentBuyMng contentBuyMng;
	@Autowired
	private ContentChargeMng contentChargeMng;
}

