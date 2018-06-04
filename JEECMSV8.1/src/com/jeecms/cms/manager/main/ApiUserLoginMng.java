package com.jeecms.cms.manager.main;

import com.jeecms.common.page.Pagination;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.cms.entity.main.ApiUserLogin;

public interface ApiUserLoginMng {
	public Pagination getPage(int pageNo, int pageSize);

	public ApiUserLogin findById(Long id);
	
	public ApiUserLogin findUserLogin(String username,String sessionKey);
	
	public CmsUser findUser(String sessionKey,String aesKey,String ivKey);
	
	public ApiUserLogin userLogin(String username,String sessionKey);
	
	public ApiUserLogin userLogout(String username);

	public ApiUserLogin save(ApiUserLogin bean);

	public ApiUserLogin update(ApiUserLogin bean);

	public ApiUserLogin deleteById(Long id);
	
	public ApiUserLogin[] deleteByIds(Long[] ids);
}