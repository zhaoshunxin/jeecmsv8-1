package com.jeecms.cms.dao.main.impl;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.jeecms.cms.dao.main.ContentChannelDao;
import com.jeecms.cms.entity.main.ContentChannel;
import com.jeecms.cms.entity.main.NamePK;
import com.jeecms.common.hibernate4.HibernateBaseDao;

@Repository
public class ContentChannelDaoImpl extends HibernateBaseDao<ContentChannel, Integer>
		implements ContentChannelDao {
	

	@Override
	protected Class<ContentChannel> getEntityClass() {
		return ContentChannel.class;
	}

	@Override
	public ContentChannel findById(Integer contentid, Integer channelid) {
		NamePK namePk = new NamePK();
		namePk.setChannelid(channelid);
		namePk.setContentid(contentid);
	    return (ContentChannel)getSession().get(ContentChannel.class, namePk);
	}
	
}