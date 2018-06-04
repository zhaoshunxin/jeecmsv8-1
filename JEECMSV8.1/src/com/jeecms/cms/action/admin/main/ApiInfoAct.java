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

import com.jeecms.cms.entity.main.ApiInfo;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.cms.manager.main.ApiInfoMng;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.WebErrors;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;

@Controller
public class ApiInfoAct {
	private static final Logger log = LoggerFactory.getLogger(ApiInfoAct.class);

	@RequiresPermissions("apiInfo:v_list")
	@RequestMapping("/apiInfo/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		return "apiInfo/list";
	}

	@RequiresPermissions("apiInfo:v_add")
	@RequestMapping("/apiInfo/v_add.do")
	public String add(ModelMap model) {
		return "apiInfo/add";
	}

	@RequiresPermissions("apiInfo:v_edit")
	@RequestMapping("/apiInfo/v_edit.do")
	public String edit(Integer id, Integer pageNo, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("apiInfo", manager.findById(id));
		model.addAttribute("pageNo",pageNo);
		return "apiInfo/edit";
	}

	@RequiresPermissions("apiInfo:o_save")
	@RequestMapping("/apiInfo/o_save.do")
	public String save(ApiInfo bean, HttpServletRequest request, ModelMap model) {
		bean.init();
		bean = manager.save(bean);
		log.info("save ApiInfo id={}", bean.getId());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("apiInfo:o_update")
	@RequestMapping("/apiInfo/o_update.do")
	public String update(ApiInfo bean, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update ApiInfo id={}.", bean.getId());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("apiInfo:o_delete")
	@RequestMapping("/apiInfo/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		ApiInfo[] beans = manager.deleteByIds(ids);
		for (ApiInfo bean : beans) {
			log.info("delete ApiInfo id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}
	
	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		ApiInfo entity = manager.findById(id);
		if(errors.ifNotExist(entity, ApiInfo.class, id)) {
			return true;
		}
		return false;
	}
	
	@Autowired
	private ApiInfoMng manager;
}