package com.jeecms.cms.manager.main;

import com.jeecms.common.page.Pagination;
import com.jeecms.cms.entity.main.ApiAccount;

public interface ApiAccountMng {
	public Pagination getPage(int pageNo, int pageSize);
	
	public ApiAccount findByAppId(String appId);

	public ApiAccount findById(Integer id);

	public ApiAccount save(ApiAccount bean);

	public ApiAccount update(ApiAccount bean,String appKey,String aesKey,String ivKey);

	public ApiAccount deleteById(Integer id);
	
	public ApiAccount[] deleteByIds(Integer[] ids);
}