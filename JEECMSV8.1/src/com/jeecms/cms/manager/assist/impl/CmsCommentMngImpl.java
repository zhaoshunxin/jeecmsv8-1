package com.jeecms.cms.manager.assist.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.cms.dao.assist.CmsCommentDao;
import com.jeecms.cms.entity.assist.CmsComment;
import com.jeecms.cms.entity.assist.CmsCommentExt;
import com.jeecms.cms.manager.assist.CmsCommentExtMng;
import com.jeecms.cms.manager.assist.CmsCommentMng;
import com.jeecms.cms.manager.assist.CmsSensitivityMng;
import com.jeecms.cms.manager.main.ContentCountMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.common.page.Pagination;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.manager.CmsUserMng;

@Service
@Transactional
public class CmsCommentMngImpl implements CmsCommentMng {
	@Transactional(readOnly = true)
	public Pagination getPage(Integer siteId, Integer contentId,
			Integer greaterThen, Boolean checked, Boolean recommend,
			boolean desc, int pageNo, int pageSize) {
		Pagination page = dao.getPage(siteId, contentId, greaterThen, checked,
				recommend, desc, pageNo, pageSize, false);
		return page;
	}

	@Transactional(readOnly = true)
	public Pagination getPageForTag(Integer siteId, Integer contentId,
			Integer greaterThen, Boolean checked, Boolean recommend,
			boolean desc, int pageNo, int pageSize) {
		Pagination page = dao.getPage(siteId, contentId, greaterThen, checked,
				recommend, desc, pageNo, pageSize, true);
		return page;
	}
	
	@Transactional(readOnly = true)
	public Pagination getPageForMember(Integer siteId, Integer contentId,Integer toUserId,Integer fromUserId,
			Integer greaterThen, Boolean checked, Boolean recommend,
			boolean desc, int pageNo, int pageSize){
		Pagination page = dao.getPageForMember(siteId, contentId,toUserId,fromUserId, greaterThen, checked,
				recommend, desc, pageNo, pageSize, false);
		return page;
	}
	
	@Transactional(readOnly = true)
	public List<CmsComment> getListForMember(Integer siteId, Integer contentId,
			Integer toUserId,Integer fromUserId,Integer greaterThen,
			Boolean checked, Boolean recommend,
			boolean desc, Integer first, Integer count){
		return dao.getListForMember(siteId, contentId, toUserId, 
				fromUserId, greaterThen, checked, recommend, 
				desc, first, count, true);
	}
	
	@Transactional(readOnly = true)
	public List<CmsComment> getListForDel(Integer siteId, Integer userId,Integer commentUserId,String ip){
		return dao.getListForDel(siteId,userId,commentUserId,ip);
	}

	@Transactional(readOnly = true)
	public List<CmsComment> getListForTag(Integer siteId, Integer contentId,
			Integer parentId,Integer greaterThen, Boolean checked, Boolean recommend,
			boolean desc, Integer first,int count) {
		return dao.getList(siteId, contentId,parentId,greaterThen, checked, recommend,
				desc, first,count, true);
	}

	@Transactional(readOnly = true)
	public CmsComment findById(Integer id) {
		CmsComment entity = dao.findById(id);
		return entity;
	}

	public CmsComment comment(Integer score,String text, String ip, Integer contentId,
			Integer siteId, Integer userId, boolean checked, boolean recommend,Integer parentId) {
		CmsComment comment = new CmsComment();
		comment.setContent(contentMng.findById(contentId));
		comment.setSite(cmsSiteMng.findById(siteId));
		if (userId != null) {
			comment.setCommentUser(cmsUserMng.findById(userId));
		}
		comment.setChecked(checked);
		comment.setRecommend(recommend);
		comment.setScore(score);
		comment.init();
		if(parentId!=null){
			CmsComment parent=findById(parentId);
			if(parent!=null){
				comment.setParent(parent);
				update(parent, parent.getCommentExt());
			}
		}
		dao.save(comment);
		text = cmsSensitivityMng.replaceSensitivity(text);
		cmsCommentExtMng.save(ip, text, comment);
		contentCountMng.commentCount(contentId);
		return comment;
	}

