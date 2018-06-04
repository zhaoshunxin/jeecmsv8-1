package com.jeecms.cms.action.admin.main;

import static com.jeecms.common.page.SimplePage.cpn;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.WebErrors;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;

@Controller
public class ApiRecordAct {
	private static final Logger log = LoggerFactory.getLogger(ApiRecordAct.class);

	@RequiresPermissions("apiRecord:v_list")
	@RequestMapping("/apiRecord/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		return "apiRecord/list";
	}

	@RequiresPermissions("apiRecord:o_delete")
	@RequestMapping("/apiRecord/o_delete.do")
	public String delete(Long[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		ApiRecord[] beans = manager.deleteByIds(ids);
		for (ApiRecord bean : beans) {
			log.info("delete ApiRecord id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}

	private WebErrors validateDelete(Long[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Long id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}

	private boolean vldExist(Long id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		ApiRecord entity = manager.findById(id);
		if(errors.ifNotExist(entity, ApiRecord.class, id)) {
			return true;
		}
		return false;
	}
	
	@Autowired
	private ApiRecordMng manager;
}