package com.jeecms.cms.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsConfigContentCharge;
import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.CmsModel;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentBuy;
import com.jeecms.cms.entity.main.ContentCharge;
import com.jeecms.cms.entity.main.ContentCheck;
import com.jeecms.cms.entity.main.ContentDoc;
import com.jeecms.cms.entity.main.ContentExt;
import com.jeecms.cms.entity.main.ContentTxt;
import com.jeecms.cms.entity.main.ContentType;
import com.jeecms.cms.manager.assist.CmsConfigContentChargeMng;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsModelMng;
import com.jeecms.cms.manager.main.ContentBuyMng;
import com.jeecms.cms.manager.main.ContentCountMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.cms.manager.main.ContentTypeMng;
import com.jeecms.cms.staticpage.ContentStatusChangeThread;
import com.jeecms.common.util.ArrayUtils;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsConfig;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.manager.CmsConfigMng;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class ContentApiAct {
	//顶
	private static final String OPERATE_UP="up";
	//踩
	private static final String OPERATE_DOWN="down";
	//审核
	private static final String OPERATE_CHECK="check";
	//退回
	private static final String OPERATE_REJECT="reject";
	//删除至回收站
	private static final String OPERATE_DEL="del";
	//还原
	private static final String OPERATE_RECYCLE="recycle";
	//保存
	private static final String OPERATE_SAVE="save";
	//修改
	private static final String OPERATE_UPDATE="update";
	//购买
	private static final String OPERATE_BUY="buy";
	//打赏
	private static final String OPERATE_REWARD="reward";
	
	/**
	 * 内容列表API
	 * ids tagIds、topicId、channelIds、siteIds 必须有一个参数有值，否则结果为空 
	 * ids tagIds、topicId、channelIds、siteIds 的参数值之间以,号分隔,比如channelIds "73,74"
	 * channelOption参数仅在使用 channelIds参数时有效，默认值为1
	 * @param format 非必选 默认1 内容信息简化模式  0全部信息模式
	 * @param ids  ids优先级最高
	 * @param tagIds  tagID优先级第二
	 * @param topicId 专题ID 优先级第三
	 * @param channelIds 栏目ID 优先级第四
	 * @param siteIds 站点ID 优先级第五
	 * @param typeIds 类型ID数组  非必选
	 * @param titleImg  是否有标题图 非必选
	 * @param recommend  推荐 非必选 
	 * @param orderBy  排序 非必选 默认4固顶降序
	 * @param title   标题检索  非必选
	 * @param channelOption  channelOption 1子栏目 2副栏目  0或者channelIds多个值
	 * @param first   查询开始下标  非必选  默认0
	 * @param count	  查询数量  非必选  默认10
	 */
	@RequestMapping(value = "/api/content/list.jspx")
	public void contentList(
			Integer format,String ids,String tagIds,Integer topicId,
			String channelIds,String siteIds,Integer https,
			String typeIds,Boolean titleImg,Boolean recommend,
			Integer orderBy,String title,
			Integer channelOption,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(orderBy==null){
			orderBy=4;
		}
		if(channelOption==null){
			channelOption=1;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		if(format==null){
			format=Content.CONTENT_INFO_SIMPLE;
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		Integer[]intIds= null,intTagIds = null
				,intChannelIds= null,intSiteIds = null,intTypeIds= null;
		if(StringUtils.isNotBlank(ids)){
			intIds=ArrayUtils.parseStringToArray(ids);
		}
		if(StringUtils.isNotBlank(tagIds)){
			intTagIds=ArrayUtils.parseStringToArray(tagIds);
		}
		if(StringUtils.isNotBlank(channelIds)){
			intChannelIds=ArrayUtils.parseStringToArray(channelIds);
		}
		if(StringUtils.isNotBlank(siteIds)){
			intSiteIds=ArrayUtils.parseStringToArray(siteIds);
		}
		if(StringUtils.isNotBlank(typeIds)){
			intTypeIds=ArrayUtils.parseStringToArray(typeIds);
		}
		List<Content>contents=null;
		if(intIds!=null){
			contents=contentMng.getListByIdsForTag(intIds, orderBy);
		}else if(intTagIds!=null){
			contents=contentMng.getListByTagIdsForTag(intTagIds, intSiteIds,
					intChannelIds, intTypeIds, null, titleImg, recommend,
					title,ContentDoc.ALL,null, orderBy, first, count);
		}else if(topicId!=null){
			contents=contentMng.getListByTopicIdForTag(topicId, intSiteIds,
					intChannelIds, intTypeIds, titleImg, recommend, title,
					ContentDoc.ALL,null,orderBy, first, count);
		}else if(intChannelIds!=null){
			contents=contentMng.getListByChannelIdsForTag(intChannelIds,
					intTypeIds, titleImg, recommend, title,ContentDoc.ALL,
					null,orderBy, channelOption,first, count);
		}else if(intSiteIds!=null){
			contents=contentMng.getListBySiteIdsForTag(intSiteIds, intTypeIds,
					titleImg, recommend, title,ContentDoc.ALL,
					null,orderBy, first, count);
		}
		JSONArray jsonArray=new JSONArray();
		if(contents!=null&&contents.size()>0){
			for(int i=0;i<contents.size();i++){
				jsonArray.put(i, contents.get(i).convertToJson(format,https,false));
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 我的内容API
	 * @param appId   appid 必选
	 * @param sessionKey 会话标识 必选
	 * @param format 内容显示模式  1简化模式    0全   默认1
	 * @param channelId 栏目id 非必选
	 * @param siteId 站点id 非必选默认当前站
	 * @param modelId 模型id 非必选  
	 * @param orderBy 排序 非必选 默认4 固顶降序
	 * @param title 标题 非必选
	 * @param first 开始 非必选 默认0
	 * @param count 数量 非必选 默认10 
	 */
	@RequestMapping(value = "/api/content/mycontents.jspx")
	public void contentListForUser(
			String appId,String sessionKey,Integer https,
			Integer format,Integer channelId,Integer siteId,
			Integer modelId,Integer orderBy,String title,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if(orderBy==null){
			orderBy=4;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		if(format==null){
			format=Content.CONTENT_INFO_SIMPLE;
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		if(siteId!=null){
			siteId=CmsUtils.getSiteId(request);
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
			JSONArray jsonArray=new JSONArray();
			if(user!=null){
				List<Content>contents=contentMng.getListForMember(title, channelId, 
						siteId, modelId, user.getId(), first, count);
				if(contents!=null&&contents.size()>0){
					for(int i=0;i<contents.size();i++){
						jsonArray.put(i, contents.get(i).convertToJson(format,https,false));
					}
				}
			}
			body=jsonArray.toString();
			status=Constants.API_STATUS_SUCCESS;
			message=Constants.API_MESSAGE_SUCCESS;
		}		
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 内容信息获取API
	 * @param format 1简化模式  0全 非必选 默认1
	 * @param id id 必选  
	 * @param next 非必选   1下一篇 0 上一篇  为空则是当前id内容
	 * @param channelId  栏目id  非必选
	 * @param siteId 站点id  非必选 默认当前站
	 * @param sessionKey 会话标识 非必选
	 * @param appId appId 非必选 如果存在以及sessionKey存在则盘点用户是否收藏了该内容
	 */ 
	@RequestMapping(value = "/api/content/get.jspx")
	public void contentGet(Integer format,Integer https,Integer id,Integer next,
			Integer channelId,Integer siteId,String sessionKey,String appId,
			HttpServletRequest request,HttpServletResponse response)
					throws JSONException {
		if (id == null) {
			ResponseUtils.renderJson(response, "[]");
			return;
		}
		if(format==null){
			format=Content.CONTENT_INFO_SIMPLE;
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		if(siteId==null){
			siteId=CmsUtils.getSiteId(request);
		}
		Content content;
		if(next==null){
			content=contentMng.findById(id);
		}else{
			boolean nextBool=false;
			if(next.equals(1)){
				nextBool=true;
			}
			content = contentMng.getSide(id, siteId, channelId, nextBool);
		}
		if (content != null) {
			CmsUser user=null;
			boolean hasCollect=false;
			if(StringUtils.isNotBlank(appId)){
				ApiAccount apiAccount=apiAccountMng.findByAppId(appId);
				if(apiAccount!=null){
					String aesKey=apiAccount.getAesKey();
					user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
					if(user!=null){
						hasCollect=user.getCollectContents().contains(content);
					}
				}
			}
			ResponseUtils.renderJson(response,content.convertToJson(format,https,hasCollect).toString());
		} else {
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	/**
	 * 内容发布API
	 * @param siteId 站点id 非必选  默认当前站点id
	 * @param channelId 栏目id 必选 
	 * @param modelId 模型id  非必选  默认系统默认模型
	 * @param title  标题   必选
	 * @param author 作者 非必选
	 * @param desc 描述 非必选
	 * @param txt 内容 必选
	 * @param tagStr tag关键词 非必选
	 * @param mediaPath 多媒体路径 非必选
	 * @param mediaType 多媒体播放器 非必选
	 * @param attachmentPaths  附件路径 多个附件 用逗号,分隔 非必选
	 * @param attachmentNames 附件名称 多个附件 用逗号,分隔 非必选
	 * @param attachmentFilenames 附件文件名 多个附件 用逗号,分隔 非必选
	 * @param picPaths 图片集路径 多个图片 用逗号,分隔 非必选
	 * @param picDescs 图片集描述 多个图片 用逗号,分隔 非必选
	 * @param charge 收费模式设置 非必选  默认免费模式
	 * @param chargeAmount 收费金额 非必选
	 * @param isDoc 是否文库 非必选  默认false
	 * @param docPath 文库文档路径  非必选
	 * @param downNeed 文库下载需要积分  非必选
	 * @param isOpen 文库是否开放 非必选 默认
	 * @param docSuffix 文库文件后缀格式 非必选
	 * @param contentStatus 内容状态 非必选 默认审核中1 0 草稿   1审核中 2终审  4投稿
	 * @param typeId 类型id 非必选 默认系统默认类型
	 * @param contentImg 内容图 非必选
	 * @param titleImg 标题图 非必选
	 * @param typeImg 类型图  非必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/save.jspx")
	public void contentSave(
			Integer siteId, Integer channelId,Integer modelId, 
			String title, String author, String desc,
			String txt, String tagStr,String mediaPath,String mediaType,
			String attachmentPaths, String attachmentNames,
			String attachmentFilenames, 
			String picPaths, String picDescs,
			Short charge,Double chargeAmount,
			Boolean isDoc,String docPath,Integer downNeed,
			Boolean isOpen,String docSuffix,
			Byte contentStatus,Integer typeId,
			String contentImg,String titleImg,String typeImg,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		saveOrUpdateContent(OPERATE_SAVE, siteId, null, 
				channelId, modelId, title, author, desc, txt, 
				tagStr, mediaPath, mediaType, attachmentPaths, 
				attachmentNames, attachmentFilenames, picPaths, picDescs, 
				charge, chargeAmount, isDoc, docPath, downNeed, isOpen,
				docSuffix, contentStatus, typeId, contentImg,
				titleImg, typeImg, appId, nonce_str, sign,
				sessionKey, request, response);
	}
	
	/**
	 * 内容修改API
	 * @param id 内容id 必选 
	 * @param channelId 栏目id 非必选 
	 * @param modelId 模型id  非必选  默认系统默认模型
	 * @param title  标题   非必选
	 * @param author 作者 非必选
	 * @param desc 描述 非必选
	 * @param txt 内容 非 非必选
	 * @param tagStr tag关键词 非必选
	 * @param mediaPath 多媒体路径 非必选
	 * @param mediaType 多媒体播放器 非必选
	 * @param attachmentPaths  附件路径 多个附件 用逗号,分隔 非必选
	 * @param attachmentNames 附件名称 多个附件 用逗号,分隔 非必选
	 * @param attachmentFilenames 附件文件名 多个附件 用逗号,分隔 非必选
	 * @param picPaths 图片集路径 多个图片 用逗号,分隔 非必选
	 * @param picDescs 图片集描述 多个图片 用逗号,分隔 非必选
	 * @param charge 收费模式设置 非必选  默认免费模式
	 * @param chargeAmount 收费金额 非必选
	 * @param isDoc 是否文库 非必选  默认false
	 * @param docPath 文库文档路径  非必选
	 * @param downNeed 文库下载需要积分  非必选
	 * @param isOpen 文库是否开放 非必选 默认
	 * @param docSuffix 文库文件后缀格式 非必选
	 * @param contentStatus 内容状态 非必选 默认审核中1 0 草稿   1审核中 2终审  4投稿
	 * @param typeId 类型id 非必选 默认系统默认类型
	 * @param contentImg 内容图 非必选
	 * @param titleImg 标题图 非必选
	 * @param typeImg 类型图  非必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/update.jspx")
	public void contentUpdate(
			Integer id, Integer channelId,Integer modelId, 
			String title, String author, String desc,
			String txt, String tagStr,String mediaPath,String mediaType,
			String attachmentPaths, String attachmentNames,
			String attachmentFilenames, 
			String picPaths, String picDescs,
			Short charge,Double chargeAmount,
			Boolean isDoc,String docPath,Integer downNeed,
			Boolean isOpen,String docSuffix,
			Byte contentStatus,Integer typeId,
			String contentImg,String titleImg,String typeImg,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		saveOrUpdateContent(OPERATE_UPDATE, null, id, 
				channelId, modelId, title, author, desc, txt, 
				tagStr, mediaPath, mediaType, attachmentPaths, 
				attachmentNames, attachmentFilenames, picPaths, picDescs, 
				charge, chargeAmount, isDoc, docPath, downNeed, isOpen,
				docSuffix, contentStatus, typeId, contentImg,
				titleImg, typeImg, appId, nonce_str, sign,
				sessionKey, request, response);
	}
	
	/**
	 * 删除内容至回收站API
	 * @param ids 内容id 逗号,分隔  必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/del.jspx")
	public void contentDel(
			String  ids,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		contentsOperate(OPERATE_DEL, ids, appId,
				nonce_str, sign, sessionKey, request, response);
	}
	
	/**
	 * 回收站恢复内容API
	 * @param ids 内容id 逗号,分隔  必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/recycle.jspx")
	public void contentRecycle(
			String  ids,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		contentsOperate(OPERATE_RECYCLE, ids, appId,
				nonce_str, sign, sessionKey, request, response);
	}
	
	/**
	 * 内容审核API
	 * @param ids 内容id 逗号,分隔  必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/check.jspx")
	public void contentCheck(
			String  ids,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		contentsOperate(OPERATE_CHECK, ids, appId,
				nonce_str, sign, sessionKey, request, response);
	}
	
	/**
	 * 内容退回API
	 * @param ids 内容id 逗号,分隔  必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/reject.jspx")
	public void contentReject(
			String  ids,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		contentsOperate(OPERATE_REJECT, ids, appId,
				nonce_str, sign, sessionKey, request, response);
	}
	
	/**
	 * 内容顶API
	 * @param id 内容id   必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 非 必选
	 */
	@RequestMapping(value = "/api/content/up.jspx")
	public void contentUp(
			Integer id,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		singleContentOperate(OPERATE_UP, id, appId,
				nonce_str, sign, sessionKey,  null,null,request, response);
	}
	
	/**
	 * 内容踩API
	 * @param id 内容id   必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 非必选
	 */
	@RequestMapping(value = "/api/content/down.jspx")
	public void contentDown(
			Integer id,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)throws JSONException {
		singleContentOperate(OPERATE_DOWN, id, appId,
				nonce_str, sign, sessionKey, null,null,request, response);
	}
	
	/**
	 * 内容购买API
	 * @param id  内容ID 必选
	 * @param outOrderNum 外部订单号 必选
	 * @param orderType  1微信支付   2支付宝支付 必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识  必选
	 */
	@RequestMapping(value = "/api/content/buy.jspx")
	public void contentBuy(
			Integer id,String outOrderNum,
			Integer orderType,String appId,String nonce_str,
			String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)
					throws JSONException {
		singleContentOperate(OPERATE_BUY, id, appId,
				nonce_str, sign, sessionKey,
				outOrderNum,orderType,request, response);
	}
	
	/**
	 * 内容打赏API
	 * @param id  内容ID 必选
	 * @param outOrderNum 外部订单号 必选
	 * @param orderType  1微信支付   2支付宝支付 必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 * @param sessionKey 会话标识 必选
	 */
	@RequestMapping(value = "/api/content/reward.jspx")
	public void contentReward(
			Integer id,String outOrderNum,
			Integer orderType,String appId,String nonce_str,
			String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response)
					throws JSONException {
		singleContentOperate(OPERATE_REWARD, id, appId,
				nonce_str, sign, sessionKey, 
				outOrderNum,orderType,request, response);
	}
	
	
	private void singleContentOperate(
			String operate,Integer id,
			String appId,String nonce_str,String sign,String sessionKey,
			String outOrderNum,Integer orderType,
			HttpServletRequest request,HttpServletResponse response){
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		CmsSite currSite=CmsUtils.getSite(request);
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user=null;
		//验证公共非空参数
		if(operate.equals(OPERATE_BUY)){
			errors=ApiValidate.validateRequiredParams(request,errors,appId,
					sessionKey,nonce_str,sign,id,currSite);
		}else{
			errors=ApiValidate.validateRequiredParams(request,errors,appId,
					nonce_str,sign,id,currSite);
		}
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
			//apiAccount可能获取不到，需要再次判断
			if(!errors.hasErrors()){
				String aesKey=apiAccount.getAesKey();
				user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
				//购买模式需要验证用户
				if(operate.equals(OPERATE_BUY)){
					//验证用户
					if(user==null){
						errors.addErrorString(Constants.API_MESSAGE_USER_NOT_LOGIN);
					}
				}
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
				Content c=contentMng.findById(id);
				if(c!=null){
					if(operate.equals(OPERATE_DOWN)){
						contentCountMng.contentDown(id);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/down.jspx",sign);
						message=Constants.API_MESSAGE_SUCCESS;
						status=Constants.API_STATUS_SUCCESS;
					}else if(operate.equals(OPERATE_UP)){
						contentCountMng.contentUp(id);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/up.jspx",sign);
						message=Constants.API_MESSAGE_SUCCESS;
						status=Constants.API_STATUS_SUCCESS;
					}else if(operate.equals(OPERATE_BUY)){
						if(StringUtils.isNotBlank(outOrderNum)&&orderType!=null){
							//购买
							//外部订单号不可以用多次
							ContentBuy buy=contentBuyMng.findByOutOrderNum(outOrderNum, orderType);
							if(buy==null){
								buy=contentBuyMng.contentOrder(id, orderType,
										ContentCharge.MODEL_CHARGE, user.getId(),outOrderNum);
								if(buy.getPrePayStatus()==ContentBuy.PRE_PAY_STATUS_SUCCESS){
									apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
											appId, "/api/content/buy.jspx",sign);
									message=Constants.API_MESSAGE_SUCCESS;
									status=Constants.API_STATUS_SUCCESS;
								}else if(buy.getPrePayStatus()==ContentBuy.PRE_PAY_STATUS_ORDER_NUM_ERROR){
									message=Constants.API_MESSAGE_ORDER_NUMBER_ERROR;
								}else if(buy.getPrePayStatus()==ContentBuy.PRE_PAY_STATUS_ORDER_AMOUNT_NOT_ENOUGH){
									message=Constants.API_MESSAGE_ORDER_AMOUNT_NOT_ENOUGH;
								}
							}else{
								message=Constants.API_MESSAGE_ORDER_NUMBER_USED;
							}
						}else{
							message=Constants.API_MESSAGE_PARAM_REQUIRED;
						}
					}else if(operate.equals(OPERATE_REWARD)){
						if(StringUtils.isNotBlank(outOrderNum)
								&&orderType!=null){
							//打赏
							//外部订单号不可以用多次
							ContentBuy buy=contentBuyMng.findByOutOrderNum(outOrderNum, orderType);
							if(buy==null){
								//允许匿名打赏
								if(user!=null){
									buy=contentBuyMng.contentOrder(id, orderType,
											ContentCharge.MODEL_REWARD, user.getId(), outOrderNum);
								}else{
									buy=contentBuyMng.contentOrder(id, orderType,
											ContentCharge.MODEL_REWARD, null, outOrderNum);
								}
								if(buy.getPrePayStatus()==ContentBuy.PRE_PAY_STATUS_SUCCESS){
									apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
											appId, "/api/content/reward.jspx",sign);
									message=Constants.API_MESSAGE_SUCCESS;
									status=Constants.API_STATUS_SUCCESS;
								}else if(buy.getPrePayStatus()==ContentBuy.PRE_PAY_STATUS_ORDER_NUM_ERROR){
									message=Constants.API_MESSAGE_ORDER_NUMBER_ERROR;
								}
							}else{
								message=Constants.API_MESSAGE_ORDER_NUMBER_USED;
							}
						}else{
							message=Constants.API_MESSAGE_PARAM_REQUIRED;
						}
					}
				}else{
					message=Constants.API_MESSAGE_CONTENT_NOT_FOUND;
				}
			}	
		}	
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private void contentsOperate(String operate,String  ids,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response){
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		CmsSite currSite=CmsUtils.getSite(request);
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				sessionKey,nonce_str,sign,ids,currSite);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
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
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				String[]idArray=ids.split(Constants.API_ARRAY_SPLIT_STR);
				Integer[]intIds=new Integer[idArray.length];
				for(int i=0;i<idArray.length;i++){
					if(StringUtils.isNotBlank(idArray[i])){
						Integer contentId=Integer.parseInt(idArray[i]);
						intIds[i]=contentId;
					}
				}
				boolean contentNotFound=false;
				for(Integer id:intIds){
					Content c=contentMng.findById(id);
					if(c==null){
						contentNotFound=true;
						break;
					}
				}
				if(!contentNotFound){
					if(operate.equals(OPERATE_DEL)){
						contentMng.cycle(user, intIds);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/del.jspx",sign);
					}else if(operate.equals(OPERATE_REJECT)){
						contentMng.reject(intIds,user,"");
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/reject.jspx",sign);
					}else if(operate.equals(OPERATE_CHECK)){
						contentMng.check(intIds,user);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/check.jspx",sign);
					}else if(operate.equals(OPERATE_RECYCLE)){
						contentMng.recycle(intIds);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/recycle.jspx",sign);
					}
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}else{
					message=Constants.API_MESSAGE_CONTENT_NOT_FOUND;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private void saveOrUpdateContent(
			String operate,Integer siteId,
			Integer id, Integer channelId,Integer modelId, 
			String title, String author, String desc,
			String txt, String tagStr,String mediaPath,String mediaType,
			String attachmentPaths, String attachmentNames,
			String attachmentFilenames, 
			String picPaths, String picDescs,
			Short charge,Double chargeAmount,
			Boolean isDoc,String docPath,Integer downNeed,
			Boolean isOpen,String docSuffix,
			Byte contentStatus,Integer typeId,
			String contentImg,String titleImg,String typeImg,
			String appId,String nonce_str,String sign,String sessionKey,
			HttpServletRequest request,HttpServletResponse response){
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		CmsSite site=CmsUtils.getSite(request);
		if(operate.equals(OPERATE_UPDATE)){
			if(id!=null){
				Content content=contentMng.findById(id);
				if(content!=null){
					site=content.getSite();
				}
			}
		}else if(operate.equals(OPERATE_SAVE)){
			if(siteId!=null){
				CmsSite findsite=siteMng.findById(siteId);
				if(site!=null){
					site=findsite;
				}
			}
		}
		
		if(isDoc==null){
			isDoc=false;
		}
		//contentStatus 0 草稿   1审核中 2终审  4投稿
		if(contentStatus==null){
			contentStatus=ContentCheck.CHECKING;
		}
		if(typeId==null){
			ContentType defType=contentTypeMng.getDef();
			if(defType!=null){
				typeId=defType.getId();
			}
		}
		CmsSite currSite=CmsUtils.getSite(request);
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				sessionKey,nonce_str,sign,currSite);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
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
			errors=validateParams(operate,title,author, desc, txt, isDoc,
					docPath,tagStr, channelId,user,site,request, response);
			if(errors.hasErrors()){
				message="\""+errors.getErrors().get(0)+"\"";
			}else{
				//签名数据不可重复利用
				ApiRecord record=apiRecordMng.findBySign(sign, appId);
				if(record!=null){
					message=Constants.API_MESSAGE_REQUEST_REPEAT;
				}else{
					String attachmentPath[]=null ,attachmentName[]=null,
					attachmentFilename[]=null,picPath[]=null,picDesc[]= null;
					if(StringUtils.isNotBlank(attachmentPaths)){
						attachmentPath=attachmentPaths.split(Constants.API_ARRAY_SPLIT_STR);
					}
					if(StringUtils.isNotBlank(attachmentNames)){
						attachmentName=attachmentNames.split(Constants.API_ARRAY_SPLIT_STR);
					}
					if(StringUtils.isNotBlank(attachmentFilenames)){
						attachmentFilename=attachmentNames.split(Constants.API_ARRAY_SPLIT_STR);
					}
					if(StringUtils.isNotBlank(picPaths)){
						picPath=picPaths.split(Constants.API_ARRAY_SPLIT_STR);
					}
					if(StringUtils.isNotBlank(picDescs)){
						picDesc=picDescs.split(Constants.API_ARRAY_SPLIT_STR);
					}
					Integer contentId=null;
					if(operate.equals(OPERATE_UPDATE)){
						contentId=updateContent(id,site, user, title, author, desc,
								txt, tagStr, channelId, modelId, mediaPath, mediaType,
								attachmentPath, attachmentName, attachmentFilename, 
								picPath, picDesc, charge, chargeAmount, 
								isDoc, docPath, downNeed, isOpen,docSuffix,
								contentStatus,typeId,contentImg,titleImg,typeImg);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/update.jspx",sign);
					}else if(operate.equals(OPERATE_SAVE)){
						contentId=saveContent(site, user, title, author, desc,
								txt, tagStr, channelId, modelId, mediaPath, mediaType,
								attachmentPath, attachmentName, attachmentFilename, 
								picPath, picDesc, charge, chargeAmount, 
								isDoc, docPath, downNeed, isOpen,docSuffix,
								contentStatus,typeId,contentImg,titleImg,typeImg);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/content/save.jspx",sign);
					}
					body="{\"id\":"+"\""+contentId+"\"}";
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private Integer saveContent(CmsSite site,CmsUser user,
			String title, String author, String description,
			String txt, String tagStr, Integer channelId,Integer modelId, 
			String mediaPath,String mediaType,
			String[] attachmentPaths, String[] attachmentNames,
			String[] attachmentFilenames, String[] picPaths, String[] picDescs,
			Short charge,Double chargeAmount,
			Boolean isDoc,String docPath,Integer downNeed,
			Boolean isOpen,String docSuffix,
			Byte contentStatus,Integer typeId,
			String contentImg,String titleImg,String typeImg){
		Content c = new Content();
		c.setSite(site);
		CmsModel defaultModel=cmsModelMng.getDefModel();
		if(modelId!=null){
			CmsModel m=cmsModelMng.findById(modelId);
			if(m!=null){
				c.setModel(m);
			}else{
				c.setModel(defaultModel);
			}
		}else{
			c.setModel(defaultModel);
		}
		ContentExt ext = new ContentExt();
		ext.setTitle(title);
		ext.setAuthor(author);
		ext.setDescription(description);
		ext.setMediaPath(mediaPath);
		ext.setMediaType(mediaType);
		ContentTxt t = new ContentTxt();
		t.setTxt(txt);
		ContentType type=null;
		if(typeId!=null){
			type = contentTypeMng.findById(typeId);
		}
		if (type == null) {
			throw new RuntimeException("Default ContentType not found.");
		}
		String[] tagArr = StrUtils.splitAndTrim(tagStr, ",", null);
		if(c.getRecommendLevel()==null){
			c.setRecommendLevel((byte) 0);
		}
		if(StringUtils.isNotBlank(contentImg)){
			ext.setContentImg(contentImg);
		}
		if(StringUtils.isNotBlank(typeImg)){
			ext.setTypeImg(typeImg);
		}
		if(StringUtils.isNotBlank(titleImg)){
			ext.setTitleImg(titleImg);
		}
		c.setStatus(contentStatus);
		ContentDoc doc = null;
		if(isDoc){
			doc=new ContentDoc();
			doc.setDocPath(docPath);
			doc.setDownNeed(downNeed);
			doc.init();
			doc.setFileSuffix(docSuffix);
		}
		CmsConfigContentCharge contentChargeConfig=cmsConfigContentChargeMng.getDefault();
		CmsConfig cmsConfig=cmsConfigMng.get();
		Double[]fixValues=ArrayUtils.convertStrArrayToDouble(cmsConfig.getRewardFixValues());
		c=contentMng.save(c, ext, t, doc,null, null, null,
				tagArr, attachmentPaths, attachmentNames, attachmentFilenames,
				picPaths, picDescs, channelId, typeId, null,null,
				charge,chargeAmount,contentChargeConfig.getRewardPattern(),
				contentChargeConfig.getRewardMin(),contentChargeConfig.getRewardMax(),
				fixValues,user, true);
		afterContentStatusChange(c,null, ContentStatusChangeThread.OPERATE_ADD);
		return c.getId();
	}
	
	
	private Integer updateContent(Integer id,CmsSite site,CmsUser user,
			String title, String author, String description,
			String txt, String tagStr, Integer channelId,Integer modelId, 
			String mediaPath,String mediaType,
			String[] attachmentPaths, String[] attachmentNames,
			String[] attachmentFilenames, String[] picPaths, String[] picDescs,
			Short charge,Double chargeAmount,
			Boolean isDoc,String docPath,Integer downNeed,
			Boolean isOpen,String docSuffix,
			Byte contentStatus,Integer typeId,
			String contentImg,String titleImg,String typeImg){
		Content c = new Content();
		c.setId(id);
		c.setSite(site);
		CmsModel defaultModel=cmsModelMng.getDefModel();
		if(modelId!=null){
			CmsModel m=cmsModelMng.findById(modelId);
			if(m!=null){
				c.setModel(m);
			}else{
				c.setModel(defaultModel);
			}
		}else{
			c.setModel(defaultModel);
		}
		ContentExt ext = new ContentExt();
		ext.setId(id);
		if(StringUtils.isNotBlank(title)){
			ext.setTitle(title);
		}
		if(StringUtils.isNotBlank(author)){
			ext.setAuthor(author);
		}
		if(StringUtils.isNotBlank(description)){
			ext.setDescription(description);
		}
		if(StringUtils.isNotBlank(mediaPath)){
			ext.setMediaPath(mediaPath);
		}
		if(StringUtils.isNotBlank(mediaType)){
			ext.setMediaType(mediaType);
		}
		ContentTxt t = new ContentTxt();
		t.setId(id);
		if(StringUtils.isNotBlank(txt)){
			t.setTxt(txt);
		}
		ContentType type=null;
		if(typeId!=null){
			type = contentTypeMng.findById(typeId);
		}
		if(type==null){
			type=contentTypeMng.getDef();
		}
		String[] tagArr = StrUtils.splitAndTrim(tagStr, ",", null);
		if(c.getRecommendLevel()==null){
			c.setRecommendLevel((byte) 0);
		}
		if(StringUtils.isNotBlank(contentImg)){
			ext.setContentImg(contentImg);
		}
		if(StringUtils.isNotBlank(typeImg)){
			ext.setTypeImg(typeImg);
		}
		if(StringUtils.isNotBlank(titleImg)){
			ext.setTitleImg(titleImg);
		}
		c.setStatus(contentStatus);
		ContentDoc doc = null;
		if(isDoc){
			doc=new ContentDoc();
			doc.setId(id);
			if(StringUtils.isNotBlank(docPath)){
				doc.setDocPath(docPath);
			}
			if(downNeed!=null){
				doc.setDownNeed(downNeed);
			}
			doc.init();
			if(StringUtils.isNotBlank(docSuffix)){
				doc.setFileSuffix(docSuffix);
			}
		}
		List<Map<String, Object>>list=contentMng.preChange(contentMng.findById(id));
		CmsConfigContentCharge contentChargeConfig=cmsConfigContentChargeMng.getDefault();
		CmsConfig cmsConfig=cmsConfigMng.get();
		Double[]fixValues=ArrayUtils.convertStrArrayToDouble(cmsConfig.getRewardFixValues());
		c=contentMng.update(c, ext, t,doc, tagArr, null, null, null, 
				attachmentPaths,attachmentNames, attachmentFilenames
				,picPaths,picDescs, null, channelId, typeId, null, 
				charge,chargeAmount,contentChargeConfig.getRewardPattern(),
				contentChargeConfig.getRewardMin(),contentChargeConfig.getRewardMax(),
				fixValues,user, true);
		afterContentStatusChange(c,list, ContentStatusChangeThread.OPERATE_UPDATE);
		return c.getId();
	}
	
	
	private void afterContentStatusChange(Content content,
			List<Map<String, Object>>list,Short operate){
		ContentStatusChangeThread afterThread = new ContentStatusChangeThread(
				content,operate,
				contentMng.getListenerList(),list);
		afterThread.start();
	}
	
	private WebErrors validateParams(
			String operate,String title, String author,
			String description, String txt,Boolean isDoc,String docPath, 
			String tagStr, Integer channelId,CmsUser user,
			CmsSite site, HttpServletRequest request, HttpServletResponse response) {
		WebErrors errors = WebErrors.create(request);
		if(operate==OPERATE_SAVE){
			if (errors.ifBlank(title, "title", 150)) {
				return errors;
			}
		}
		if (errors.ifMaxLength(author, "author", 100)) {
			return errors;
		}
		if (errors.ifMaxLength(description, "description", 255)) {
			return errors;
		}
		if(!isDoc){
			// 内容不能大于1M
			if(operate==OPERATE_SAVE){
				if (errors.ifBlank(txt, "txt", 1048575)) {
					return errors;
				}
			}
		}else{
			if(operate==OPERATE_SAVE&&StringUtils.isBlank(docPath)){
				errors.addErrorCode("error.hasNotUploadDoc");
				return errors;
			}
		}
		if (errors.ifMaxLength(tagStr, "tagStr", 255)) {
			return errors;
		}
		if (operate==OPERATE_SAVE&&errors.ifNull(channelId, "channelId")) {
			return errors;
		}
		if(user==null){
			errors.addErrorCode("error.usernameNotLogin");
			return errors;
		}
		if (vldChannel(errors, site, user, channelId)) {
			return errors;
		}
		return errors;
	}
	
	private boolean vldChannel(WebErrors errors, CmsSite site, CmsUser user,
			Integer channelId) {
		Channel channel = channelMng.findById(channelId);
		if (errors.ifNotExist(channel, Channel.class, channelId)) {
			return true;
		}
		if (!channel.getSite().getId().equals(site.getId())) {
			errors.notInSite(Channel.class, channelId);
			return true;
		}
		if (!channel.getContriGroups().contains(user.getGroup())) {
			errors.noPermission(Channel.class, channelId);
			return true;
		}
		return false;
	}
	
	@Autowired
	private ContentMng contentMng;
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private CmsSiteMng siteMng;
	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
	@Autowired
	private ContentTypeMng contentTypeMng;
	@Autowired
	private CmsModelMng cmsModelMng;
	@Autowired
	private ContentCountMng contentCountMng;
	@Autowired
	private ContentBuyMng contentBuyMng;
	@Autowired
	private CmsConfigMng cmsConfigMng;
	@Autowired
	private CmsConfigContentChargeMng cmsConfigContentChargeMng;
}

