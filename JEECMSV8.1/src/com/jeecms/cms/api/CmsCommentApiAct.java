package com.jeecms.cms.api;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsComment;
import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.entity.main.ChannelExt;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.manager.assist.CmsCommentMng;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsCommentApiAct {
	
	/**
	 * 评论列表
	 * @param siteId 站点ID 非必选 
	 * @param contentId 内容ID 非必选 
	 * @param parentId 父评论ID 非必选 
	 * @param greaterThen 支持数大于 非必选 
	 * @param checked 是否审核 非必选 
	 * @param recommend 是否推荐 非必选 
	 * @param orderBy 是否推荐 0升序  1降序 非必选 默认1
	 * @param first 开始
	 * @param count 数量
	 */
	@RequestMapping(value = "/api/comment/list.jspx")
	public void commentList(Integer siteId,Integer contentId,Integer parentId,
			Integer greaterThen,Boolean checked,Boolean recommend,Integer orderBy,
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
		List<CmsComment> list = cmsCommentMng.getListForTag(siteId,
				contentId,parentId, greaterThen,
				checked, recommend, orderDesc,first,count);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, list.get(i).convertToJson());
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 我的评论
	 * @param siteId 站点ID 非必须
	 * @param appId appId 必选
	 * @param sessionKey 会话标识 必选
	 * @param first 开始 
	 * @param count 数量
	 */
	@RequestMapping(value = "/api/comment/mylist.jspx")
	public void myCommentList(Integer siteId,
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
			List<CmsComment> list = null ;
			if(user!=null){
				list= cmsCommentMng.getListForMember(siteId, null,
						user.getId(), null, null, null, null, true,first,
						count);
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
	 * 获取单个评论
	 * @param id 评论ID
	 */
	@RequestMapping(value = "/api/comment/get.jspx")
	public void commentGet(Integer id,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		CmsComment comment = cmsCommentMng.findById(id);
		if(comment!=null){
			ResponseUtils.renderJson(response, comment.convertToJson().toString());
		}else{
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	/**
	 * 发布评论
	 * @param contentId  内容ID  必选
	 * @param parentId   父评论ID 非必选
	 * @param score      评分   非必须
	 * @param text       评论内容 必选
	 * @param appId      appid  必选
	 * @param sessionKey 用户会话  必选
	 * @param nonce_str  随机字符串  必选
	 * @param sign		  签名  必选
	 */
	@RequestMapping(value = "/api/comment/save.jspx")
	public void commentSave(
			Integer contentId, 
			Integer parentId,Integer score,String text,
			String sessionKey,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				sessionKey,nonce_str,sign,contentId,text);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			Content content=contentMng.findById(contentId);
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			if(content!=null){
				if(content.getChannel().getCommentControl() == ChannelExt.COMMENT_OFF){
					// 评论关闭
					message="\"comment off\"";
				}else if ((content.getChannel().getCommentControl() == ChannelExt.COMMENT_LOGIN|content.getChannel().getCommentControl() == ChannelExt.COMMENT_LOGIN_MANY)
						&& user == null){
					// 需要登录才能评论
					message="\"comment need login\"";
				}else if(content.getChannel().getCommentControl() == ChannelExt.COMMENT_LOGIN&&user!=null){
					if (hasCommented(user, content)) {
						// 已经评论过，不能重复评论
						message="\"has commented\"";
					}
				}else{
					boolean checked = false;
					Integer userId = null;
					if (user != null) {
						checked = !user.getGroup().getNeedCheck();
						userId = user.getId();
					}
					//签名数据不可重复利用
					ApiRecord record=apiRecordMng.findBySign(sign, appId);
					if(record!=null){
						message=Constants.API_MESSAGE_REQUEST_REPEAT;
					}else{
						CmsComment comment=cmsCommentMng.comment(score,text, RequestUtils.getIpAddr(request),
								contentId, content.getSiteId(), 
								userId, checked, false,parentId);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/comment/save.jspx",sign);
						body="{\"id\":"+"\""+comment.getId()+"\"}";
						message=Constants.API_MESSAGE_SUCCESS;
						status=Constants.API_STATUS_SUCCESS;
					}
				}
			}else{
				message=Constants.API_MESSAGE_CONTENT_NOT_FOUND;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private boolean hasCommented(CmsUser user, Content content) {
		if (content.hasCommentUser(user)) {
			return true;
		} else {
			return false;
		}
	}

	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
	@Autowired
	protected CmsCommentMng cmsCommentMng;
	@Autowired
	private ContentMng contentMng;
}

