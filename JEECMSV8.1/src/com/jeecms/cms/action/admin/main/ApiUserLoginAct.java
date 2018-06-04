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

import com.jeecms.cms.entity.main.ApiUserLogin;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;

@Controller
public class ApiUserLoginAct {
	private static final Logger log = LoggerFactory.getLogger(ApiUserLoginAct.class);

	@RequiresPermissions("apiUserLogin:v_list")
	@RequestMapping("/apiUserLogin/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		return "apiUserLogin/list";
	}

	@RequiresPermissions("apiUserLogin:v_add")
	@RequestMapping("/apiUserLogin/v_add.do")
	public String add(ModelMap model) {
		return "apiUserLogin/add";
	}

	@RequiresPermissions("apiUserLogin:v_edit")
	@RequestMapping("/apiUserLogin/v_edit.do")
	public String edit(Long id, Integer pageNo, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("apiUserLogin", manager.findById(id));
		model.addAttribute("pageNo",pageNo);
		return "apiUserLogin/edit";
	}

	@RequiresPermissions("apiUserLogin:o_save")
	@RequestMapping("/apiUserLogin/o_save.do")
	public String save(ApiUserLogin bean, HttpServletRequest request, ModelMap model) {
		bean = manager.save(bean);
		log.info("save ApiUserLogin id={}", bean.getId());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("apiUserLogin:o_update")
	@RequestMapping("/apiUserLogin/o_update.do")
	public String update(ApiUserLogin bean, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update ApiUserLogin id={}.", bean.getId());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("apiUserLogin:o_delete")
	@RequestMapping("/apiUserLogin/o_delete.do")
	public String delete(Long[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		ApiUserLogin[] beans = manager.deleteByIds(ids);
		for (ApiUserLogin bean : beans) {
			log.info("delete ApiUserLogin id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}
	
	private WebErrors validateEdit(Long id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id,  errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Long id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateDelete(Long[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Long id : ids) {
			vldExist(id,  errors);
		}
		return errors;
	}

	private boolean vldExist(Long id, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		ApiUserLogin entity = manager.findById(id);
		if(errors.ifNotExist(entity, ApiUserLogin.class, id)) {
			return true;
		}
		return false;
	}
	
	@Autowired
	private ApiUserLoginMng manager;
}