package com.jeecms.cms.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsGuestbook;
import com.jeecms.cms.entity.assist.CmsGuestbookCtg;
import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.manager.assist.CmsGuestbookCtgMng;
import com.jeecms.cms.manager.assist.CmsGuestbookMng;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsGuestbookApiAct {
	
	/**
	 * 留言列表API
	 * @param siteId 站点ID  非必选
	 * @param ctgId 分类ID  非必选
	 * @param checked 是否审核  非必选
	 * @param recommend 是否推荐  非必选
	 * @param orderBy 排序 0升序  1降序   默认降序
	 * @param first 开始
	 * @param count 数量
	 */
	@RequestMapping(value = "/api/guestbook/list.jspx")
	public void guestbookList(Integer siteId,Integer ctgId,
			Boolean checked,Boolean recommend,Integer orderBy,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if (siteId == null) {
			siteId = CmsUtils.getSiteId(request);
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		boolean orderDesc=true;
		if(orderBy!=null&&orderBy.equals(0)){
			orderDesc=false;
		}
		List<CmsGuestbook> list = cmsGuestbookMng.getList(siteId,
				ctgId, null,recommend, checked,
				orderDesc, orderDesc, first, count);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, list.get(i).convertToJson());
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 留言类别API
	 * @param siteId 站点id
	 */
	@RequestMapping(value = "/api/guestbookctg/list.jspx")
	public void guestbookCtgList(Integer siteId,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if (siteId == null) {
			siteId = CmsUtils.getSiteId(request);
		}
		List<CmsGuestbookCtg> list = cmsGuestbookCtgMng.getList(siteId);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, list.get(i).convertToJson());
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 我的留言API
	 * @param siteId 站点ID   非必选
	 * @param ctgId 分类ID  非必选
	 * @param appId appId 必选
	 * @param sessionKey 会话标识 必选
	 * @param first 开始 非必选 默认0
	 * @param count 数量  非必选 默认10
	 */
	@RequestMapping(value = "/api/guestbook/mylist.jspx")
	public void myGuestbookList(Integer siteId,Integer ctgId,
			Integer first,Integer count, String sessionKey,String appId,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if (siteId == null) {
			siteId = CmsUtils.getSiteId(request);
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
			//验证签名
			errors=ApiValidate.validateApiAccount(request, errors,apiAccount);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			List<CmsGuestbook> list = null ;
			if(user!=null){
				list= cmsGuestbookMng.getList(siteId, ctgId, user.getId(), null,
						null, true, true, first, count);
			}
			JSONArray jsonArray=new JSONArray();
			if(list!=null&&list.size()>0){
				for(int i=0;i<list.size();i++){
					jsonArray.put(i, list.get(i).convertToJson());
				}
			}
			body= jsonArray.toString();
			message=Constants.API_MESSAGE_SUCCESS;
			status=Constants.API_STATUS_SUCCESS;
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 留言详情获取API
	 * @param id 留言ID 必选
	 */
	@RequestMapping(value = "/api/guestbook/get.jspx")
	public void guestbookGet(Integer id,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		CmsGuestbook guestbook = cmsGuestbookMng.findById(id);
		if(guestbook!=null){
			ResponseUtils.renderJson(response, guestbook.convertToJson().toString());
		}else{
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	/**
	 * 留言发布API
	 * @param siteId 站点ID 非必选 默认当前站
	 * @param ctgId  分类ID 必选
	 * @param title 标题 必选
	 * @param content 内容 必选
	 * @param email 邮箱 非必选
	 * @param phone 电话 非必选
	 * @param qq qq号 非必选
	 * @param sessionKey 会话标识 非必选
	 * @param appId appid 必选
	 * @param nonce_str 随机数 必选
	 * @param sign 签名 必选
	 */
	@RequestMapping(value = "/api/guestbook/save.jspx")
	public void guestbookSave(
			Integer siteId, Integer ctgId, String title,
			String content, String email, String phone, String qq,
			String sessionKey,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,ctgId,title,content);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			CmsGuestbookCtg ctg=cmsGuestbookCtgMng.findById(ctgId);
			if(ctg!=null){
				String aesKey=apiAccount.getAesKey();
				user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
				//签名数据不可重复利用
				ApiRecord record=apiRecordMng.findBySign(sign, appId);
				if(record!=null){
					message=Constants.API_MESSAGE_REQUEST_REPEAT;
				}else{
					String ip = RequestUtils.getIpAddr(request);
					if(siteId==null){
						siteId=CmsUtils.getSiteId(request);
					}
					CmsGuestbook guestbook=cmsGuestbookMng.save(user, siteId, 
							ctgId, ip, title, content, email,phone, qq);
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/guestbook/save.jspx",sign);
					body="{\"id\":"+"\""+guestbook.getId()+"\"}";
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}
			}else{
				message="\"guestbook ctg not found\"";
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}

	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
	@Autowired
	protected CmsGuestbookMng cmsGuestbookMng;
	@Autowired
	private CmsGuestbookCtgMng cmsGuestbookCtgMng;
}

