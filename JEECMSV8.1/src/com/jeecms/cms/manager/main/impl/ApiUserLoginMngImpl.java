package com.jeecms.cms.manager.main.impl;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.common.hibernate4.Updater;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.util.AES128Util;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.cms.dao.main.ApiUserLoginDao;
import com.jeecms.cms.entity.main.ApiUserLogin;
import com.jeecms.cms.manager.main.ApiUserLoginMng;

@Service
@Transactional
public class ApiUserLoginMngImpl implements ApiUserLoginMng {
	@Transactional(readOnly = true)
	public Pagination getPage(int pageNo, int pageSize) {
		Pagination page = dao.getPage(pageNo, pageSize);
		return page;
	}

	@Transactional(readOnly = true)
	public ApiUserLogin findById(Long id) {
		ApiUserLogin entity = dao.findById(id);
		return entity;
	}
	
	@Transactional(readOnly = true)
	public ApiUserLogin findUserLogin(String username,String sessionKey){
		return dao.findUserLogin(username, sessionKey);
	}
	
	@Transactional(readOnly = true)
	public CmsUser findUser(String sessionKey,String aesKey,String ivKey){
		String decryptSessionKey="";
		CmsUser user=null;
		if(StringUtils.isNotBlank(sessionKey)){
			try {
				//sessionKey用户会话标志加密串
				decryptSessionKey=AES128Util.decrypt(sessionKey, aesKey,ivKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ApiUserLogin apiUserLogin=findUserLogin(null, decryptSessionKey);
			if(apiUserLogin!=null&&StringUtils.isNotBlank(decryptSessionKey)){
				String username=apiUserLogin.getUsername();
				if(StringUtils.isNotBlank(username)){
					user=userMng.findByUsername(username);
				}
			}
		}
		return user;
	}
	
	public ApiUserLogin userLogin(String username,String sessionKey){
		ApiUserLogin login=findUserLogin(username,null);
		if(login==null){
			login=new ApiUserLogin();
			login.setLoginTime(Calendar.getInstance().getTime());
			login.setLoginCount(1);
			login.setSessionKey(sessionKey);
			login.setUsername(username);
			login=save(login);
		}else{
			login.setLoginTime(Calendar.getInstance().getTime());
			login.setLoginCount(1+login.getLoginCount());
			login.setSessionKey(sessionKey);
			update(login);
		}
		return login;
	}
	
	public ApiUserLogin userLogout(String username){
		ApiUserLogin login=findUserLogin(username,null);
		if(login!=null){
			login.setSessionKey("");
			update(login);
		}
		return login;
	}

	public ApiUserLogin save(ApiUserLogin bean) {
		dao.save(bean);
		return bean;
	}

	public ApiUserLogin update(ApiUserLogin bean) {
		Updater<ApiUserLogin> updater = new Updater<ApiUserLogin>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}

	public ApiUserLogin deleteById(Long id) {
		ApiUserLogin bean = dao.deleteById(id);
		return bean;
	}
	
	public ApiUserLogin[] deleteByIds(Long[] ids) {
		ApiUserLogin[] beans = new ApiUserLogin[ids.length];
		for (int i = 0,len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	private ApiUserLoginDao dao;
	@Autowired
	private CmsUserMng userMng;

	@Autowired
	public void setDao(ApiUserLoginDao dao) {
		this.dao = dao;
	}
}