package com.jeecms.cms.action.admin.main;

import static com.jeecms.common.page.SimplePage.cpn;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.WebErrors;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.ResponseUtils;

@Controller
public class ApiAccountAct {
	private static final Logger log = LoggerFactory.getLogger(ApiAccountAct.class);

	@RequiresPermissions("apiAccount:v_list")
	@RequestMapping("/apiAccount/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		return "apiAccount/list";
	}

	@RequiresPermissions("apiAccount:v_add")
	@RequestMapping("/apiAccount/v_add.do")
	public String add(ModelMap model) {
		return "apiAccount/add";
	}

	@RequiresPermissions("apiAccount:v_edit")
	@RequestMapping("/apiAccount/v_edit.do")
	public String edit(Integer id, Integer pageNo, 
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("apiAccount", manager.findById(id));
		model.addAttribute("pageNo",pageNo);
		return "apiAccount/edit";
	}

	@RequiresPermissions("apiAccount:o_save")
	@RequestMapping("/apiAccount/o_save.do")
	public String save(ApiAccount bean, HttpServletRequest request, ModelMap model) {
		bean = manager.save(bean);
		log.info("save ApiAccount id={}", bean.getId());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("apiAccount:o_update")
	@RequestMapping("/apiAccount/o_update.do")
	public String update(ApiAccount bean,String appKey,
			String aesKey,String ivKey,Integer pageNo, 
			HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean,appKey,aesKey,ivKey);
		log.info("update ApiAccount id={}.", bean.getId());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("apiAccount:o_delete")
	@RequestMapping("/apiAccount/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		ApiAccount[] beans = manager.deleteByIds(ids);
		for (ApiAccount bean : beans) {
			log.info("delete ApiAccount id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}
	
	@RequiresPermissions("apiAccount:v_exist")
	@RequestMapping("/apiAccount/v_exist.do")
	public void appIdExist(String appId,HttpServletResponse response) {
		if(StringUtils.isNotBlank(appId)){
			ApiAccount account=manager.findByAppId(appId);
			if(account!=null){
				ResponseUtils.renderJson(response, "false");
			}else{
				ResponseUtils.renderJson(response, "true");
			}
		}else{
			ResponseUtils.renderJson(response, "false");
		}
		
		
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
		ApiAccount entity = manager.findById(id);
		if(errors.ifNotExist(entity, ApiAccount.class, id)) {
			return true;
		}
		return false;
	}
	
	@Autowired
	private ApiAccountMng manager;
}