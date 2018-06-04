package com.jeecms.cms.api;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.action.front.AbstractVote;
import com.jeecms.cms.entity.assist.CmsVoteTopic;
import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.common.util.ArrayUtils;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsVoteApiAct extends AbstractVote{
	
	/**
	 * 调查列表API
	 * @param siteId 站点ID 非必选 默认当前站
	 * @param def 是否默认  非必选 默认全部  true默认  false 非默认 
	 * @param first 开始 非必选 默认0
	 * @param count 数量 非必选 默认10
	 */
	@RequestMapping(value = "/api/vote/list.jspx")
	public void cmsVoteList(Integer https,Integer siteId,
			Boolean def,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(https==null){
			https=Constants.URL_HTTP;
		}
		if (siteId == null) {
			siteId = CmsUtils.getSiteId(request);
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		List<CmsVoteTopic>list=cmsVoteTopicMng.getList(
				def,siteId,first,count);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, list.get(i).convertToJson(https));
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 投票信息获取
	 * @param id 投票ID
	 */
	@RequestMapping(value = "/api/vote/get.jspx")
	public void cmsVoteGet(Integer https,Integer id,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(https==null){
			https=Constants.URL_HTTP;
		}
		CmsVoteTopic voteTopic = cmsVoteTopicMng.findById(id);
		if(voteTopic!=null){
			ResponseUtils.renderJson(response, voteTopic.convertToJson(https).toString());
		}else{
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	/**
	 * 投票API
	 * @param siteId 站点ID 非必选 默认当前站
	 * @param voteId 投票ID 必选 
	 * @param subIds 调查题目ID 逗号,分隔  必选 
	 * @param itemIds 投票的调查题目选择性子项id  逗号,分隔  必选 
	 * @param subTxtIds  投票的调查题目选文本性项id  非必选 
	 * @param replys 投票的调查题目选文本性项回复内容  非必选 
	 * @param sessionKey 会话标志   非必选
	 * @param appId   appid 必选
	 * @param nonce_str 随机数 必选
	 * @param sign 签名 必选
	 */
	@RequestMapping(value = "/api/vote/save.jspx")
	public void cmsVoteSave(
			Integer siteId,
			Integer voteId,String subIds,
			String itemIds,String subTxtIds,String replys, 
			String sessionKey,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,
			HttpServletResponse response,ModelMap model) throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,voteId,subIds,itemIds);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors,apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			CmsVoteTopic voteTopic=cmsVoteTopicMng.findById(voteId);
			String aesKey=apiAccount.getAesKey();
			user=apiUserLoginMng.findUser(sessionKey, aesKey,apiAccount.getIvKey());
			if(voteTopic!=null){
				//签名数据不可重复利用
				ApiRecord record=apiRecordMng.findBySign(sign, appId);
				if(record!=null){
					message=Constants.API_MESSAGE_REQUEST_REPEAT;
				}else{
					Integer[]intSubIds=ArrayUtils.parseStringToArray(subIds);
					Integer[]intSubTxtIds=ArrayUtils.parseStringToArray(subTxtIds);
					String[]reply = null;
					if(StringUtils.isNotBlank(replys)){
						reply=replys.split(Constants.API_ARRAY_SPLIT_STR);
					}
					CmsVoteTopic vote=voteByApi(user, voteId, intSubIds, 
							parseStringToArrayList(itemIds),intSubTxtIds,
							reply, request, response, model);
					if(vote!=null){
						apiRecordMng.callApiRecord(
								RequestUtils.getIpAddr(request),
								appId, "/api/vote/save.jspx",sign);
						body="{\"id\":"+"\""+vote.getId()+"\"}";
						message=Constants.API_MESSAGE_SUCCESS;
						status=Constants.API_STATUS_SUCCESS;
					}else{
						Object voteResult=model.get("status");
						message="\""+voteResult+"\"";
					}
				}
			}else{
				message="\"vote not found\"";
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	
	public static List<Integer[]>parseStringToArrayList(String ids){
		if(StringUtils.isNotBlank(ids)){
			List<Integer[]>li=new ArrayList<Integer[]>();
			String[] listArray=ids.split(Constants.API_LIST_SPLIT_STR);
			for(String array:listArray){
				Integer[]intArray=ArrayUtils.parseStringToArray(array);
				li.add(intArray);
			}
			return li;
		}else{
			return null;
		}
	}

	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
}

