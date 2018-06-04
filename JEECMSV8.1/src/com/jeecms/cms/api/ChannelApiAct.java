package com.jeecms.cms.api;

import java.util.List;
import java.util.Map;

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
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.ChannelExt;
import com.jeecms.cms.entity.main.ChannelTxt;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class ChannelApiAct {
	
	/**
	 * 栏目列表API
	 * @param parentId  父栏目ID
	 * @param siteId    站点ID
	 * @param hasContentOnly  是否有内容
	 * @param first   查询开始下标
	 * @param count	  查询数量
	 */
	@RequestMapping(value = "/api/channel/list.jspx")
	public void channelList(Integer https,Integer parentId,Integer siteId,
			Boolean hasContentOnly,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(hasContentOnly==null){
			hasContentOnly=false;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		List<Channel> list;
		if (parentId != null) {
			list = channelMng.getChildListForTag(parentId, hasContentOnly);
		} else {
			if (siteId == null) {
				siteId = 1;
			}
			list = channelMng.getTopListForTag(siteId, hasContentOnly);
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, convertToJson(list.get(i),https));
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 获取栏目信息
	 * id或者path
	 * path和siteId必须一起使用
	 * @param id 栏目id
	 * @param path  栏目路径
	 * @param siteId  站点id
	 */
	@RequestMapping(value = "/api/channel/get.jspx")
	public void channelGet(Integer https,
			Integer id,String path,Integer siteId,
			HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		Channel channel;
		if(https==null){
			https=Constants.URL_HTTP;
		}
		if (id != null) {
			channel = channelMng.findById(id);
		} else {
			if(siteId==null){
				siteId=1;
			}
			channel = channelMng.findByPathForTag(path, siteId);
		}
		if (channel != null) {
			JSONObject json=convertToJson(channel,https);
			ResponseUtils.renderJson(response, json.toString());
		} else {
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	/**
	 * 栏目保存接口
	 * @param siteId 站点ID 非必选
	 * @param parentId 父栏目ID 非必选
	 * @param name  名称  必选
	 * @param path  路径 必选
	 * @param title 标题 非必选
	 * @param keywords meta关键词  非必选
	 * @param desc 描述 非必选
	 * @param txt  栏目文本内容 非必选
	 * @param priority 排序  非必选
	 * @param display 是否展示 非必选
	 * @param modelId 模型ID 必选  新闻 1
	 * @param workflowId 应用工作流ID 非必选
	 * @param titleImg 标题图 非必选
	 * @param contentImg 内容图 非必选
	 * @param finalStep 终审步骤 非必选
	 * @param afterCheck 审核后 非必选
	 * @param tplChannel PC端模板 非必选
	 * @param tplMobileChannel 移动端模板 非必选
	 * @param appId appid 必选
	 * @param nonce_str 随机数 必选
	 * @param sign 签名 必选
	 */
	@RequestMapping(value = "/api/channel/save.jspx")
	public void channelSave(
			Integer siteId,Integer parentId,
			String name,String path,String title,String keywords,
			String desc,String txt,Integer priority,
			Boolean display,Integer modelId,Integer workflowId,
			String titleImg,String contentImg,Byte finalStep,
			Byte afterCheck,String tplChannel,String tplMobileChannel,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,sign,nonce_str
				,name,path,modelId);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证appId
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			CmsSite site=siteMng.findById(siteId);
			if(site==null){
				site=CmsUtils.getSite(request);
			}
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				Channel channel=new Channel();
				ChannelExt ext=new ChannelExt();
				ChannelTxt channelTxt=new ChannelTxt();
				channel.setChannelExt(ext);
				channel.setPath(path);
				ext.setName(name);
				ext.setTitle(title);
				ext.setKeywords(keywords);
				ext.setDescription(desc);
				ext.setTitleImg(titleImg);
				ext.setContentImg(contentImg);
				ext.setFinalStep(finalStep);
				ext.setAfterCheck(afterCheck);
				ext.setChannel(channel);
				channelTxt.setChannel(channel);
				channelTxt.setTxt(txt);
				if (priority == null) {
					channel.setPriority(10);
				}
				if (display == null) {
					channel.setDisplay(true);
				}
				// 加上模板前缀
				String tplPath = site.getTplPath();
				if (!StringUtils.isBlank(tplChannel)) {
					ext.setTplChannel(tplPath + tplChannel);
				}
				if (!StringUtils.isBlank(tplMobileChannel)) {
					ext.setTplMobileChannel(tplPath + tplMobileChannel);
				}
				channel.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				channel=channelMng.save(channel, ext, channelTxt, null, null,
						null, siteId, parentId, modelId,workflowId,null,null,null);
				apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
						appId, "/api/channel/save.jspx",sign);
				body="{\"id\":"+"\""+channel.getId()+"\"}";
				message=Constants.API_MESSAGE_SUCCESS;
				status=Constants.API_STATUS_SUCCESS;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 栏目修改接口
	 * @param channelId 栏目ID 必选
	 * @param siteId 站点ID 非必选
	 * @param parentId 父栏目ID 非必选
	 * @param name  名称  非必选
	 * @param path  路径 非必选
	 * @param title 标题 非必选
	 * @param keywords meta关键词  非必选
	 * @param desc 描述 非必选
	 * @param txt  栏目文本内容 非必选
	 * @param priority 排序  非必选
	 * @param display 是否展示 非必选
	 * @param modelId 模型ID 非必选  新闻 1
	 * @param workflowId 应用工作流ID 非必选
	 * @param titleImg 标题图 非必选
	 * @param contentImg 内容图 非必选
	 * @param finalStep 终审步骤 非必选
	 * @param afterCheck 审核后 非必选
	 * @param tplChannel PC端模板 非必选
	 * @param tplMobileChannel 移动端模板 非必选
	 * @param appId appid 必选
	 * @param nonce_str 随机数 必选
	 * @param sign 签名 必选
	 * @throws JSONException
	 */
	@RequestMapping(value = "/api/channel/update.jspx")
	public void channelUpdate(
			Integer channelId,Integer siteId,Integer parentId,
			String name,String path,String title,String keywords,
			String desc,String txt,Integer priority,
			Boolean display,Integer modelId,Integer workflowId,
			String titleImg,String contentImg,Byte finalStep,
			Byte afterCheck,String tplChannel,String tplMobileChannel,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		Map<String, String> attr = RequestUtils.getRequestMap(request, "attr_");
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				sign,nonce_str,channelId);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证appId
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			CmsSite site=siteMng.findById(siteId);
			Channel channel=channelMng.findById(channelId);
			if(site==null){
				site=CmsUtils.getSite(request);
			}
			if(channel==null){
				message=Constants.API_MESSAGE_PARAM_ERROR;
			}else{
				site=channel.getSite();
				//签名数据不可重复利用
				ApiRecord record=apiRecordMng.findBySign(sign, appId);
				if(record!=null){
					message=Constants.API_MESSAGE_REQUEST_REPEAT;
				}else{
					ChannelExt ext=channel.getChannelExt();
					ChannelTxt channelTxt=channel.getChannelTxt();
					if(channelTxt==null){
						channelTxt=new ChannelTxt();
					}
					if(StringUtils.isNotBlank(path)){
						channel.setPath(path);
					}
					if(StringUtils.isNotBlank(name)){
						ext.setName(name);
					}
					if(StringUtils.isNotBlank(title)){
						ext.setTitle(title);
					}
					if(StringUtils.isNotBlank(keywords)){
						ext.setKeywords(keywords);
					}
					if(StringUtils.isNotBlank(desc)){
						ext.setDescription(desc);
					}
					if(StringUtils.isNotBlank(titleImg)){
						ext.setTitleImg(titleImg);
					}
					if(StringUtils.isNotBlank(contentImg)){
						ext.setContentImg(contentImg);
					}
					if(finalStep!=null){
						ext.setFinalStep(finalStep);
					}
					if(afterCheck!=null){
						ext.setAfterCheck(afterCheck);
					}
					if(StringUtils.isNotBlank(txt)){
						channelTxt.setTxt(txt);
					}
					if (priority != null) {
						channel.setPriority(priority);
					}
					if (display != null) {
						channel.setDisplay(display);
					}
					// 加上模板前缀
					String tplPath = site.getTplPath();
					if (!StringUtils.isBlank(tplChannel)) {
						ext.setTplChannel(tplPath + tplChannel);
					}
					if (!StringUtils.isBlank(tplMobileChannel)) {
						ext.setTplMobileChannel(tplPath + tplMobileChannel);
					}
					channelMng.update(channel, ext, channelTxt, null, null,
							null, parentId, attr,modelId,workflowId,null,null,null);
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/channel/update.jspx",sign);
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private JSONObject convertToJson(Channel channel,Integer https) throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", channel.getId());
		json.put("name", channel.getName());
		json.put("txt", channel.getTxt());
		json.put("path", channel.getPath());
		json.put("title", channel.getTitle());
		json.put("keywords", channel.getKeywords());
		json.put("description", channel.getDescription());
		json.put("deep", channel.getDeep());

		json.put("childCount", channel.getChild().size());
		
		json.put("hasContent", channel.getHasContent());
		json.put("display", channel.getDisplay());
		json.put("link", channel.getLink());
		
		CmsSite site=channel.getSite();
		String urlPrefix="";
		if(https==com.jeecms.cms.api.Constants.URL_HTTP){
			json.put("url", channel.getUrl());
			json.put("siteUrl", channel.getSite().getUrl());
			urlPrefix=site.getUrlPrefixWithNoDefaultPort();
		}else{
			json.put("url", channel.getHttpsUrl());
			json.put("siteUrl", channel.getSite().getHttpsUrl());
			urlPrefix=site.getSafeUrlPrefix();
		}
		Ftp uploadFtp=site.getUploadFtp();
		boolean uploadToFtp=false;
		if(uploadFtp!=null){
			uploadToFtp=true;
		}
		if(!uploadToFtp){
			if(StringUtils.isNotBlank(channel.getTitleImg())){
				json.put("titleImg", urlPrefix+channel.getTitleImg());
				json.put("contentImg",urlPrefix+channel.getContentImg());
			}else{
				json.put("titleImg", "");
				json.put("contentImg", "");
			}
		}else{
			if(StringUtils.isNotBlank(channel.getTitleImg())){
				json.put("titleImg", channel.getTitleImg());
				json.put("contentImg", channel.getContentImg());
			}else{
				json.put("titleImg", "");
				json.put("contentImg", "");
			}
		}
		json.put("hasTitleImg", channel.getHasTitleImg());
		json.put("hasContentImg", channel.getHasContentImg());
		json.put("views", channel.getChannelCount().getViews());
		json.put("viewsMonth", channel.getChannelCount().getViewsMonth());
		json.put("viewsWeek", channel.getChannelCount().getViewsWeek());
		json.put("viewsDay", channel.getChannelCount().getViewsDay());
		json.put("siteName", channel.getSite().getName());
		json.put("siteId", channel.getSite().getId());
		json.put("model", channel.getModel().getName());
		json.put("modelId", channel.getModel().getId());
		
		if(channel.getParent()!=null){
			json.put("parentId", channel.getParent().getId());
			json.put("parentName", channel.getParent().getName());
			json.put("parentUrl", channel.getParent().getUrl());
			json.put("parentTxt", channel.getParent().getTxt());
			json.put("parentPath", channel.getParent().getPath());
			json.put("parentTitle", channel.getParent().getTitle());
		}
		
		if(channel.getTopChannel()!=null){
			json.put("topId", channel.getTopChannel().getId());
			json.put("topName", channel.getTopChannel().getName());
			json.put("topUrl", channel.getTopChannel().getUrl());
			json.put("topTxt", channel.getTopChannel().getTxt());
			json.put("topPath", channel.getTopChannel().getPath());
			json.put("topTitle", channel.getTopChannel().getTitle());
		}
		return json;
	}
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private CmsSiteMng siteMng;
	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
}

