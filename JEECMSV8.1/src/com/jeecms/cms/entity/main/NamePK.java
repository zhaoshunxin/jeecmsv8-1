package com.jeecms.cms.entity.main;

import java.io.Serializable;

public class NamePK implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer contentid;
	private Integer channelid;
	public Integer getContentid() {
		return contentid;
	}
	public void setContentid(Integer contentid) {
		this.contentid = contentid;
	}
	public Integer getChannelid() {
		return channelid;
	}
	public void setChannelid(Integer channelid) {
		this.channelid = channelid;
	}
	
	
}
