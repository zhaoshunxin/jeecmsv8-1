package com.jeecms.cms.dao.main.impl;

import static com.jeecms.cms.entity.main.Content.ContentStatus.all;
import static com.jeecms.cms.entity.main.Content.ContentStatus.checked;
import static com.jeecms.cms.entity.main.Content.ContentStatus.draft;
import static com.jeecms.cms.entity.main.Content.ContentStatus.passed;
import static com.jeecms.cms.entity.main.Content.ContentStatus.prepared;
import static com.jeecms.cms.entity.main.Content.ContentStatus.recycle;
import static com.jeecms.cms.entity.main.Content.ContentStatus.rejected;
import static com.jeecms.cms.entity.main.Content.ContentStatus.contribute;
import static com.jeecms.cms.entity.main.Content.ContentStatus.pigeonhole;

import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_START;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_END;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_LIKE;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_IN;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_EQ;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_GT;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_GTE;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_LT;
import static com.jeecms.cms.action.directive.abs.AbstractContentDirective.PARAM_ATTR_LTE;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jeecms.cms.dao.main.ChannelDao;
import com.jeecms.cms.dao.main.ContentDao;
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentCheck;
import com.jeecms.cms.entity.main.ContentDoc;
import com.jeecms.cms.entity.main.Content.ContentStatus;
import com.jeecms.cms.service.ContentQueryFreshTimeCache;
import com.jeecms.common.hibernate4.Finder;
import com.jeecms.common.hibernate4.HibernateBaseDao;
import com.jeecms.common.page.Pagination;
import com.jeecms.core.dao.CmsDictionaryDao;
import com.jeecms.core.entity.CmsDictionary;

