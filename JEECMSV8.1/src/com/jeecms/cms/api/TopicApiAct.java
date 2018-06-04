package com.jeecms.cms.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.entity.main.CmsTopic;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsTopicMng;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class TopicApiAct {
	
	/**
	 * 专题列表API
	 * @param channelId 栏目ID
	 * @param recommend 是否推荐 true推荐 false全部  非必选 默认false
	 * @param first 第几条开始 默认0
	 * @param count 数量 默认10
	 */
	@RequestMapping(value = "/api/topic/list.jspx")
	public void topicList(Integer https,Integer channelId,
			Boolean recommend,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		if(recommend==null){
			recommend=false;
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		CmsSite site=CmsUtils.getSite(request);
		List<CmsTopic> list;
		list =topicMng.getListForTag(channelId, recommend,first,count);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, convertToJson(site,list.get(i),https));
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 专题信息获取
	 * @param id 专题id 必选
	 */
	@RequestMapping(value = "/api/topic/get.jspx")
	public void topicGet(Integer https,
			Integer id,HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		CmsTopic topic = null;
		CmsSite site=CmsUtils.getSite(request);
		if (id != null) {
			topic = topicMng.findById(id);
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		if (topic != null) {
			JSONObject json=convertToJson(site,topic,https);
			ResponseUtils.renderJson(response, json.toString());
		} else {
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	/**
	 * 专题保存接口
	 * @param channelId 栏目ID 非必选
	 * @param name  名称  必选
	 * @param shortName  路径 非必选
	 * @param keywords  关键词 非必选
	 * @param desc  关键词 非必选
	 * @param titleImg  标题图 非必选
	 * @param contentImg  内容图 非必选
	 * @param priority  排序 非必选 默认10 
	 * @param recommend  推荐 非必选 默认false
	 * @param appId appid 必选
	 * @param nonce_str 随机数 必选
	 * @param sign 签名 必选
	 */
	@RequestMapping(value = "/api/topic/save.jspx")
	public void topicSave(
			Integer channelId,
			String name,String shortName,String keywords,String desc,
			String titleImg,String contentImg,Integer priority,
			Boolean recommend,String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response)
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if(priority==null){
			priority=10;
		}
		if(recommend==null){
			recommend=false;
		}
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,sign,nonce_str,name);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				CmsTopic topic=new CmsTopic();
				if(channelId!=null){
					topic.setChannel(channelMng.findById(channelId));
				}
				topic.setName(name);
				topic.setContentImg(contentImg);
				topic.setDescription(desc);
				topic.setKeywords(keywords);
				topic.setPriority(priority);
				topic.setRecommend(recommend);
				topic.setShortName(shortName);
				topic.setTitleImg(titleImg);
				topic = topicMng.save(topic, channelId,null);
				apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
						appId, "/api/topic/save.jspx",sign);
				body="{\"id\":"+"\""+topic.getId()+"\"}";
				message=Constants.API_MESSAGE_SUCCESS;
				status=Constants.API_STATUS_SUCCESS;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 专题修改接口
	 * @param id ID 必选
	 * @param channelId 栏目ID 非必选
	 * @param name  名称  非必选
	 * @param shortName  路径 非必选
	 * @param keywords  关键词 非必选
	 * @param desc  关键词 非必选
	 * @param titleImg  标题图 非必选
	 * @param contentImg  内容图 非必选
	 * @param priority  排序 非必选
	 * @param recommend  推荐 非必选
	 * @param appId appid 必选
	 * @param nonce_str 随机数 必选
	 * @param sign 签名 必选
	 */
	@RequestMapping(value = "/api/topic/update.jspx")
	public void topicUpdate(
			Integer id,Integer channelId,
			String name,String shortName,String keywords,String desc,
			String titleImg,String contentImg,Integer priority,
			Boolean recommend,String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,sign,nonce_str,id);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				CmsTopic topic=topicMng.findById(id);
				if(topic!=null){
					if(channelId!=null){
						topic.setChannel(channelMng.findById(channelId));
					}
					if(StringUtils.isNotBlank(name)){
						topic.setName(name);
					}
					if(StringUtils.isNotBlank(contentImg)){
						topic.setContentImg(contentImg);
					}
					if(StringUtils.isNotBlank(desc)){
						topic.setDescription(desc);
					}
					if(StringUtils.isNotBlank(keywords)){
						topic.setKeywords(keywords);
					}
					if(priority!=null){
						topic.setPriority(priority);
					}
					if(recommend!=null){
						topic.setRecommend(recommend);
					}
					if(StringUtils.isNotBlank(shortName)){
						topic.setShortName(shortName);
					}
					if(StringUtils.isNotBlank(titleImg)){
						topic.setTitleImg(titleImg);
					}
					topic = topicMng.update(topic, channelId,null);
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/topic/update.jspx",sign);
					body="{\"id\":"+"\""+topic.getId()+"\"}";
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}else{
					message=Constants.API_MESSAGE_PARAM_ERROR;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private JSONObject convertToJson(CmsSite site,CmsTopic topic
			,Integer https) throws JSONException{
		JSONObject json=new JSONObject();
		Ftp uploadFtp=site.getUploadFtp();
		boolean uploadToFtp=false;
		if(uploadFtp!=null){
			uploadToFtp=true;
		}
		json.put("id", topic.getId());
		json.put("name", topic.getName());
		json.put("shortName", topic.getShortName());
		json.put("description", topic.getDescription());
		
		json.put("priority", topic.getPriority());
		json.put("recommend", topic.getRecommend());
		String urlPrefix="";
		if(https==com.jeecms.cms.api.Constants.URL_HTTP){
			urlPrefix=site.getUrlPrefixWithNoDefaultPort();
		}else{
			urlPrefix=site.getSafeUrlPrefix();
		}
		if(!uploadToFtp){
			if(StringUtils.isNotBlank(topic.getTitleImg())){
				json.put("titleImg", urlPrefix+topic.getTitleImg());
				json.put("contentImg", urlPrefix+topic.getContentImg());
			}else{
				json.put("titleImg", "");
				json.put("contentImg", "");
			}
		}else{
			if(StringUtils.isNotBlank(topic.getTitleImg())){
				json.put("titleImg", topic.getTitleImg());
				json.put("contentImg", topic.getContentImg());
			}else{
				json.put("titleImg", "");
				json.put("contentImg", "");
			}
		}
		return json;
	}
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private CmsTopicMng topicMng;
}

