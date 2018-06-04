package com.jeecms.cms.entity.assist;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.cms.entity.assist.base.BaseCmsAdvertising;
import com.jeecms.common.util.DateUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.Ftp;

public class CmsAdvertising extends BaseCmsAdvertising {
	private static final long serialVersionUID = 1L;

	public int getPercent() {
		if (getDisplayCount() <= 0) {
			return 0;
		} else {
			return (int) (getClickCount() * 100 / getDisplayCount());
		}
	}

	public Long getStartTimeMillis() {
		if (getStartTime() != null) {
			return getStartTime().getTime();
		} else {
			return null;
		}
	}

	public Long getEndTimeMillis() {
		if (getEndTime() != null) {
			return getEndTime().getTime();
		} else {
			return null;
		}
	}

	public void init() {
		if (getClickCount() == null) {
			setClickCount(0L);
		}
		if (getDisplayCount() == null) {
			setDisplayCount(0L);
		}
		if (getEnabled() == null) {
			setEnabled(true);
		}
		if (getWeight() == null) {
			setWeight(1);
		}
		blankToNull();
	}

	public void blankToNull() {
		if (StringUtils.isBlank(getCode())) {
			setCode(null);
		}
	}
	
	public JSONObject convertToJson(Integer https) 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("name", getName());
		json.put("category", getCategory());
		json.put("code", getCode());
		json.put("weight", getWeight());
		json.put("displayCount", getDisplayCount());
		json.put("clickCount", getClickCount());
		if(getStartTime()!=null){
			json.put("startTime", DateUtils.parseDateToDateStr(getStartTime()));
		}else{
			json.put("startTime","");
		}
		if(getEndTime()!=null){
			json.put("endTime", DateUtils.parseDateToDateStr(getEndTime()));
		}else{
			json.put("endTime","");
		}
		json.put("adspace", getAdspace().getName());
		json.put("enabled", getEnabled());
		json.put("percent", getPercent());
		CmsSite site=getSite();
		String urlPrefix="";
		if(https==com.jeecms.cms.api.Constants.URL_HTTP){
			urlPrefix=site.getUrlPrefixWithNoDefaultPort();
		}else{
			urlPrefix=site.getSafeUrlPrefix();
		}
		Ftp uploadFtp=site.getUploadFtp();
		boolean uploadToFtp=false;
		if(uploadFtp!=null){
			uploadToFtp=true;
		}
		if(getCategory().equals("image")){
			json.put("image_link", getAttr().get("image_link"));
			String imageUrl=getAttr().get("image_url");
			if(!uploadToFtp){
				if(StringUtils.isNotBlank(imageUrl)){
					json.put("image_url", urlPrefix+imageUrl);
				}else{
					json.put("image_url", "");
				}
			}else{
				if(StringUtils.isNotBlank(imageUrl)){
					json.put("image_url", imageUrl);
				}else{
					json.put("image_url", "");
				}
			}
			json.put("image_target", getAttr().get("image_target"));
			json.put("image_title", getAttr().get("image_title"));
			json.put("image_width", getAttr().get("image_width"));
			json.put("image_height", getAttr().get("image_height"));
		}else if(getCategory().equals("flash")){
			String flashUrl=getAttr().get("flash_url");
			if(!uploadToFtp){
				if(StringUtils.isNotBlank(flashUrl)){
					json.put("flash_url", urlPrefix+flashUrl);
				}else{
					json.put("flash_url", "");
				}
			}else{
				if(StringUtils.isNotBlank(flashUrl)){
					json.put("flash_url", flashUrl);
				}else{
					json.put("flash_url", "");
				}
			}
			
			json.put("flash_width", getAttr().get("flash_width"));
			json.put("flash_height", getAttr().get("flash_height"));
		}else if(getCategory().equals("text")){
			json.put("text_link", getAttr().get("text_link"));
			json.put("text_target", getAttr().get("text_target"));
			json.put("text_color", getAttr().get("text_color"));
			json.put("text_font", getAttr().get("text_font"));
			json.put("text_title", getAttr().get("text_title"));
		}
		return json;
	}

	/* [CONSTRUCTOR MARKER BEGIN] */
	public CmsAdvertising() {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsAdvertising(java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsAdvertising(java.lang.Integer id,
			com.jeecms.cms.entity.assist.CmsAdvertisingSpace adspace,
			com.jeecms.core.entity.CmsSite site, java.lang.String name,
			java.lang.String category, java.lang.Integer weight,
			java.lang.Long displayCount, java.lang.Long clickCount,
			java.lang.Boolean enabled) {

		super(id, adspace, site, name, category, weight, displayCount,
				clickCount, enabled);
	}

	/* [CONSTRUCTOR MARKER END] */

}