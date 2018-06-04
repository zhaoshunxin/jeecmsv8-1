package com.jeecms.cms.manager.main;

import com.jeecms.cms.entity.main.ContentChannel;

public interface ContentChannelMng {
	
	public ContentChannel update(ContentChannel bean);
	
	public ContentChannel findById(Integer contentid, Integer channelid);
	
}