	public CmsComment update(CmsComment bean, CmsCommentExt ext) {
		CmsComment dbComment=findById(bean.getId());
		CmsComment parent=dbComment.getParent();
		if(parent!=null){
			//从未审核到审核
			if(!dbComment.getChecked()&&bean.getChecked()){
				if(parent.getReplyCount()!=null&&parent.getReplyCount()>0){
					parent.setReplyCount(parent.getReplyCount()+1);
				}else{
					parent.setReplyCount(1);
				}
			}
			//从审核到未审核
			if(dbComment.getChecked()&&!bean.getChecked()){
				if(parent.getReplyCount()!=null&&parent.getReplyCount()>0){
					parent.setReplyCount(parent.getReplyCount()-1);
				}else{
					parent.setReplyCount(0);
				}
			}
			update(parent, parent.getCommentExt());
		}
		Updater<CmsComment> updater = new Updater<CmsComment>(bean);
		bean = dao.updateByUpdater(updater);
		cmsCommentExtMng.update(ext);
		return bean;
	}

	public int deleteByContentId(Integer contentId) {
		cmsCommentExtMng.deleteByContentId(contentId);
		return dao.deleteByContentId(contentId);
	}

	public CmsComment deleteById(Integer id) {
		CmsComment bean = dao.deleteById(id);
		CmsComment parent=bean.getParent();
		if(parent!=null&&bean.getChecked()){
			if(parent.getReplyCount()!=null&&parent.getReplyCount()>0){
				parent.setReplyCount(parent.getReplyCount()-1);
			}else{
				parent.setReplyCount(0);
			}
			update(parent, parent.getCommentExt());
		}
		return bean;
	}

	public CmsComment[] deleteByIds(Integer[] ids) {
		CmsComment[] beans = new CmsComment[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}
	
	public CmsComment[] checkByIds(Integer[] ids, CmsUser user, boolean checked) {
		CmsComment[] beans = new CmsComment[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = checkById(ids[i],user,checked);
		}
		return beans;
	}
	
	
	private CmsComment checkById(Integer id,CmsUser checkUser,boolean checked){
		CmsComment bean=findById(id);
		CmsComment parent=bean.getParent();
		if(parent!=null){
			//从未审核到审核
			if(!bean.getChecked()&&checked){
				if(parent.getReplyCount()!=null&&parent.getReplyCount()>0){
					parent.setReplyCount(parent.getReplyCount()+1);
				}else{
					parent.setReplyCount(1);
				}
			}
			//从审核到未审核
			if(bean.getChecked()&&!checked){
				if(parent.getReplyCount()!=null&&parent.getReplyCount()>0){
					parent.setReplyCount(parent.getReplyCount()-1);
				}else{
					parent.setReplyCount(0);
				}
			}
			update(parent, parent.getCommentExt());
		}
		Updater<CmsComment> updater = new Updater<CmsComment>(bean);
		bean = dao.updateByUpdater(updater);
		bean.setChecked(checked);
		return bean;
	}
	
	public void ups(Integer id) {
		CmsComment comment = findById(id);
		comment.setUps((short) (comment.getUps() + 1));
	}

	public void downs(Integer id) {
		CmsComment comment = findById(id);
		comment.setDowns((short) (comment.getDowns() + 1));
	}

	private CmsSensitivityMng cmsSensitivityMng;
	private CmsUserMng cmsUserMng;
	private CmsSiteMng cmsSiteMng;
	private ContentMng contentMng;
	private ContentCountMng contentCountMng;
	private CmsCommentExtMng cmsCommentExtMng;
	private CmsCommentDao dao;

	@Autowired
	public void setCmsSensitivityMng(CmsSensitivityMng cmsSensitivityMng) {
		this.cmsSensitivityMng = cmsSensitivityMng;
	}

	@Autowired
	public void setCmsUserMng(CmsUserMng cmsUserMng) {
		this.cmsUserMng = cmsUserMng;
	}

	@Autowired
	public void setCmsSiteMng(CmsSiteMng cmsSiteMng) {
		this.cmsSiteMng = cmsSiteMng;
	}

	@Autowired
	public void setContentMng(ContentMng contentMng) {
		this.contentMng = contentMng;
	}

	@Autowired
	public void setCmsCommentExtMng(CmsCommentExtMng cmsCommentExtMng) {
		this.cmsCommentExtMng = cmsCommentExtMng;
	}

	@Autowired
	public void setContentCountMng(ContentCountMng contentCountMng) {
		this.contentCountMng = contentCountMng;
	}

	@Autowired
	public void setDao(CmsCommentDao dao) {
		this.dao = dao;
	}

}