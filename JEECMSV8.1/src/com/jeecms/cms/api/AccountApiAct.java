package com.jeecms.cms.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsAccountDraw;
import com.jeecms.cms.entity.assist.CmsConfigContentCharge;
import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.manager.assist.CmsAccountDrawMng;
import com.jeecms.cms.manager.assist.CmsConfigContentChargeMng;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.common.util.ArrayUtils;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.WebErrors;

@Controller
public class AccountApiAct {
	
	/**
	 * 提现申请
	 * @param appId      appid 必选
	 * @param sessionKey 用户会话  必选
	 * @param first
	 * @param count
	 */
	@RequestMapping(value = "/api/draw/list.jspx")
	public void myDrawApplyList(
			String appId,String sessionKey,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		getMyInfoList(appId, sessionKey, 
				first, count, request, response);
	}
	
	/**
	 * 删除提现申请
	 * @param ids   申请的id ,间隔 比如 1,2   必选
	 * @param appId      appid   必选
	 * @param sessionKey 用户会话  必选
	 * @param nonce_str  随机字符串   必选
	 * @param sign		  签名 必选 
	 */
	@RequestMapping(value = "/api/draw/delete.jspx")
	public void deleteApply(String ids,
			String appId,String sessionKey,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		applyDelete(ids, appId, sessionKey,nonce_str, sign,request, response);
	}
	
	/**
	 * 申请提现
	 * @param drawAmout  申请金额  必选
	 * @param appId      appid  必选
	 * @param sessionKey 用户会话  必选
	 * @param nonce_str  随机字符串  必选
	 * @param sign		  签名  必选
	 */
	@RequestMapping(value = "/api/draw/apply.jspx")
	public void drawApply(Double drawAmout,
			String appId,String sessionKey,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		CmsUser user = null;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				sessionKey,nonce_str,sign,drawAmout);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			//会话用户
			if(user!=null){
				//验证用户业务
				if(user.getUserAccount()==null){
					errors.addErrorString("user account not found");
				}else{
					CmsConfigContentCharge config=configContentChargeMng.getDefault();
					if(drawAmout>user.getUserAccount().getContentNoPayAmount()){
						errors.addErrorString("balance not Enough");
					}
					if(drawAmout<config.getMinDrawAmount()){
						errors.addErrorString("draw less min amount "+config.getMinDrawAmount());
					}
				}
				if(errors.hasErrors()){
					message="\""+errors.getErrors().get(0)+"\"";
				}else{
					//签名数据不可重复利用
					ApiRecord record=apiRecordMng.findBySign(sign, appId);
					if(record!=null){
						message=Constants.API_MESSAGE_REQUEST_REPEAT;
					}else{
						//微信openid作为默认提现账户
						accountDrawMng.draw(user, drawAmout, user.getUserAccount().getAccountWeixinOpenId());
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/draw/apply.jspx",sign);
						message=Constants.API_STATUS_SUCCESS;
						status=Constants.API_STATUS_SUCCESS;
					}
				}
			}else{
				message=Constants.API_MESSAGE_SESSION_ERROR;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}

	/**
	 * 获取用户账户信息
	 * @param appId      appid  必选
	 * @param sessionKey 用户会话    必选
	 */
	@RequestMapping(value = "/api/account/get.jspx")
	public void getAccountInfo(String appId,String sessionKey,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		CmsUser user = null;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				sessionKey);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证appId
			errors=ApiValidate.validateApiAccount(request, errors,apiAccount);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			//会话用户
			if(user!=null){
				if(user.getUserAccount()!=null){
					body=user.getUserAccount().convertToJson().toString();
					message=Constants.API_STATUS_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}else{
					message="\"user account not found\"";
				}
			}else{
				message=Constants.API_MESSAGE_SESSION_ERROR;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private void applyDelete(
			String  ids,String appId,String sessionKey
			,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response){
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		CmsUser user = null;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, ids,appId,
				sessionKey,nonce_str,sign);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			//会话用户
			if(user!=null){
				//签名数据不可重复利用
				ApiRecord record=apiRecordMng.findBySign(sign, appId);
				if(record!=null){
					message=Constants.API_MESSAGE_REQUEST_REPEAT;
				}else{
					Integer[] intIds=ArrayUtils.parseStringToArray(ids);
					accountDrawMng.deleteByIds(intIds);
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/draw/delete.jspx",sign);
					message=Constants.API_STATUS_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}
			}else{
				message=Constants.API_MESSAGE_SESSION_ERROR;
			}
		}		
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private void getMyInfoList(String appId,String sessionKey,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException{
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
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
			//验证签名
			errors=ApiValidate.validateApiAccount(request, errors,apiAccount);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			if(user!=null){
				JSONArray jsonArray=new JSONArray();
				List<CmsAccountDraw>list = null;
				list=accountDrawMng.getList(user.getId(),null,null,null,
						first,count);
				if(list!=null&&list.size()>0){
					for(int i=0;i<list.size();i++){
						jsonArray.put(i, list.get(i).convertToJson());
					}
				}
				body=jsonArray.toString();
				status=Constants.API_STATUS_SUCCESS;
				message=Constants.API_MESSAGE_SUCCESS;
			}else{
				message=Constants.API_MESSAGE_USER_NOT_LOGIN;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private CmsAccountDrawMng accountDrawMng;
	@Autowired
	private CmsConfigContentChargeMng configContentChargeMng;
}

