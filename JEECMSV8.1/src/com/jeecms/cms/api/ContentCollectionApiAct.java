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
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class ContentCollectionApiAct{
	
	/**
	 * 我的收藏
	 * @param siteId 站点id 非必选 默认当前站
	 * @param format 模式 非必选 默认1 内容信息简化模式  0全部信息
	 * @param appId   appid 必选
	 * @param sessionKey 会话标识 必选
	 * @param first 开始 非必选 默认0
	 * @param count 数量 非必选 默认10 
	 */
	@RequestMapping(value = "/api/content/mycollect.jspx")
	public void mycollectList(
			Integer siteId,Integer format,Integer https,
			String appId,String sessionKey,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		CmsUser user = null;
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		if(siteId==null){
			siteId=CmsUtils.getSiteId(request);
		}
		if(format==null){
			format=Content.CONTENT_INFO_SIMPLE;
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		List<Content>contents=null;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,sessionKey);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证appid
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
			contents=contentMng.getListForCollection(
					siteId, user.getId(), first, count);
			JSONArray jsonArray=new JSONArray();
			if(contents!=null&&contents.size()>0){
				for(int i=0;i<contents.size();i++){
					jsonArray.put(i, contents.get(i).convertToJson(format,https,true));
				}
			}
			body=jsonArray.toString();
			message=Constants.API_MESSAGE_SUCCESS;
			status=Constants.API_STATUS_SUCCESS;
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 收藏API
	 * @param id   内容ID 必选
	 * @param operate 操作 非必选  1收藏 0 取消收藏   默认1 
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/collect.jspx")
	public void contentCollect(
			Integer id,Integer operate,
			String appId,String sessionKey,
			String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if(operate==null){
			operate=1;
		}
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				sessionKey,nonce_str,sign,id);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
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
			Content content=contentMng.findById(id);
			if(content!=null){
				ApiRecord record=apiRecordMng.findBySign(sign, appId);
				if(record!=null){
					message=Constants.API_MESSAGE_REQUEST_REPEAT;
				}else{
					userMng.updateUserConllection(user,id,operate);
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/content/collect.jspx",sign);
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}
			}else{
				message=Constants.API_MESSAGE_CONTENT_NOT_FOUND;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}

	@Autowired
	private ContentMng contentMng;
	@Autowired
	private CmsUserMng userMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
	@Autowired
	private ApiRecordMng apiRecordMng;
}

