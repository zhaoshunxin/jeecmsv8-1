package com.jeecms.cms.entity.assist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jeecms.cms.entity.assist.base.BaseCmsVoteTopic;
import com.jeecms.common.util.DateUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.Ftp;

public class CmsVoteTopic extends BaseCmsVoteTopic {
	private static final long serialVersionUID = 1L;

	public void init() {
		if (getTotalCount() == null) {
			setTotalCount(0);
		}
		if (getMultiSelect() == null) {
			setMultiSelect(1);
		}
		if (getDef() == null) {
			setDef(false);
		}
		if (getDisabled() == null) {
			setDisabled(false);
		}
		if (getRestrictMember() == null) {
			setRestrictMember(false);
		}
		if (getRestrictIp() == null) {
			setRestrictIp(false);
		}
		if (getRestrictCookie() == null) {
			setRestrictCookie(true);
		}
	}
	
	public JSONObject convertToJson(Integer https) 
			throws JSONException{
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
		JSONObject json=new JSONObject();
		json.put("id", getId());
		json.put("title", getTitle());
		json.put("description", getDescription());
		if(getStartTime()!=null){
			json.put("startTime",DateUtils.parseDateToTimeStr(getStartTime()));
		}else{
			json.put("startTime", "");
		}
		if(getEndTime()!=null){
			json.put("endTime",DateUtils.parseDateToTimeStr(getEndTime()));
		}else{
			json.put("endTime", "");
		}
		json.put("repeateHour", getRepeateHour());
		json.put("totalCount", getTotalCount());
		json.put("restrictMember", getRestrictMember());
		json.put("restrictIp", getRestrictIp());
		json.put("restrictCookie", getRestrictCookie());
		json.put("disabled", getDisabled());
		json.put("def", getDef());
		json.put("limitWeiXin", getLimitWeiXin());
		json.put("voteDay", getVoteDay());
		JSONArray subtopics=new JSONArray();
		Set<CmsVoteSubTopic> subtoics=getSubtopics();
		Iterator<CmsVoteSubTopic> subTopicIt=subtoics.iterator();
		if(subtoics!=null&&subtoics.size()>0){
			int i=0;
			while(subTopicIt.hasNext()){
				JSONObject subTopicJson=new JSONObject();
				CmsVoteSubTopic sub=subTopicIt.next();
				Set<CmsVoteItem>items=sub.getVoteItems();
				if(items!=null&&items.size()>0){
					Iterator<CmsVoteItem> itemIt=items.iterator();
					JSONArray itemsArray=new JSONArray();
					int j=0;
					while(itemIt.hasNext()){
						CmsVoteItem item=itemIt.next();
						JSONObject itemJson=new JSONObject();
						itemJson.put("id", item.getId());
						itemJson.put("percent", item.getPercent());
						itemJson.put("title", item.getTitle());
						itemJson.put("voteCount", item.getVoteCount());
						itemJson.put("priority", item.getPriority());
						if(!uploadToFtp){
							if(StringUtils.isNotBlank(item.getPicture())){
								itemJson.put("picture", urlPrefix+item.getPicture());
							}else{
								itemJson.put("picture", "");
							}
						}else{
							if(StringUtils.isNotBlank(item.getPicture())){
								itemJson.put("picture", item.getPicture());
							}else{
								itemJson.put("picture", "");
							}
						}
						itemsArray.put(j++,itemJson);
					}
					subTopicJson.put("voteItems", itemsArray);
				}else{
					subTopicJson.put("voteItems", "");
				}
				Set<CmsVoteReply>replys=sub.getVoteReplys();
				if(replys!=null&&replys.size()>0){
					Iterator<CmsVoteReply> replyIt=replys.iterator();
					JSONArray replysArray=new JSONArray();
					int j=0;
					while(replyIt.hasNext()){
						CmsVoteReply reply=replyIt.next();
						JSONObject replyJson=new JSONObject();
						replyJson.put("id", reply.getId());
						replyJson.put("reply", reply.getReply());
						replysArray.put(j++,replyJson);
					}
					subTopicJson.put("voteReplys", replysArray);
				}else{
					subTopicJson.put("voteReplys", "");
				}
				subTopicJson.put("title", sub.getTitle());
				subTopicJson.put("type", sub.getType());
				subTopicJson.put("priority", sub.getPriority());
				subTopicJson.put("id", sub.getId());
				subtopics.put(i++, subTopicJson);
			}
		}
		json.put("subtopics", subtopics);
		
		return json;
	}

	/* [CONSTRUCTOR MARKER BEGIN] */
	public CmsVoteTopic () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsVoteTopic (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsVoteTopic (
		java.lang.Integer id,
		com.jeecms.core.entity.CmsSite site,
		java.lang.String title,
		java.lang.Integer totalCount,
		java.lang.Integer multiSelect,
		java.lang.Boolean restrictMember,
		java.lang.Boolean restrictIp,
		java.lang.Boolean restrictCookie,
		java.lang.Boolean disabled,
		java.lang.Boolean def) {

		super (
			id,
			site,
			title,
			totalCount,
			multiSelect,
			restrictMember,
			restrictIp,
			restrictCookie,
			disabled,
			def);
	}
	public void addToSubTopics(CmsVoteSubTopic subTopic){
		Set<CmsVoteSubTopic>subTopics=getSubtopics();
		if(subTopics==null){
			subTopics=new HashSet<CmsVoteSubTopic>();
			setSubtopics(subTopics);
		}
		subTopics.add(subTopic);
	}

	/* [CONSTRUCTOR MARKER END] */

}