package com.jeecms.cms.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsAdvertising;
import com.jeecms.cms.entity.assist.CmsAdvertisingSpace;
import com.jeecms.cms.manager.assist.CmsAdvertisingMng;
import com.jeecms.cms.manager.assist.CmsAdvertisingSpaceMng;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsAdApiAct {
	
	/**
	 * @param siteId 站点ID
	 * @param ctgId 分类ID
	 * @param enabled 是否启用
	 * @param first 开始
	 * @param count 数量
	 */
	@RequestMapping(value = "/api/ad/list.jspx")
	public void adList(Integer https,Integer adspaceId,Boolean enabled,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if (enabled == null) {
			enabled = true;
		}
		if(https==null){
			https=Constants.URL_HTTP;
		}
		List<CmsAdvertising> list = advertisingMng.getList(adspaceId, enabled);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, list.get(i).convertToJson(https));
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * 获取广告信息
	 * @param id 广告ID
	 */
	@RequestMapping(value = "/api/ad/get.jspx")
	public void adGet(Integer https,Integer id,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(https==null){
			https=Constants.URL_HTTP;
		}
		CmsAdvertising ad = advertisingMng.findById(id);
		if(ad!=null){
			ResponseUtils.renderJson(response, ad.convertToJson(https).toString());
		}else{
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	/**
	 * 广告版块
	 * @param siteId 站点ID
	 */
	@RequestMapping(value = "/api/adctg/list.jspx")
	public void adCtgList(Integer siteId,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(siteId==null){
			siteId=CmsUtils.getSiteId(request);
		}
		List<CmsAdvertisingSpace> list = advertisingSpaceMng.getList(siteId);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, list.get(i).convertToJson());
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	@Autowired
	private CmsAdvertisingSpaceMng advertisingSpaceMng;
	@Autowired
	private CmsAdvertisingMng advertisingMng;
}

