package com.jeecms.cms.manager.main.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jeecms.cms.dao.main.ContentChannelDao;
import com.jeecms.cms.entity.main.ContentChannel;
import com.jeecms.cms.manager.assist.CmsCommentMng;
import com.jeecms.cms.manager.main.ContentChannelMng;
import com.jeecms.cms.manager.main.ContentCheckMng;
import com.jeecms.cms.manager.main.ContentCountMng;
import com.jeecms.cms.manager.main.ContentExtMng;
import com.jeecms.cms.manager.main.ContentTagMng;
import com.jeecms.cms.manager.main.ContentTxtMng;
import com.jeecms.cms.manager.main.ContentTypeMng;
import com.jeecms.cms.service.ChannelDeleteChecker;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.core.manager.CmsGroupMng;
import com.jeecms.core.manager.CmsUserMng;

@Service
@Transactional
public class ContentChannelMngImpl implements ContentChannelMng, ChannelDeleteChecker {
		
	
	public ContentChannel update(ContentChannel bean){
		Updater<ContentChannel> updater = new Updater<ContentChannel>(bean);
		System.out.println("^^^^^^" + bean.getKjb_sort());
		System.out.println("^^^^^^^^" + updater.getBean().getKjb_sort());
		bean = dao.updateByUpdater(updater);
		return bean;
	}

	private ContentChannelDao dao;
	
	
	@Autowired
	public void setContentTypeMng(ContentTypeMng contentTypeMng) {
	}

	@Autowired
	public void setContentCountMng(ContentCountMng contentCountMng) {
	}

	@Autowired
	public void setContentExtMng(ContentExtMng contentExtMng) {
	}

	@Autowired
	public void setContentTxtMng(ContentTxtMng contentTxtMng) {
	}

	@Autowired
	public void setContentCheckMng(ContentCheckMng contentCheckMng) {
	}

	

	@Autowired
	public void setContentTagMng(ContentTagMng contentTagMng) {
	}

	@Autowired
	public void setCmsGroupMng(CmsGroupMng cmsGroupMng) {
	}

	@Autowired
	public void setCmsUserMng(CmsUserMng cmsUserMng) {
	}

	@Autowired
	public void setCmsCommentMng(CmsCommentMng cmsCommentMng) {
	}
	
	
	@Autowired
	public void setDao(ContentChannelDao dao) {
		this.dao = dao;
	}

	


	@Override
	public String checkForChannelDelete(Integer channelId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentChannel findById(Integer contentid, Integer channelid) {
		return dao.findById(contentid, channelid);
	}



	

	
}