@Repository
public class ContentDaoImpl extends HibernateBaseDao<Content, Integer>
		implements ContentDao {
	@Autowired
	private CmsDictionaryDao dicDao;
	@Autowired
	private ChannelDao channelDao;
	private String sql_content_base="select content0_.content_id, content0_.sort_date , jc_content_channel.sort as top_level , content0_.has_title_img , content0_.is_recommend, content0_.status, content0_.views_day , content0_.comments_day , content0_.downloads_day, content0_.ups_day , content0_.recommend_level , content0_.score, content0_.type_id , content0_.site_id, content0_.user_id, content0_.channel_id, content0_.model_id"
			+ " from jc_content content0_ "
			+ " inner join jc_content_ext on content0_.content_id = jc_content_ext.content_id"
			+ " inner join jc_content_channel on content0_.content_id = jc_content_channel.content_id"
			+ " inner join jc_channel channel on jc_content_channel.channel_id = channel.channel_id";
			//+ " left join jc_content_attr on content0_.content_id = jc_content_attr.content_id";
	
	public Pagination getPage(Integer share,String title, Integer typeId,Integer currUserId,
			Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,Integer modelId,
			Integer channelId,int orderBy, int pageNo, int pageSize) {
		return getPageData(Content.QUERY_DATA, share, title, typeId, 
				currUserId, inputUserId, topLevel, recommend, status,
				checkStep, siteId, modelId, channelId, orderBy, pageNo, pageSize);
	}
	
	public Pagination getPageCount(Integer share,String title, Integer typeId,Integer currUserId,
			Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,Integer modelId,
			Integer channelId,int orderBy, int pageNo, int pageSize) {
		return getPageData(Content.QUERY_PAGE, share, title, typeId, 
				currUserId, inputUserId, topLevel, recommend, status,
				checkStep, siteId, modelId, channelId, orderBy, pageNo, pageSize);
	}

	//只能管理自己的数据不能审核他人信息，工作流相关表无需查询
	public Pagination getPageBySelf(Integer share,String title, Integer typeId,
			Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,
			Integer channelId, Integer userId, int orderBy, int pageNo,
			int pageSize) {
		return getPageDataBySelf(Content.QUERY_DATA, share, title, typeId, inputUserId,
				topLevel, recommend, status, checkStep, siteId, channelId,
				userId, orderBy, pageNo, pageSize);
	}
	
	public Pagination getPageCountBySelf(Integer share,String title, Integer typeId,
			Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,
			Integer channelId, Integer userId, int orderBy, int pageNo,
			int pageSize) {
		return getPageDataBySelf(Content.QUERY_PAGE, share, title, typeId, inputUserId,
				topLevel, recommend, status, checkStep, siteId, channelId,
				userId, orderBy, pageNo, pageSize);
	}

	public Pagination getPageByRight(Integer share,String title, Integer typeId,
			Integer currUserId,Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,
			Integer channelId, Integer departId, Integer userId, boolean selfData,
			int orderBy,int pageNo, int pageSize) {
		return getPageDataByRight(Content.QUERY_DATA,share, title, typeId, currUserId,
				inputUserId, topLevel, recommend, status, checkStep,
				siteId, channelId, departId, userId, selfData,
				orderBy, pageNo, pageSize);
	}
	
	public Pagination getPageCountByRight(Integer share,String title, Integer typeId,
			Integer currUserId,Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,
			Integer channelId, Integer departId, Integer userId, boolean selfData,
			int orderBy,int pageNo, int pageSize) {
		return getPageDataByRight(Content.QUERY_PAGE,share, title, typeId, currUserId,
				inputUserId, topLevel, recommend, status, checkStep,
				siteId, channelId, departId, userId, selfData,
				orderBy, pageNo, pageSize);
	}
	
	public List<Content> getList(String title, Integer typeId,Integer currUserId,
			Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,Integer modelId,
			Integer channelId,int orderBy, int first, int count) {
		Finder f = Finder.create("select  bean from Content bean ");
		if (rejected == status) {
			f.append("  join bean.contentCheckSet check ");
		}
		if (prepared == status|| passed == status) {
			f.append("  join bean.eventSet event  ");
		}
		if (channelId != null) {
			f.append(" join bean.channel channel,Channel parent");
			f.append(" where (channel.lft between parent.lft and parent.rgt");
			f.append(" and channel.site.id=parent.site.id");
			f.append(" and parent.id=:parentId)");
			f.setParam("parentId", channelId);
		} else if (siteId != null) {
			f.append(" where (bean.site.id=:siteId)");
			f.setParam("siteId", siteId);
		} else {
			f.append(" where 1=1");
		}
		//跳级审核人不应该看到？
		if (passed == status) {
			//操作人不在待审人列表中且非终审 或非发起人
			f.append("  and ((:operateId not in(select eventUser.user.id from CmsWorkflowEventUser eventUser where eventUser.event.id=event.id) and event.initiator.id!=:operateId) or event.initiator.id=:operateId) and event.nextStep!=-1").setParam("operateId", currUserId);
		}
		if (prepared == status) {
			//操作人在待审人列表中
			f.append("  and :operateId in(select eventUser.user.id from CmsWorkflowEventUser eventUser where eventUser.event.id=event.id)").setParam("operateId", currUserId);
		}
		if (rejected == status) {
			f.append(" and check.rejected=true");
		}
		if(modelId!=null){
			f.append(" and bean.model.id=:modelId").setParam("modelId", modelId);
		}
		appendQuery(f, title, typeId, inputUserId, status, topLevel, recommend);
		appendOrder(f, orderBy);
		f.setFirstResult(first);
		f.setMaxResults(count);
		return find(f);
	}
	
	private Pagination getPageData(Integer queryMode,Integer share,String title,
			Integer typeId,Integer currUserId,Integer inputUserId, 
			boolean topLevel, boolean recommend,ContentStatus status, 
			Byte checkStep, Integer siteId,Integer modelId,
			Integer channelId,int orderBy, int pageNo, int pageSize){
		Finder f = Finder.create("select bean from Content bean ");
		//kjb
		StringBuilder sql_builder = new StringBuilder("select content0_.content_id, content0_.sort_date , jc_content_channel.sort as top_level , content0_.has_title_img , content0_.is_recommend, content0_.status, content0_.views_day , content0_.comments_day , content0_.downloads_day, content0_.ups_day , content0_.recommend_level , content0_.score, content0_.type_id , content0_.site_id, content0_.user_id, content0_.channel_id, content0_.model_id");
		sql_builder.append(" from jc_content content0_ ");
		//
		if (rejected == status) {
			f.append("  join bean.contentCheckSet check ");
			sql_builder.append(" inner join jc_content_check check on jc_content.content_id = check.content_id");
		}
		if (prepared == status|| passed == status) {
			f.append("  join bean.eventSet event  ");
			
		}
		if (channelId != null) {
			//共享内容
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" join bean.channels channel,Channel parent");
				f.append(" where (channel.lft between parent.lft and parent.rgt");
				f.append(" and channel.site.id=parent.site.id");
				f.append(" and parent.id=:parentId and bean.site.id!=:siteId)");
				f.setParam("parentId", channelId).setParam("siteId", siteId);
			}else{
				f.append(" join bean.channel channel,Channel parent");
				f.append(" where (channel.lft between parent.lft and parent.rgt");
				f.append(" and channel.site.id=parent.site.id");
				f.append(" and parent.id=:parentId)");
				f.setParam("parentId", channelId);
				
				//kjb
				sql_builder.append(" inner join jc_content_ext on content0_.content_id = jc_content_ext.content_id");
				sql_builder.append(" inner join jc_content_channel on content0_.content_id = jc_content_channel.content_id");
				sql_builder.append(" inner join jc_channel channel on jc_content_channel.channel_id = channel.channel_id");
				if((470 <= channelId && channelId <= 472)||(501 <= channelId && channelId <= 509)||(516 <= channelId && channelId <= 517)){
					sql_builder.append(" left join jc_content_attr on content0_.content_id = jc_content_attr.content_id");
					sql_builder.append(" where");
					sql_builder.append(" jc_content_attr.attr_name='department' and jc_content_attr.attr_value=:channelName");
				}else if((562 <= channelId && channelId <= 563)||(569 <= channelId && channelId <= 575)||(577 <= channelId && channelId <= 599)){
					sql_builder.append(" inner join jc_content_attr on content0_.content_id = jc_content_attr.content_id");
					sql_builder.append(" where");
					sql_builder.append(" jc_content_attr.attr_name='province' and jc_content_attr.attr_value=:channel_NameProvince");
				}else{
					sql_builder.append(" cross join jc_channel parent");
					sql_builder.append(" where");
					sql_builder.append(" (channel.lft between parent.lft and parent.rgt)");
					sql_builder.append(" and channel.site_id=parent.site_id");
					sql_builder.append(" and parent.channel_id=:channelId");
				}
				//
			}
		} else if (siteId != null) {
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" join bean.channels channel ");
				f.append(" where (bean.site.id!=:siteId and channel.site.id=:siteId)");
				f.setParam("siteId", siteId);
			}else{
				f.append(" where bean.site.id=:siteId");
				f.setParam("siteId", siteId);
				//kjb
				sql_builder.append(" inner join jc_content_ext on content0_.content_id = jc_content_ext.content_id");
				sql_builder.append(" inner join jc_content_channel on content0_.content_id = jc_content_channel.content_id");
				sql_builder.append(" where content0_.site_id=:siteId");
				//
			}
		} else {
			f.append(" where 1=1");
			//kjb
			sql_builder.append(" where 1 = 1");
			//
		}
		//跳级审核人不应该看到？
		if (passed == status) {
			//操作人不在待审人列表中且非终审 或非发起人
			f.append("  and ((:operateId not in(select eventUser.user.id from CmsWorkflowEventUser eventUser where eventUser.event.id=event.id) and event.initiator.id!=:operateId) or event.initiator.id=:operateId) and event.nextStep!=-1").setParam("operateId", currUserId);
		}
		if (prepared == status) {
			//操作人在待审人列表中
			f.append("  and :operateId in(select eventUser.user.id from CmsWorkflowEventUser eventUser where eventUser.event.id=event.id)").setParam("operateId", currUserId);
		}
		if (rejected == status) {
			f.append(" and check.rejected=true");
			//kjb
			sql_builder.append(" and check.is_rejected = '1'");
			//
		}
		if(modelId!=null){
			f.append(" and bean.model.id=:modelId").setParam("modelId", modelId);
			//kjb
			sql_builder.append(" and jc_content.model_id = :modelId");
			//
		}
		f.setCacheable(true);
		appendQuery(f, title, typeId, inputUserId, status, topLevel, recommend);
		appendOrder(f, orderBy);
		//kjb
		appendQuery_sql(sql_builder, title, typeId, inputUserId, status, topLevel, recommend);
		appendOrder_sql(sql_builder, orderBy);
		//sql_builder.append(" order by jc_content_channel.sort");
		String sql = sql_builder.toString();
		String sql_count = "select count(1) " + sql.substring(sql.indexOf("from"));
		getSession().flush();
		SQLQuery query = getSession().createSQLQuery(sql);
		SQLQuery query_count = getSession().createSQLQuery(sql_count);
		if(sql.indexOf(":siteId") > 0){
			query.setParameter("siteId", siteId);
			query_count.setParameter("siteId", siteId);
		}
		if(sql.indexOf(":channelId") > 0){
			query.setParameter("channelId", channelId);
			query_count.setParameter("channelId", channelId);
		}
		if(sql.indexOf(":checkStep") > 0){
			query.setParameter("checkStep", checkStep);
			query_count.setParameter("checkStep", checkStep);
		}
		if(sql.indexOf(":modelId") > 0){
			query.setParameter("modelId", modelId);
			query_count.setParameter("modelId", modelId);
		}
		if(sql.indexOf(":title") > 0){
			query.setParameter("title", "%" + title + "%");
			query_count.setParameter("title", "%" + title + "%");
		}
		if(sql.indexOf(":typeId") > 0){
			query.setParameter("typeId", typeId);
			query_count.setParameter("typeId", typeId);
		}
		if(sql.indexOf(":inputUserId") > 0){
			query.setParameter("inputUserId", inputUserId);
			query_count.setParameter("inputUserId", inputUserId);
		}
		if(sql.indexOf(":channelName") > 0){
			List<CmsDictionary> list = dicDao.getList("司局");
			com.jeecms.cms.entity.main.Channel c = channelDao.findById(channelId);
			Iterator<CmsDictionary> it = list.iterator();
			query.setParameter("channelName", "");
			query_count.setParameter("channelName", "");
			while(it.hasNext()){
				CmsDictionary dic = (CmsDictionary) it.next();
				if(dic.getName().equals(c.getName())){
					query.setParameter("channelName", dic.getValue());
					query_count.setParameter("channelName", dic.getValue());
					break;
				}
			}
		}
		if(sql.indexOf(":channel_NameProvince") > 0){
			com.jeecms.cms.entity.main.Channel c = channelDao.findById(channelId);
			query.setParameter("channel_NameProvince", c.getName());
			query_count.setParameter("channel_NameProvince", c.getName());
		}
		if(sql.indexOf(":status") > 0){
			if (draft == status) {
				query.setParameter("status", ContentCheck.DRAFT);
				query_count.setParameter("status", ContentCheck.DRAFT);
			}if (contribute == status) {
				query.setParameter("status", ContentCheck.CONTRIBUTE);
				query_count.setParameter("status", ContentCheck.CONTRIBUTE);
			} else if (checked == status) {
				query.setParameter("status", ContentCheck.CHECKED);
				query_count.setParameter("status", ContentCheck.CHECKED);
			} else if (prepared == status ) {
				query.setParameter("status", ContentCheck.CHECKING);
				query_count.setParameter("status", ContentCheck.CHECKING);
			} else if (rejected == status ) {
				query.setParameter("status", ContentCheck.REJECT);
				query_count.setParameter("status", ContentCheck.REJECT);
			}else if (passed == status) {
				query.setParameter("checking", ContentCheck.CHECKING);
				query.setParameter("checked", ContentCheck.CHECKED);
				query_count.setParameter("checking", ContentCheck.CHECKING);
				query_count.setParameter("checked", ContentCheck.CHECKED);
			} else if (all == status) {
				query.setParameter("status", ContentCheck.RECYCLE);
				query_count.setParameter("status", ContentCheck.RECYCLE);
			} else if (recycle == status) {
				query.setParameter("status", ContentCheck.RECYCLE);
				query_count.setParameter("status", ContentCheck.RECYCLE);
			} else if (pigeonhole == status) {
				query.setParameter("status", ContentCheck.PIGEONHOLE);
				query_count.setParameter("status", ContentCheck.PIGEONHOLE);
			} else {
				// never
			}
		}
		
		query.addEntity(Content.class);
		query.setCacheable(false);
		f.setCacheable(false);
		//
		/*if(queryMode!=null&&queryMode.equals(Content.QUERY_PAGE)){
			return findBigDataPage(f, pageNo, pageSize);
		}else{
			return findBigData(f, pageNo, pageSize);
		}*/
		return findBigData_flm2(query, pageNo, pageSize, query_count);
	}
	
	private Pagination getPageDataBySelf(Integer queryMode,Integer share,String title, Integer typeId,
			Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,
			Integer channelId, Integer userId, int orderBy, int pageNo,
			int pageSize){
		Finder f = Finder.create("select  bean from Content bean");
		if (prepared == status || passed == status || rejected == status) {
			f.append(" join bean.contentCheckSet check");
		}
		if (channelId != null) {
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" join bean.channels channel,Channel parent");
				f.append(" where channel.lft between parent.lft and parent.rgt");
				f.append(" and channel.site.id=parent.site.id");
				f.append(" and parent.id=:parentId and bean.site.id!=:siteId");
				f.setParam("parentId", channelId);
			}else{
				f.append(" join bean.channel channel,Channel parent");
				f.append(" where channel.lft between parent.lft and parent.rgt");
				f.append(" and channel.site.id=parent.site.id");
				f.append(" and parent.id=:parentId");
				f.setParam("parentId", channelId);
			}
		}else if (siteId != null) {
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" join bean.channels channel ");
				f.append(" where bean.site.id!=:siteId and channel.site.id=:siteId");
				f.setParam("siteId", siteId);
			}else{
				f.append(" where bean.site.id=:siteId");
				f.setParam("siteId", siteId);
			}
		} else {
			f.append(" where 1=1");
		}
		f.append(" and bean.user.id=:userId");
		f.setParam("userId", userId);
		if (prepared == status) {
			f.append(" and check.checkStep<:checkStep");
			f.append(" and check.rejected=false");
			f.setParam("checkStep", checkStep);
		} else if (passed == status) {
			f.append(" and check.checkStep=:checkStep");
			f.append(" and check.rejected=false");
			f.setParam("checkStep", checkStep);
		} else if (rejected == status) {
			f.append(" and check.checkStep=:checkStep");
			f.append(" and check.rejected=true");
			f.setParam("checkStep", checkStep);
		}
		appendQuery(f, title, typeId, inputUserId, status, topLevel, recommend);
		if (prepared == status) {
			f.append(" order by check.checkStep desc,bean.id desc");
		} else {
			appendOrder(f, orderBy);
		}
		f.setCacheable(true);
		if(queryMode!=null&&queryMode.equals(Content.QUERY_PAGE)){
			return findBigDataPage(f, pageNo, pageSize);
		}else{
			return findBigData(f, pageNo, pageSize);
		}
	}
	
	private Pagination getPageDataByRight(Integer queryMode,
			Integer share,String title, Integer typeId,
			Integer currUserId,Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,
			Integer channelId, Integer departId, Integer userId, boolean selfData,
			int orderBy,int pageNo, int pageSize){
		Finder f = Finder.create("select  bean from Content bean ");
		if (rejected == status) {
			f.append("  join bean.contentCheckSet check ");
		}
		if (prepared == status|| passed == status) {
			f.append("  join bean.eventSet event  ");
		}
		if (channelId != null) {
			//共享内容
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" join bean.channels channel ");
			}else{
				f.append(" join bean.channel channel ");
			}
			if(departId!=null){
				f.append("left join channel.departments depart");
			}
			f.append(",Channel parent");
			f.append(" where (channel.lft between parent.lft and parent.rgt");
			f.append(" and channel.site.id=parent.site.id");
			f.append(" and parent.id=:parentId");
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" and bean.site.id!=:siteId and channel.site.id=:siteId").setParam("siteId", siteId);
			}
			if(departId!=null){
				f.append(" and depart.id =:departId").setParam("departId", departId);
			}
			f.append(")" );
			f.setParam("parentId", channelId);
		} else if (siteId != null) {
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" join bean.channels channel ");
			}else{
				f.append(" join bean.channel channel ");
			}
			if(departId!=null){
				f.append(" left join channel.departments depart");
				f.append(" where depart.id  =:departId").setParam("departId", departId);
			}else{
				f.append(" where 1=1 ");
			}
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" and bean.site.id!=:siteId and channel.site.id=:siteId");
				f.setParam("siteId", siteId);
			}else{
				f.append(" and bean.site.id=:siteId").setParam("siteId", siteId);
			}
		} else {
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" join bean.channels channel ");
			}else{
				f.append(" join bean.channel channel ");
			}
			if(departId!=null){
				f.append(" left join channel.departments depart");
				f.append(" where depart.id  =:departId").setParam("departId", departId);
			}else{
				f.append(" where 1=1 ");
			}
			if(share!=null&&share.equals(Content.CONTENT_QUERY_SHARE)){
				f.append(" and bean.site.id!=channel.site.id");
			}else{
				f.append(" and bean.site.id=channel.site.id");
			}
		}
		if (selfData) {
			// userId前面已赋值
			f.append(" and bean.user.id=:userId");
			f.setParam("userId", userId);
		}
		//跳级审核人不应该看到？
		if (passed == status) {
			//操作人不在待审人列表中且非终审 或非发起人
			f.append("  and ((:operateId not in(select eventUser.user.id from CmsWorkflowEventUser eventUser where eventUser.event.id=event.id) and event.initiator.id!=:operateId) or event.initiator.id=:operateId) and event.nextStep!=-1").setParam("operateId", currUserId);
		}
		if (prepared == status) {
			//操作人在待审人列表中
			f.append("  and :operateId in(select eventUser.user.id from CmsWorkflowEventUser eventUser where eventUser.event.id=event.id)").setParam("operateId", currUserId);
		}
		if (rejected == status) {
			f.append(" and check.rejected=true");
		}
		appendQuery(f, title, typeId, inputUserId, status, topLevel, recommend);
		appendOrder(f, orderBy);
		f.setCacheable(true);
		if(queryMode!=null&&queryMode.equals(Content.QUERY_PAGE)){
			return findBigDataPage(f, pageNo, pageSize);
		}else{
			return findBigData(f, pageNo, pageSize);
		}
	}
	

	public Pagination getPageForCollection(Integer siteId, Integer memberId, int pageNo, int pageSize){
		Finder f=createCollectFinder(siteId, memberId);
		return find(f, pageNo, pageSize);
	}
	
	public List<Content> getListForCollection(Integer siteId, Integer memberId, 
			Integer first, Integer count){
		Finder f=createCollectFinder(siteId, memberId);
		if(first!=null){
			f.setFirstResult(first);
		}
		if(count!=null){
			f.setMaxResults(count);
		}
		return find(f);
	}
	
	@SuppressWarnings("unchecked")
	public  List<Content> getExpiredTopLevelContents(byte topLevel,Date expiredDay){
		String hql = "from  Content bean where bean.status=:status and bean.topLevel>:topLevel and bean.contentExt.topLevelDate<:topLevelDate";
		Finder f=Finder.create(hql).setParam("status", ContentCheck.CHECKED).setParam("topLevel", topLevel).setParam("topLevelDate", expiredDay);
		return find(f);
	}
	
	@SuppressWarnings("unchecked")
	public  List<Content> getPigeonholeContents(Date pigeonholeDay){
		String hql = "from  Content bean where bean.status=:status and bean.contentExt.pigeonholeDate<:pigeonholeDate";
		Finder f=Finder.create(hql).setParam("status", ContentCheck.CHECKED).setParam("pigeonholeDate", pigeonholeDay);
		return find(f);
	}

	private void appendQuery(Finder f, String title, Integer typeId,
			Integer inputUserId, ContentStatus status, boolean topLevel,
			boolean recommend) {
		if (!StringUtils.isBlank(title)) {
			f.append(" and bean.contentExt.title like :title  escape '/'");
			title=title.replaceAll("%","/%");
			title=title.replaceAll("_","/_");
			f.setParam("title", "%" + title + "%");
		}
		if (typeId != null) {
			f.append(" and bean.type.id=:typeId");
			f.setParam("typeId", typeId);
		}
		if (inputUserId != null&&inputUserId!=0) {
			f.append(" and bean.user.id=:inputUserId");
			f.setParam("inputUserId", inputUserId);
		}else{
			//输入了没有的用户名的情况
			if(inputUserId==null){
				f.append(" and 1!=1");
			}
		}
		if (topLevel) {
			f.append(" and bean.topLevel>0");
		}
		if (recommend) {
			f.append(" and bean.recommend=true");
		}
		if (draft == status) {
			f.append(" and bean.status=:status");
			f.setParam("status", ContentCheck.DRAFT);
		}if (contribute == status) {
			f.append(" and bean.status=:status");
			f.setParam("status", ContentCheck.CONTRIBUTE);
		} else if (checked == status) {
			f.append(" and bean.status=:status");
			f.setParam("status", ContentCheck.CHECKED);
		} else if (prepared == status ) {
			f.append(" and bean.status=:status");
			f.setParam("status", ContentCheck.CHECKING);
		} else if (rejected == status ) {
			f.append(" and bean.status=:status");
			f.setParam("status", ContentCheck.REJECT);
		}else if (passed == status) {
			f.append(" and (bean.status=:checking or bean.status=:checked)");
			f.setParam("checking", ContentCheck.CHECKING);
			f.setParam("checked", ContentCheck.CHECKED);
		} else if (all == status) {
			f.append(" and bean.status<>:status");
			f.setParam("status", ContentCheck.RECYCLE);
		} else if (recycle == status) {
			f.append(" and bean.status=:status");
			f.setParam("status", ContentCheck.RECYCLE);
		} else if (pigeonhole == status) {
			f.append(" and bean.status=:status");
			f.setParam("status", ContentCheck.PIGEONHOLE);
		} else {
			// never
		}
	}
	

	public Content getSide(Integer id, Integer siteId, Integer channelId,
			boolean next, boolean cacheable) {
		Finder f = Finder.create("from Content bean where 1=1");
		if (channelId != null) {
			f.append(" and bean.channel.id=:channelId");
			f.setParam("channelId", channelId);
		} else if (siteId != null) {
			f.append(" and bean.site.id=:siteId");
			f.setParam("siteId", siteId);
		}
		if (next) {
			f.append(" and bean.id>:id");
			f.setParam("id", id);
			f.append(" and bean.status=" + ContentCheck.CHECKED);
			f.append(" order by bean.id asc");
		} else {
			f.append(" and bean.id<:id");
			f.setParam("id", id);
			f.append(" and bean.status=" + ContentCheck.CHECKED);
			f.append(" order by bean.id desc");
		}
		Query query = f.createQuery(getSession());
		query.setCacheable(cacheable).setMaxResults(1);
		return (Content) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<Content> getListByIdsForTag(Integer[] ids, int orderBy) {
		Finder f = Finder.create("from Content bean where bean.id in (:ids)");
		f.setParamList("ids", ids);
		appendOrder(f, orderBy);
		f.setCacheable(true);
		return find(f);
	}

	public Pagination getPageBySiteIdsForTag(Integer[] siteIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title, int open,Map<String,String[]>attr,int orderBy, int pageNo, int pageSize) {
		Finder f = bySiteIds(siteIds, typeIds, titleImg, recommend, title,
				open,attr,orderBy);
		f.setCacheable(true);
		return find(f, pageNo, pageSize);
	}

	@SuppressWarnings("unchecked")
	public List<Content> getListBySiteIdsForTag(Integer[] siteIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title,int open,Map<String,String[]>attr, int orderBy, Integer first, Integer count) {
		Finder f = bySiteIds(siteIds, typeIds, titleImg, recommend, title,open,attr,
				orderBy);
		if (first != null) {
			f.setFirstResult(first);
		}
		if (count != null) {
			f.setMaxResults(count);
		}
		f.setCacheable(true);
		return find(f);
	}

	public Pagination getPageByChannelIdsForTag(Integer[] channelIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title,int open, Map<String,String[]>attr, int orderBy, int option,int pageNo, int pageSize) {
		/*Finder f = byChannelIds(channelIds, typeIds, titleImg, recommend,
				title, open,attr,orderBy, option);
		f.setCacheable(true);
		return find(f, pageNo, pageSize);*/
		SQLQuery query = byChannelIds_sql(channelIds, typeIds, titleImg, recommend,
				title,attr,orderBy, option);	
		query.addEntity(Content.class);
		int totalCount = query.list().size();
		Pagination p = null;
		if(channelIds[0] == 188){
			p = new Pagination();
			p.setPageNo(pageNo);
			p.setTotalCount(totalCount);
			p.setPageSize(pageSize);
		}else{
			p = new Pagination(pageNo, pageSize, totalCount);
		}
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		if(channelIds[0]==188){
			Channel c = channelDao.findById(188);
			int yw_num = Integer.parseInt(c.getAttr().get("display_yw_num"));
			if(pageNo==1){
				query.setFirstResult(0);
				query.setMaxResults(yw_num);
				System.out.println("yw_num: " + yw_num);
			}else{
				query.setFirstResult(yw_num + (pageNo-2) * pageSize);
				query.setMaxResults(pageSize);
				System.out.println("yw_num2: " + yw_num);
			}
			if((totalCount-yw_num)%pageSize != 0){
				p.setTotalPage((totalCount-yw_num)/pageSize + 2);
			}else{
				p.setTotalPage((totalCount-yw_num)/pageSize + 1);
			}
			p.setTotalCount(totalCount + pageSize - yw_num);
		}else{
			query.setFirstResult(p.getFirstResult());
			query.setMaxResults(p.getPageSize());
		}
		List list = query.list();
		p.setList(list);
		return p;
	}

	@SuppressWarnings("unchecked")
	public List<Content> getListByChannelIdsForTag(Integer[] channelIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title,int open,Map<String,String[]>attr, int orderBy, int option, Integer first, Integer count) {
		/*Finder f = byChannelIds(channelIds, typeIds, titleImg, recommend,
				title, open,attr,orderBy, option);
		if (first != null) {
			f.setFirstResult(first);
		}
		if (count != null) {
			f.setMaxResults(count);
		}
		f.setCacheable(true);
		return find(f);*/
		SQLQuery query = byChannelIds_sql(channelIds, typeIds, titleImg, recommend,
				title,attr,orderBy, option);
		if (first != null) {
			query.setFirstResult(first);
		}
		if (count != null) {
			query.setMaxResults(count);
		}
		query.addEntity(Content.class);
		return query.list();
	}

	public Pagination getPageByChannelPathsForTag(String[] paths,
			Integer[] siteIds, Integer[] typeIds, Boolean titleImg,
			Boolean recommend, String title, int open,Map<String,String[]>attr,int orderBy, int pageNo,
			int pageSize) {
		Finder f = byChannelPaths(paths, siteIds, typeIds, titleImg, recommend,
				title,open,attr,orderBy);
		f.setCacheable(true);
		return find(f, pageNo, pageSize);
	}

	@SuppressWarnings("unchecked")
	public List<Content> getListByChannelPathsForTag(String[] paths,
			Integer[] siteIds, Integer[] typeIds, Boolean titleImg,
			Boolean recommend, String title,int open,Map<String,String[]>attr, int orderBy, Integer first,
			Integer count) {
		Finder f = byChannelPaths(paths, siteIds, typeIds, titleImg, recommend,
				title, open,attr,orderBy);
		if (first != null) {
			f.setFirstResult(first);
		}
		if (count != null) {
			f.setMaxResults(count);
		}
		f.setCacheable(true);
		return find(f);
	}

	public Pagination getPageByTopicIdForTag(Integer topicId,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Boolean titleImg, Boolean recommend, String title, int open,Map<String,String[]>attr,int orderBy,
			int pageNo, int pageSize) {
		Finder f = byTopicId(topicId, siteIds, channelIds, typeIds, titleImg,
				recommend, title, open,attr,orderBy);
		f.setCacheable(true);
		return find(f, pageNo, pageSize);
	}

	@SuppressWarnings("unchecked")
	public List<Content> getListByTopicIdForTag(Integer topicId,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Boolean titleImg, Boolean recommend, String title,int open,Map<String,String[]>attr, int orderBy,
			Integer first, Integer count) {
		Finder f = byTopicId(topicId, siteIds, channelIds, typeIds, titleImg,
				recommend, title,open, attr,orderBy);
		if (first != null) {
			f.setFirstResult(first);
		}
		if (count != null) {
			f.setMaxResults(count);
		}
		f.setCacheable(true);
		return find(f);
	}

	public Pagination getPageByTagIdsForTag(Integer[] tagIds,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Integer excludeId, Boolean titleImg, Boolean recommend,
			String title,int open, Map<String,String[]>attr,int orderBy, int pageNo, int pageSize) {
		Finder f = byTagIds(tagIds, siteIds, channelIds, typeIds, excludeId,
				titleImg, recommend, title,open,attr, orderBy);
		f.setCacheable(true);
		return find(f, pageNo, pageSize);
	}

	@SuppressWarnings("unchecked")
	public List<Content> getListByTagIdsForTag(Integer[] tagIds,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Integer excludeId, Boolean titleImg, Boolean recommend,
			String title,int open,Map<String,String[]>attr, int orderBy, Integer first, Integer count) {
		Finder f = byTagIds(tagIds, siteIds, channelIds, typeIds, excludeId,
				titleImg, recommend, title, open,attr,orderBy);
		if (first != null) {
			f.setFirstResult(first);
		}
		if (count != null) {
			f.setMaxResults(count);
		}
		f.setCacheable(true);
		return find(f);
	}

	private Finder bySiteIds(Integer[] siteIds, Integer[] typeIds,
			Boolean titleImg, Boolean recommend, String title, int open,Map<String,String[]>attr,int orderBy) {
		Finder f = Finder.create("select  bean from Content bean where 1=1");
		appendJoinConentDoc(f, open);
		//f.append(" join bean.contentExt as ext where 1=1");
		if (titleImg != null) {
			f.append(" and bean.hasTitleImg=:titleImg");
			f.setParam("titleImg", titleImg);
		}
		if (recommend != null) {
			f.append(" and bean.recommend=:recommend");
			f.setParam("recommend", recommend);
		}
		appendOpen(f, open);
		appendReleaseDate(f);
		appendTypeIds(f, typeIds);
		appendSiteIds(f, siteIds);
		f.append(" and bean.status=" + ContentCheck.CHECKED);
		if (!StringUtils.isBlank(title)) {
			f.append(" and bean.contentExt.title like :title");
			f.setParam("title", "%" + title + "%");
		}
		appendAttr(f, attr);
		appendOrder(f, orderBy);
		return f;
	}

	private Finder byChannelIds(Integer[] channelIds, Integer[] typeIds,
			Boolean titleImg, Boolean recommend, String title, int open,Map<String,String[]>attr,int orderBy,
			int option) {
		Finder f = Finder.create();
		int len = channelIds.length;
		// 如果多个栏目
		if (option == 0 || len > 1) {
			f.append("select  bean from Content bean ");
			appendJoinConentDoc(f, open);
			//f.append(" join bean.contentExt as ext");
			if (len == 1) {
				f.append(" where (bean.channel.id=:channelId)");
				f.setParam("channelId", channelIds[0]);
			} else {
				f.append(" where (bean.channel.id in (:channelIds))");
				f.setParamList("channelIds", channelIds);
			}
		} else if (option == 1) {
			// 包含子栏目
			f.append("select  bean from Content bean");
			appendJoinConentDoc(f, open);
			//f.append(" join bean.contentExt as ext");
			f.append(" join bean.channel node,Channel parent");
			f.append(" where (node.lft between parent.lft and parent.rgt");
			f.append(" and bean.site.id=parent.site.id");
			f.append(" and parent.id=:channelId)");
			f.setParam("channelId", channelIds[0]);
		} else if (option == 2) {
			// 包含副栏目
			f.append("select  bean from Content bean");
			appendJoinConentDoc(f, open);
			//f.append(" join bean.contentExt as ext");
			f.append(" join bean.channels as channel");
			f.append(" where (channel.id=:channelId)");
			f.setParam("channelId", channelIds[0]);
		} else {
			throw new RuntimeException("option value must be 0 or 1 or 2.");
		}
		if (titleImg != null) {
			f.append(" and bean.hasTitleImg=:titleImg");
			f.setParam("titleImg", titleImg);
		}
		if (recommend != null) {
			f.append(" and bean.recommend=:recommend");
			f.setParam("recommend", recommend);
		}
		appendOpen(f, open);
		appendReleaseDate(f);
		appendTypeIds(f, typeIds);
		f.append(" and bean.status=" + ContentCheck.CHECKED);
		if (!StringUtils.isBlank(title)) {
			f.append(" and bean.contentExt.title like :title");
			f.setParam("title", "%" + title + "%");
		}
		appendAttr(f, attr);
		appendOrder(f, orderBy);
		return f;
	}
	
	private SQLQuery byChannelIds_sql(Integer[] channelIds, Integer[] typeIds,
			Boolean titleImg, Boolean recommend, String title,Map<String,String[]>attr,int orderBy,
			int option) {
		int len = channelIds.length;
		StringBuilder sql_builder = new StringBuilder(sql_content_base);
		if(attr!=null&&!attr.isEmpty()){
			sql_builder.append(" inner join jc_content_attr on content0_.content_id = jc_content_attr.content_id");
		}
		// 如果多个栏目
		if (option == 0 || len > 1) {
			if (len == 1) {
				sql_builder.append(" where content0_.channel_id = :channelId");
			} else {
				sql_builder.append(" where content0_.channel_id in(:channelId)");
			}
		} else if (option == 1) {
			// 包含子栏目
			sql_builder.append(" cross join jc_channel parent");
			sql_builder.append(" where");
			sql_builder.append(" (channel.lft between parent.lft and parent.rgt)");
			sql_builder.append(" and channel.site_id=parent.site_id");
			sql_builder.append(" and parent.channel_id=:channelId");
		} else if (option == 2) {
			// 包含副栏目
			sql_builder.append(" where jc_content_channel.channel_id=:channelId");
		} else {
			throw new RuntimeException("option value must be 0 or 1 or 2.");
		}
		if (titleImg != null) {
			sql_builder.append(" and content0_.has_title_img = :titleImg");
		}
		if (recommend != null) {
			sql_builder.append(" and content0_.is_recommend = :recommend");
		}
		sql_builder.append(" and jc_content_ext.release_date < :currentDate");
		int len2;
		if (typeIds != null) {
			len2 = typeIds.length;
			if (len2 == 1) {
				sql_builder.append(" and content0_.type_id = :typeId");
			} else if (len2 > 1) {
				sql_builder.append(" and content0_.type_id in (:typeIds)");
			}
		}
		sql_builder.append(" and content0_.status=" + ContentCheck.CHECKED);
		if (!StringUtils.isBlank(title)) {
			sql_builder.append(" and jc_content_ext.title like :title");
		}
		sql_builder = appendAttr_sql(sql_builder, attr);
		sql_builder = appendOrder_sql(sql_builder, orderBy);
		String sql = sql_builder.toString();
		SQLQuery query = getSession().createSQLQuery(sql);
		if((sql.indexOf(":channelId") > 0) && (sql.indexOf(":channelIds") < 0)){
			query.setParameter("channelId", channelIds[0]);
		}
		if(sql.indexOf(":channelIds") > 0){
			query.setParameter("channelIds", channelIds);
		}
		if(sql.indexOf(":title") > 0){
			query.setParameter("title", "%" + title + "%");
		}
		if(sql.indexOf(":titleImg") > 0){
			query.setParameter("titleImg", titleImg);
		}
		if(sql.indexOf(":recommend") > 0){
			query.setParameter("recommend", recommend);
		}
		if(sql.indexOf(":currentDate") > 0){
			query.setParameter("currentDate", contentQueryFreshTimeCache.getTime());
		}
		if((sql.indexOf(":typeId") > 0) && (sql.indexOf(":typeIds") < 0)){
			query.setParameter("typeId", typeIds[0]);
		}
		if(sql.indexOf("typeIds") > 0){
			query.setParameter("typeIds", typeIds);
		}
		if(sql.indexOf("key") > 0){
			if(attr!=null&&!attr.isEmpty()){
				Set<String>keys=attr.keySet();
				Iterator<String>keyIterator=keys.iterator();
				while(keyIterator.hasNext()){
					String key=keyIterator.next();
					String[] mapValue=attr.get(key);
					String value=mapValue[0],operate=mapValue[1];
					if(StringUtils.isNotBlank(key)&&StringUtils.isNotBlank(value)){
						query.setParameter("key", key);
					}
				}
			}
		}
		
		if(sql.indexOf("value") > 0){
			if(attr!=null&&!attr.isEmpty()){
				Set<String>keys=attr.keySet();
				Iterator<String>keyIterator=keys.iterator();
				while(keyIterator.hasNext()){
					String key=keyIterator.next();
					String[] mapValue=attr.get(key);
					String value=mapValue[0],operate=mapValue[1];
					if(StringUtils.isNotBlank(key)&&StringUtils.isNotBlank(value)){
						if("department".equals(key)){
							List<CmsDictionary> list = dicDao.getList("司局");
							Iterator<CmsDictionary> it = list.iterator();
							while(it.hasNext()){
								CmsDictionary cDic = it.next();
								if(cDic.getName().equals(value)){
									value = cDic.getValue();
									break;
								}
							}
						}
						query.setParameter("value", value);
					}
				}
			}
		}
		//System.out.println("#$$#" + sql_builder.toString());
		return query;
	}

	private Finder byChannelPaths(String[] paths, Integer[] siteIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title, int open,Map<String,String[]>attr,int orderBy) {
		Finder f = Finder.create();
		f.append("select  bean from Content bean join bean.channel channel ");
		appendJoinConentDoc(f, open);
		//f.append(" join bean.contentExt as ext");
		int len = paths.length;
		if (len == 1) {
			f.append(" where (channel.path=:path)").setParam("path", paths[0]);
		} else {
			f.append(" where (channel.path in (:paths))");
			f.setParamList("paths", paths);
		}
		if (siteIds != null) {
			len = siteIds.length;
			if (len == 1) {
				f.append(" and (channel.site.id=:siteId)");
				f.setParam("siteId", siteIds[0]);
			} else if (len > 1) {
				f.append(" and (channel.site.id in (:siteIds))");
				f.setParamList("siteIds", siteIds);
			}
		}
		if (titleImg != null) {
			f.append(" and bean.hasTitleImg=:titleImg");
			f.setParam("titleImg", titleImg);
		}
		if (recommend != null) {
			f.append(" and bean.recommend=:recommend");
			f.setParam("recommend", recommend);
		}
		appendOpen(f, open);
		appendReleaseDate(f);
		appendTypeIds(f, typeIds);
		f.append(" and bean.status=" + ContentCheck.CHECKED);
		if (!StringUtils.isBlank(title)) {
			f.append(" and bean.contentExt.title like :title");
			f.setParam("title", "%" + title + "%");
		}
		appendAttr(f, attr);
		appendOrder(f, orderBy);
		return f;
	}

	private Finder byTopicId(Integer topicId, Integer[] siteIds,
			Integer[] channelIds, Integer[] typeIds, Boolean titleImg,
			Boolean recommend, String title, int open,Map<String,String[]>attr,int orderBy) {
		Finder f = Finder.create();
		f.append("select bean from Content bean join bean.topics topic");
		appendJoinConentDoc(f, open);
		//f.append(" join bean.contentExt as ext");
		f.append(" where topic.id=:topicId").setParam("topicId", topicId);
		if (titleImg != null) {
			f.append(" and bean.hasTitleImg=:titleImg");
			f.setParam("titleImg", titleImg);
		}
		if (recommend != null) {
			f.append(" and bean.recommend=:recommend");
			f.setParam("recommend", recommend);
		}
		appendOpen(f, open);
		appendReleaseDate(f);
		appendTypeIds(f, typeIds);
		appendChannelIds(f, channelIds);
		appendSiteIds(f, siteIds);
		f.append(" and bean.status=" + ContentCheck.CHECKED);
		if (!StringUtils.isBlank(title)) {
			f.append(" and bean.contentExt.title like :title");
			f.setParam("title", "%" + title + "%");
		}
		appendAttr(f, attr);
		appendOrder(f, orderBy);
		return f;
	}

	private Finder byTagIds(Integer[] tagIds, Integer[] siteIds,
			Integer[] channelIds, Integer[] typeIds, Integer excludeId,
			Boolean titleImg, Boolean recommend, String title, int open,Map<String,String[]>attr,int orderBy) {
		Finder f = Finder.create();
		int len = tagIds.length;
		if (len == 1) {
			f.append("select bean from Content bean join bean.tags tag");
			appendJoinConentDoc(f, open);
			//f.append(" join bean.contentExt as ext");
			f.append(" where tag.id=:tagId").setParam("tagId", tagIds[0]);
		} else {
			f.append("select bean from Content bean");
			//f.append(" join bean.contentExt as ext");
			f.append(" join bean.tags tag");
			f.append(" where tag.id in(:tagIds)");
			f.setParamList("tagIds", tagIds);
		}
		if (titleImg != null) {
			f.append(" and bean.hasTitleImg=:titleImg");
			f.setParam("titleImg", titleImg);
		}
		if (recommend != null) {
			f.append(" and bean.recommend=:recommend");
			f.setParam("recommend", recommend);
		}
		appendOpen(f, open);
		appendReleaseDate(f);
		appendTypeIds(f, typeIds);
		appendChannelIds(f, channelIds);
		appendSiteIds(f, siteIds);
		if (excludeId != null) {
			f.append(" and bean.id<>:excludeId");
			f.setParam("excludeId", excludeId);
		}
		f.append(" and bean.status=" + ContentCheck.CHECKED);
		if (!StringUtils.isBlank(title)) {
			f.append(" and bean.contentExt.title like :title");
			f.setParam("title", "%" + title + "%");
		}
		appendAttr(f, attr);
		appendOrder(f, orderBy);
		return f;
	}

	private void appendReleaseDate(Finder f) {
		//f.append(" and ext.releaseDate<:currentDate");
		f.append(" and bean.sortDate<:currentDate");
		//f.setParam("currentDate", new Date());
		f.setParam("currentDate", contentQueryFreshTimeCache.getTime());
	}

	private void appendTypeIds(Finder f, Integer[] typeIds) {
		int len;
		if (typeIds != null) {
			len = typeIds.length;
			if (len == 1) {
				f.append(" and bean.type.id=:typeId");
				f.setParam("typeId", typeIds[0]);
			} else if (len > 1) {
				f.append(" and bean.type.id in (:typeIds)");
				f.setParamList("typeIds", typeIds);
			}
		}
	}

	private void appendChannelIds(Finder f, Integer[] channelIds) {
		int len;
		if (channelIds != null) {
			len = channelIds.length;
			if (len == 1) {
				f.append(" and bean.channel.id=:channelId");
				f.setParam("channelId", channelIds[0]);
			} else if (len > 1) {
				f.append(" and bean.channel.id in (:channelIds)");
				f.setParamList("channelIds", channelIds);
			}
		}
	}

	private void appendSiteIds(Finder f, Integer[] siteIds) {
		int len;
		if (siteIds != null) {
			len = siteIds.length;
			if (len == 1) {
				f.append(" and (bean.site.id=:siteId)");
				f.setParam("siteId", siteIds[0]);
			} else if (len > 1) {
				f.append(" and (bean.site.id in (:siteIds))");
				f.setParamList("siteIds", siteIds);
			}
		}
	}
	
	
	private void appendJoinConentDoc(Finder f,int open){
		//默认不设置open参数 不连接查询ContentDoc
		if(open!=ContentDoc.ALL){
			f.append(" join bean.contentDocSet as doc ");
		}
	}
	
	private void appendOpen(Finder f,int open){
		if(open!=ContentDoc.ALL){
			if(open==ContentDoc.OPEN){
				f.append(" and doc.isOpen=true");
			}else{
				f.append(" and doc.isOpen=false");
			}
		}
	}

	private void appendAttr(Finder f, Map<String,String[]>attr){
		if(attr!=null&&!attr.isEmpty()){
			Set<String>keys=attr.keySet();
			Iterator<String>keyIterator=keys.iterator();
			while(keyIterator.hasNext()){
				String key=keyIterator.next();
				String[] mapValue=attr.get(key);
				String value=mapValue[0],operate=mapValue[1];
				if(StringUtils.isNotBlank(key)&&StringUtils.isNotBlank(value)){
					if(operate.equals(PARAM_ATTR_EQ)){
						f.append(" and bean.attr[:k"+key+"]=:v"+key).setParam("k"+key, key).setParam("v"+key, value);
					}else if(operate.equals(PARAM_ATTR_START)){
						f.append(" and bean.attr[:k"+key+"] like :v"+key).setParam("k"+key, key).setParam("v"+key, value+"%");
					}else if(operate.equals(PARAM_ATTR_END)){
						f.append(" and bean.attr[:k"+key+"] like :v"+key).setParam("k"+key, key).setParam("v"+key, "%"+value);
					}else if(operate.equals(PARAM_ATTR_LIKE)){
						f.append(" and bean.attr[:k"+key+"] like :v"+key).setParam("k"+key, key).setParam("v"+key, "%"+value+"%");
					}else if(operate.equals(PARAM_ATTR_IN)){
						if(StringUtils.isNotBlank(value)){
							f.append(" and bean.attr[:k"+key+"] in (:v"+key+")").setParam("k"+key, key);
							f.setParamList("v"+key, value.split(","));
						}
					}
					else {
						//取绝对值比较大小
						Float floatValue=Float.valueOf(value);
						if(operate.equals(PARAM_ATTR_GT)){
							if(floatValue>=0){
								f.append(" and (bean.attr[:k"+key+"]>=0 and abs(bean.attr[:k"+key+"])>:v"+key+")").setParam("k"+key, key).setParam("v"+key, floatValue);
							}else{
								f.append(" and ((bean.attr[:k"+key+"]<0 and abs(bean.attr[:k"+key+"])<:v"+key+") or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
					 	}else if(operate.equals(PARAM_ATTR_GTE)){
					 		if(floatValue>=0){
								f.append(" and (abs(bean.attr[:k"+key+"])>=:v"+key+" and bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, floatValue);
							}else{
								f.append(" and ((abs(bean.attr[:k"+key+"])<=:v"+key+" and bean.attr[:k"+key+"]<0) or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
						}else if(operate.equals(PARAM_ATTR_LT)){
							if(floatValue>=0){
								f.append(" and ((abs(bean.attr[:k"+key+"])<:v"+key+" and bean.attr[:k"+key+"]>=0) or bean.attr[:k"+key+"]<=0)").setParam("k"+key, key).setParam("v"+key, floatValue);
							}else{
								f.append(" and ((abs(bean.attr[:k"+key+"])>:v"+key+" and bean.attr[:k"+key+"]<0) or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
						}else if(operate.equals(PARAM_ATTR_LTE)){
							if(floatValue>=0){
								f.append(" and ((abs(bean.attr[:k"+key+"])<=:v"+key+" and bean.attr[:k"+key+"]>=0) or bean.attr[:k"+key+"]<=0)").setParam("k"+key, key).setParam("v"+key, floatValue);
							}else{
								f.append(" and ((abs(bean.attr[:k"+key+"])>=:v"+key+" and bean.attr[:k"+key+"]<0) or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
						}
					}
				}
			}
		}
	}
	
	private StringBuilder appendAttr_sql(StringBuilder sql_builder, Map<String,String[]>attr){
		if(attr!=null&&!attr.isEmpty()){
			Set<String>keys=attr.keySet();
			Iterator<String>keyIterator=keys.iterator();
			while(keyIterator.hasNext()){
				String key=keyIterator.next();
				String[] mapValue=attr.get(key);
				String value=mapValue[0],operate=mapValue[1];
				if(StringUtils.isNotBlank(key)&&StringUtils.isNotBlank(value)){
					if(operate.equals(PARAM_ATTR_EQ)){
						sql_builder.append(" and jc_content_attr.attr_name=:key and jc_content_attr.attr_value=:value");
					}else if(operate.equals(PARAM_ATTR_START)){
						//f.append(" and bean.attr[:k"+key+"] like :v"+key).setParam("k"+key, key).setParam("v"+key, value+"%");
						sql_builder.append(" and jc_content_attr.attr_name=:key and jc_content_attr.attr_value like :value");
					}else if(operate.equals(PARAM_ATTR_END)){
						//f.append(" and bean.attr[:k"+key+"] like :v"+key).setParam("k"+key, key).setParam("v"+key, "%"+value);
						sql_builder.append(" and jc_content_attr.attr_name=:key and jc_content_attr.attr_value like :value");
					}else if(operate.equals(PARAM_ATTR_LIKE)){
						//f.append(" and bean.attr[:k"+key+"] like :v"+key).setParam("k"+key, key).setParam("v"+key, "%"+value+"%");
						sql_builder.append(" and jc_content_attr.attr_name=:key and jc_content_attr.attr_value like :value");
					}else if(operate.equals(PARAM_ATTR_IN)){
						if(StringUtils.isNotBlank(value)){
							//f.append(" and bean.attr[:k"+key+"] in (:v"+key+")").setParam("k"+key, key);
							//f.setParamList("v"+key, value.split(","));
							sql_builder.append(" and jc_content_attr.attr_name=:key and jc_content_attr.attr_value in (:value)");
						}
					}
					else {
						//取绝对值比较大小
						/*Float floatValue=Float.valueOf(value);
						if(operate.equals(PARAM_ATTR_GT)){
							if(floatValue>=0){
								f.append(" and (bean.attr[:k"+key+"]>=0 and abs(bean.attr[:k"+key+"])>:v"+key+")").setParam("k"+key, key).setParam("v"+key, floatValue);
								sql_builder.append(" ");
							}else{
								f.append(" and ((bean.attr[:k"+key+"]<0 and abs(bean.attr[:k"+key+"])<:v"+key+") or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
					 	}else if(operate.equals(PARAM_ATTR_GTE)){
					 		if(floatValue>=0){
								f.append(" and (abs(bean.attr[:k"+key+"])>=:v"+key+" and bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, floatValue);
							}else{
								f.append(" and ((abs(bean.attr[:k"+key+"])<=:v"+key+" and bean.attr[:k"+key+"]<0) or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
						}else if(operate.equals(PARAM_ATTR_LT)){
							if(floatValue>=0){
								f.append(" and ((abs(bean.attr[:k"+key+"])<:v"+key+" and bean.attr[:k"+key+"]>=0) or bean.attr[:k"+key+"]<=0)").setParam("k"+key, key).setParam("v"+key, floatValue);
							}else{
								f.append(" and ((abs(bean.attr[:k"+key+"])>:v"+key+" and bean.attr[:k"+key+"]<0) or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
						}else if(operate.equals(PARAM_ATTR_LTE)){
							if(floatValue>=0){
								f.append(" and ((abs(bean.attr[:k"+key+"])<=:v"+key+" and bean.attr[:k"+key+"]>=0) or bean.attr[:k"+key+"]<=0)").setParam("k"+key, key).setParam("v"+key, floatValue);
							}else{
								f.append(" and ((abs(bean.attr[:k"+key+"])>=:v"+key+" and bean.attr[:k"+key+"]<0) or bean.attr[:k"+key+"]>=0)").setParam("k"+key, key).setParam("v"+key, -floatValue);
							}
						}*/
					}
				}
			}
		}
		return sql_builder;
	}
	
	
	private void appendOrder(Finder f, int orderBy) {
		switch (orderBy) {
		case 1:
			// ID升序
			f.append(" order by bean.id asc");
			break;
		case 2:
			// 发布时间降序
			f.append(" order by bean.sortDate desc");
			//System.out.println("@!@!@!@: " );
			break;
		case 3:
			// 发布时间升序
			f.append(" order by bean.sortDate asc");
			break;
		case 4:
			// 固顶级别降序、发布时间降序
			f.append(" order by bean.topLevel desc, bean.sortDate desc");
			break;
		case 5:
			// 固顶级别降序、发布时间升序
			f.append(" order by bean.topLevel desc, bean.sortDate asc");
			break;
		case 6:
			// 日访问降序
			f.append(" and bean.contentCount.viewsDay>-1 ");
			f.append(" order by bean.contentCount.viewsDay desc");
			//f.append(", bean.id desc");
			break;
		case 7:
			// 周访问降序
			f.append(" and bean.contentCount.viewsWeek>-1 ");
			f.append(" order by bean.contentCount.viewsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 8:
			// 月访问降序
			f.append(" and bean.contentCount.viewsMonth>-1 ");
			f.append(" order by bean.contentCount.viewsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 9:
			// 总访问降序
			f.append(" and bean.contentCount.views>-1 ");
			f.append(" order by bean.contentCount.views desc");
			//f.append(", bean.id desc");
			break;
		case 10:
			// 日评论降序
			f.append(" order by bean.commentsDay desc");
			f.append(", bean.id desc");
			break;
		case 11:
			// 周评论降序
			f.append(" and bean.contentCount.commentsWeek>-1 ");
			f.append(" order by bean.contentCount.commentsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 12:
			// 月评论降序
			f.append(" and bean.contentCount.commentsMonth>-1 ");
			f.append(" order by bean.contentCount.commentsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 13:
			// 总评论降序
			f.append(" and bean.contentCount.comments>-1 ");
			f.append(" order by bean.contentCount.comments desc");
			//f.append(", bean.id desc");
			break;
		case 14:
			// 日下载降序
			f.append(" order by bean.downloadsDay desc");
			f.append(", bean.id desc");
			break;
		case 15:
			// 周下载降序
			f.append(" and bean.contentCount.downloadsWeek>-1 ");
			f.append(" order by bean.contentCount.downloadsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 16:
			// 月下载降序
			f.append(" and bean.contentCount.downloadsMonth>-1 ");
			f.append(" order by bean.contentCount.downloadsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 17:
			// 总下载降序
			f.append(" and bean.contentCount.downloads>-1 ");
			f.append(" order by bean.contentCount.downloads desc");
			//f.append(", bean.id desc");
			break;
		case 18:
			// 日顶降序
			f.append(" order by bean.upsDay desc");
			f.append(", bean.id desc");
			break;
		case 19:
			// 周顶降序
			f.append(" and bean.contentCount.upsWeek>-1 ");
			f.append(" order by bean.contentCount.upsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 20:
			// 月顶降序
			f.append(" and bean.contentCount.upsMonth>-1 ");
			f.append(" order by bean.contentCount.upsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 21:
			// 总顶降序
			f.append(" and bean.contentCount.ups>-1 ");
			f.append(" order by bean.contentCount.ups desc");
			//f.append(", bean.id desc");
			break;
		case 22:
			// 推荐级别降序、发布时间降序
			f.append(" order by bean.recommendLevel desc, bean.sortDate desc");
			break;
		case 23:
			// 推荐级别升序、发布时间降序
			f.append(" order by bean.recommendLevel asc, bean.sortDate desc");
			break;
		case 24:
			f.append(" order by bean.contentExt.releaseDate desc");
			break;
		case 25:
			f.append(" order by bean.contentExt.releaseDate asc");
			break;
		case 26:
			f.append(" order by bean.id asc");
			break;
		default:
			// 默认： ID降序
			f.append(" order by bean.id desc");
		}
	}

	public int countByChannelId(int channelId) {
		String hql = "select count(*) from Content bean"
				+ " join bean.channel channel,Channel parent"
				+ " where channel.lft between parent.lft and parent.rgt"
				+ " and channel.site.id=parent.site.id"
				+ " and parent.id=:parentId";
		Query query = getSession().createQuery(hql);
		query.setParameter("parentId", channelId);
		return ((Number) (query.iterate().next())).intValue();
	}

	public Content findById(Integer id) {
		Content entity = get(id);
		return entity;
	}

	public Content save(Content bean) {
		getSession().save(bean);
		return bean;
	}

	public Content deleteById(Integer id) {
		Content entity = super.get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
		return entity;
	}
	/**不知为何要重写，此处先注释
	protected Pagination find(Finder finder, int pageNo, int pageSize) {
		int totalCount = countQueryResult(finder);
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = getSession().createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(p.getFirstResult());
		query.setMaxResults(p.getPageSize());
		if (finder.isCacheable()) {
			query.setCacheable(true);
		}
		List list = query.list();
		p.setList(list);
		return p;
	}
	protected int countQueryResult(Finder finder) {
		Query query = getSession().createQuery(finder.getRowCountHql());
		finder.setParamsToQuery(query);
		if (finder.isCacheable()) {
			query.setCacheable(true);
		}
		return ((Number) query.iterate().next()).intValue();
	} 
	*/
	
	private Finder createCollectFinder(Integer siteId, Integer memberId){
		Finder f = Finder.create("select bean from Content bean join bean.collectUsers user where user.id=:userId").setParam("userId", memberId);
		if (siteId != null) {
			f.append(" and bean.site.id=:siteId");
			f.setParam("siteId", siteId);
		}
		f.append(" and bean.status<>:status");
		f.setParam("status", ContentCheck.RECYCLE);
		return f;
	}

	@Override
	protected Class<Content> getEntityClass() {
		return Content.class;
	}
	@Autowired
	private ContentQueryFreshTimeCache contentQueryFreshTimeCache;
	
	@Override
	public boolean update_kjb_sort(String content_id, String channel_id, Byte kjb_sort) {
		StringBuilder sql_builder = new StringBuilder("update jc_content_channel set sort=:kjb_sort where channel_id=:channel_id and content_id=:content_id");
		String sql = sql_builder.toString();
		SQLQuery query = getSession().createSQLQuery(sql);
		query.setParameter("kjb_sort", kjb_sort);
		query.setParameter("content_id", content_id);
		query.setParameter("channel_id", channel_id);
		System.out.println("**"+sql);
		int resultNum = query.executeUpdate();
		if(resultNum==1)
			return true;
		else
			return false;
	}

	@Override
	public boolean generateContentIndex(Integer contentId, String attrName, String attrValue) {
		String sql_sequence = "SELECT nextval('xxgk_seq')";
		SQLQuery query_sequence = getSession().createSQLQuery(sql_sequence);
		String sequence = query_sequence.uniqueResult().toString();
		sequence = String.format("%0" + 5 + "d", Integer.parseInt(sequence));
		attrValue = attrValue + "-" + sequence;
		
		String sql = "insert into jc_content_attr(content_id, attr_name, attr_value) values(:contentId, :attrName, :attrValue)";
		SQLQuery query = getSession().createSQLQuery(sql);
		query.setParameter("contentId", contentId);
		query.setParameter("attrName", attrName);
		query.setParameter("attrValue", attrValue);
		return query.executeUpdate() > 0;
	}

	@Override
	public boolean updateContentIndex(Integer contentId, String attrName, String attrValue) {
		String sql_sequence = "SELECT nextval('SAMPLE')";
		SQLQuery query_sequence = getSession().createSQLQuery(sql_sequence);
		String sequence = query_sequence.uniqueResult().toString();
		attrValue = attrValue + "-" + sequence;

		String sql = "update jc_content_attr set attr_value = :attrValue where content_id = :contentId and attr_name = :attrName";
		SQLQuery query = getSession().createSQLQuery(sql);
		query.setParameter("contentId", contentId);
		query.setParameter("attrName", attrName);
		query.setParameter("attrValue", attrValue);
		return query.executeUpdate() > 0;
	}
	
	private StringBuilder appendOrder_sql(StringBuilder sql_builder, int orderBy) {
		switch (orderBy) {
		case 1:
			// ID升序
			//f.append(" order by bean.id asc");
			sql_builder.append(" order by content0_.content_id asc");
			break;
		case 2:
			//f.append(" order by bean.sortDate desc");
			sql_builder.append(" order by content0_.sort_date desc");
			//System.out.println("@!@!@!@: " + sql_builder.toString());
			break;
		case 3:
			// 发布时间升序
			//f.append(" order by bean.sortDate asc");
			sql_builder.append(" order by content0_.sort_date asc");
			break;
		case 4:
			// 固顶级别降序、发布时间降序
			//f.append(" order by bean.topLevel desc, bean.sortDate desc");
			//f.append(" order by bean.sortDate asc");
			sql_builder.append(" order by jc_content_channel.sort desc, content0_.sort_date desc");
			break;
		case 5:
			// 固顶级别降序、发布时间升序
			//f.append(" order by bean.topLevel desc, bean.sortDate asc");
			sql_builder.append(" order by jc_content_channel.sort desc, content0_.sort_date asc");
			break;
		case 6:
			// 日访问降序
			//f.append(" order by bean.contentCount.viewsDay desc, bean.id desc");
			break;
		case 7:
			// 周访问降序
			//f.append(" order by bean.contentCount.viewsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 8:
			// 月访问降序
			//f.append(" order by bean.contentCount.viewsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 9:
			// 总访问降序
			//f.append(" order by bean.contentCount.views desc");
			//f.append(", bean.id desc");
			break;
		case 10:
			// 日评论降序
			//f.append(" order by bean.commentsDay desc, bean.id desc");
			break;
		case 11:
			// 周评论降序
			//f.append(" order by bean.contentCount.commentsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 12:
			// 月评论降序
			//f.append(" order by bean.contentCount.commentsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 13:
			// 总评论降序
			//f.append(" order by bean.contentCount.comments desc");
			//f.append(", bean.id desc");
			break;
		case 14:
			// 日下载降序
			//f.append(" order by bean.downloadsDay desc, bean.id desc");
			break;
		case 15:
			// 周下载降序
			//f.append(" order by bean.contentCount.downloadsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 16:
			// 月下载降序
			//f.append(" order by bean.contentCount.downloadsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 17:
			// 总下载降序
			//f.append(" order by bean.contentCount.downloads desc");
			//f.append(", bean.id desc");
			break;
		case 18:
			// 日顶降序
			//f.append(" order by bean.upsDay desc, bean.id desc");
			break;
		case 19:
			// 周顶降序
			//f.append(" order by bean.contentCount.upsWeek desc");
			//f.append(", bean.id desc");
			break;
		case 20:
			// 月顶降序
			//f.append(" order by bean.contentCount.upsMonth desc");
			//f.append(", bean.id desc");
			break;
		case 21:
			// 总顶降序
			//f.append(" order by bean.contentCount.ups desc, bean.id desc");
			break;
		case 22:
			// 推荐级别降序、发布时间降序
			//f.append(" order by bean.recommendLevel desc, bean.sortDate desc");
			break;
		case 23:
			// 推荐级别升序、发布时间降序
			//f.append(" order by bean.recommendLevel asc, bean.sortDate desc");
			break;
		case 24:
			//f.append(" order by bean.contentExt.releaseDate desc");
			break;
		case 25:
			//f.append(" order by bean.contentExt.releaseDate asc");
			break;
		case 26:
			//f.append(" order by bean.id asc");
			break;
		case 27:
			sql_builder.append(" order by jc_content_channel.sort asc, content0_.sort_date desc");
			break;
		case 28:
			sql_builder.append(" order by jc_content_channel.sort asc, content0_.sort_date asc");
			break;
		default:
			// 默认： ID降序
			sql_builder.append(" order by content0_.content_id desc");
		}
		return sql_builder;
	}
	
	private void appendQuery_sql(StringBuilder sql_builder, String title, Integer typeId,
			Integer inputUserId, ContentStatus status, boolean topLevel,
			boolean recommend) {
		System.out.println("!!!!appendQuery_sql");
		if (!StringUtils.isBlank(title)) {
			sql_builder.append(" and jc_content_ext.title like :title");
		}
		if (typeId != null) {
			sql_builder.append(" and content0_.type_id = :typeId");
		}
		if (inputUserId != null&&inputUserId!=0) {
			sql_builder.append(" and content0_.user_id = :inputUserId");
		}else{
			//输入了没有的用户名的情况
			if(inputUserId==null){
				sql_builder.append(" and 1!=1");
			}
		}
		if (topLevel) {
			sql_builder.append(" and content0_.top_level > '0'");
		}
		if (recommend) {
			sql_builder.append(" and content0_.is_recommend = '1'");//true
		}
		if (draft == status) {
			sql_builder.append(" and content0_.status = :status");
		}if (contribute == status) {
			sql_builder.append(" and content0_.status = :status");
		} else if (checked == status) {
			sql_builder.append(" and content0_.status = :status");
		} else if (prepared == status ) {
			sql_builder.append(" and content0_.status = :status");
		} else if (rejected == status ) {
			sql_builder.append(" and content0_.status = :status");
		}else if (passed == status) {
			sql_builder.append(" and (content0_.status = :checking or content0_.status = :checked)");
		} else if (all == status) {
			sql_builder.append(" and content0_.status <> :status");
		} else if (recycle == status) {
			sql_builder.append(" and content0_.status = :status");
		} else if (pigeonhole == status) {
			sql_builder.append(" and content0_.status = :status");
		} else {
			// never
		}
	}
}