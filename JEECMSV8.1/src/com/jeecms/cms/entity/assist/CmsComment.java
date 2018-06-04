package com.jeecms.cms.entity.assist;

import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.cms.entity.assist.base.BaseCmsComment;
import com.jeecms.common.util.DateUtils;
import com.jeecms.common.util.StrUtils;

public class CmsComment extends BaseCmsComment {
	private static final long serialVersionUID = 1L;

	public String getText() {
		return getCommentExt().getText();
	}

	public String getTextHtml() {
		return StrUtils.txt2htm(getText());
	}

	public String getReply() {
		return getCommentExt().getReply();
	}

	public String getReplayHtml() {
		return StrUtils.txt2htm(getReply());
	}

	public String getIp() {
		return getCommentExt().getIp();
	}

	public void init() {
		short zero = 0;
		if (getDowns() == null) {
			setDowns(zero);
		}
		if (getUps() == null) {
			setUps(zero);
		}
		if(getReplyCount()==null){
			setReplyCount(0);
		}
		if (getChecked() == null) {
			setChecked(false);
		}
		if (getRecommend() == null) {
			setRecommend(false);
		}
		if (getCreateTime() == null) {
			setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
	}
	
	public JSONObject convertToJson() 
			throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("createTime", DateUtils.parseDateToTimeStr(getCreateTime()));
		if(getReplayTime()!=null){
			json.put("replayTime",DateUtils.parseDateToTimeStr(getReplayTime()));
		}else{
			json.put("replayTime", "");
		}
		json.put("ups", getUps());
		json.put("downs", getDowns());
		json.put("recommend", getRecommend());
		json.put("checked", getChecked());
		json.put("replyCount", getReplyCount());
		if(getReplayUser()!=null){
			json.put("replayUser", getReplayUser().getUsername());
		}else{
			json.put("replayUser", "");
		}
		if(getCommentUser()!=null){
			json.put("commentUser", getCommentUser().getUsername());
		}else{
			json.put("commentUser", "");
		}
		json.put("ip", getIp());
		json.put("text", getText());
		json.put("reply", getReply());
		json.put("score", getScore());
		if(getParent()!=null){
			json.put("parentId", getParent().getId());
		}else{
			json.put("parentId", "");
		}
		json.put("contentId", getContent().getId());
		json.put("contentTitle", getContent().getTitle());
		json.put("channelId", getContent().getChannel().getId());
		return json;
	}

	/* [CONSTRUCTOR MARKER BEGIN] */
	public CmsComment () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsComment (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsComment (
		java.lang.Integer id,
		com.jeecms.cms.entity.main.Content content,
		com.jeecms.core.entity.CmsSite site,
		java.util.Date createTime,
		java.lang.Short ups,
		java.lang.Short downs,
		java.lang.Boolean recommend,
		java.lang.Boolean checked) {

		super (
			id,
			content,
			site,
			createTime,
			ups,
			downs,
			recommend,
			checked);
	}

	/* [CONSTRUCTOR MARKER END] */

}