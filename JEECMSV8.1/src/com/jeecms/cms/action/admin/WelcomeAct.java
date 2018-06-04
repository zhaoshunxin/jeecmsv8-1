package com.jeecms.cms.action.admin;

import static com.jeecms.cms.entity.assist.CmsSiteAccessStatistic.STATISTIC_ALL;
import static com.jeecms.cms.statistic.CmsStatistic.SITEID;
import static com.jeecms.cms.statistic.CmsStatistic.STATUS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsUserMenu;
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.ContentCheck;
import com.jeecms.cms.manager.assist.CmsSiteAccessMng;
import com.jeecms.cms.manager.assist.CmsSiteAccessStatisticMng;
import com.jeecms.cms.manager.assist.CmsUserMenuMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.statistic.CmsStatistic;
import com.jeecms.cms.statistic.CmsStatistic.TimeRange;
import com.jeecms.cms.statistic.CmsStatisticSvc;
import com.jeecms.cms.web.AdminContextInterceptor;
import com.jeecms.common.util.DateUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class WelcomeAct {
	
	private static final String GROUP_DAY="day";
	private static final String GROUP_WEEK="week";
	private static final String GROUP_MONTH="month";
	private static final String GROUP_YEAR="year";
	private static final String GROUP_TOTAL="total";
	
	@RequiresPermissions("index")
	@RequestMapping("/index.do")
	public String index(HttpServletRequest request) {
		return "index";
	}

	@RequiresPermissions("map")
	@RequestMapping("/map.do")
	public String map() {
		return "map";
	}

	@RequiresPermissions("top")
	@RequestMapping("/top.do")
	public String top(HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		// 需要获得站点列表
		Set<CmsSite> siteList = user.getSites();
		model.addAttribute("siteList", siteList);
		model.addAttribute("site", site);
		model.addAttribute("siteParam", AdminContextInterceptor.SITE_PARAM);
		model.addAttribute("user", user);
		return "top";
	}

	@RequiresPermissions("main")
	@RequestMapping("/main.do")
	public String main() {
		return "main";

	}

	@RequiresPermissions("left")
	@RequestMapping("/left.do")
	public String left(HttpServletRequest request, ModelMap model) {
		CmsUser user = CmsUtils.getUser(request);
		List<CmsUserMenu>menus=userMenuMng.getList(user.getId(),10);
		model.addAttribute("menus", menus);
		return "left";
	}
	
	@RequiresPermissions("right")
	@RequestMapping("/right.do")
	public String right(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		String version = site.getConfig().getVersion();
		Properties props = System.getProperties();
		Runtime runtime = Runtime.getRuntime();
		long freeMemoery = runtime.freeMemory();
		long totalMemory = runtime.totalMemory();
		long usedMemory = totalMemory - freeMemoery;
		long maxMemory = runtime.maxMemory();
		long useableMemory = maxMemory - totalMemory + freeMemoery;
		model.addAttribute("props", props);
		model.addAttribute("freeMemoery", freeMemoery);
		model.addAttribute("totalMemory", totalMemory);
		model.addAttribute("usedMemory", usedMemory);
		model.addAttribute("maxMemory", maxMemory);
		model.addAttribute("useableMemory", useableMemory);
		model.addAttribute("version", version);
		model.addAttribute("user", user);
		model.addAttribute("site", site);
		/*
		//最新10条待审内容
		List<Content>contents=(List<Content>) contentMng.getPageByRight(null, null, user.getId(), 0, false, false, ContentStatus.prepared, user.getCheckStep(site.getId()), site.getId(), null, user.getId(), 0, 1, 10).getList();
		List<Content>newcontents=(List<Content>)contentMng.getPageByRight(null, null,  user.getId(), 0, false, false, ContentStatus.checked,  user.getCheckStep(site.getId()), site.getId(), null,user.getId(), 0, 1, 10).getList();
		model.addAttribute("contents", contents);
		model.addAttribute("newcontents", newcontents);
		*/
		getChannelStatic(request, model);
		getPvStatic(GROUP_DAY,request, model);
		return "right";
	}
	
	@RequiresPermissions("right")
	@RequestMapping("/statisticCount.do")
	public String statisticCount(String  type,
			HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		Long pv=site.getDayPvTotal();
		if(type.equals(GROUP_WEEK)){
			pv=site.getWeekPvTotal();
		}else if(type.equals(GROUP_MONTH)){
			pv=site.getMonthPvTotal();
		}else if(type.equals(GROUP_TOTAL)){
			pv=site.getPvTotal();
		}
		model.addAttribute("pv", pv);
		model.addAttribute("type", type);
		return "statistic/index/statisticCount";
	}
	
	@RequiresPermissions("right")
	@RequestMapping("/statisticCountAjax.do")
	public void statisticContentCount(String  type,
			HttpServletRequest request, 
			HttpServletResponse response,ModelMap model) {
		getContentStatic(type,request,response, model);
	}
	
	@RequiresPermissions("right")
	@RequestMapping("/statisticPv.do")
	public String statisticPv(String  type,
			HttpServletRequest request, ModelMap model) {
		getPvStatic(type, request, model);
		return "statistic/index/statisticPv";
	}
	
	private void getChannelStatic(HttpServletRequest request, ModelMap model){
		Integer siteId=CmsUtils.getSiteId(request);
		List<Channel>channelList=new ArrayList<Channel>();
		//顶层栏目
		channelList=channelMng.getTopList(siteId, false);
		model.addAttribute("channelList", channelList);
	}
	
	private void getPvStatic(String type,
			HttpServletRequest request, ModelMap model){
		Integer siteId=CmsUtils.getSiteId(request);
		List<Object[]> pvList=null;
		Date now=Calendar.getInstance().getTime();
		Date weekBegin=DateUtils.getSpecficWeekStart(now, 0);
		Date monthBegin=DateUtils.getSpecficMonthStart(now, 0);
		if(type.equals(GROUP_DAY)){
			//小时pv
			pvList=cmsAccessMng.statisticToday(siteId,null);
		}else if(type.equals(GROUP_WEEK)){
			//本周PV
			pvList=cmsAccessStatisticMng.statistic(weekBegin, now, siteId, STATISTIC_ALL,null);
		}else if(type.equals(GROUP_MONTH)){
			//本月pv
			pvList=cmsAccessStatisticMng.statistic(monthBegin, now, siteId, STATISTIC_ALL,null);
		}else if(type.equals(GROUP_YEAR)){
			//本年pv
			pvList=cmsAccessStatisticMng.statisticByYear(Calendar.getInstance().get(Calendar.YEAR), siteId,STATISTIC_ALL,null,true,null);
		}
		model.addAttribute("type", type);
		model.addAttribute("pvList", pvList);
	}
	
	private void getContentStatic(String type,
			HttpServletRequest request, HttpServletResponse response, ModelMap model){
		Integer siteId=CmsUtils.getSiteId(request);
		Map<String, Object> restrictions = new HashMap<String, Object>();
		restrictions.put(SITEID, siteId);
		restrictions.put(STATUS, ContentCheck.CHECKED);
		Date now=Calendar.getInstance().getTime();
		Date dayBegin=DateUtils.getStartDate(now);
		Date weekBegin=DateUtils.getSpecficWeekStart(now, 0);
		Date monthBegin=DateUtils.getSpecficMonthStart(now, 0);
		TimeRange dayTimeRange=TimeRange.getInstance(dayBegin, now);
		TimeRange weekTimeRange=TimeRange.getInstance(weekBegin, now);
		TimeRange monthTimeRange=TimeRange.getInstance(monthBegin, now);
		TimeRange totalTimeRange=TimeRange.getInstance(null, now);
		long checkingCount=0;
		long releaseCount=0;
		long commentCount=0;
		long guestbookCount=0;
		long memberCount=0;
		if(type.equals(GROUP_DAY)){
			releaseCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, dayTimeRange, restrictions);
			restrictions.put(STATUS, ContentCheck.CHECKING);
			checkingCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, dayTimeRange, restrictions);
			commentCount=cmsStatisticSvc.statistic(CmsStatistic.COMMENT, dayTimeRange, restrictions);
			guestbookCount=cmsStatisticSvc.statistic(CmsStatistic.GUESTBOOK, dayTimeRange, restrictions);
			memberCount=cmsStatisticSvc.statistic(CmsStatistic.MEMBER, dayTimeRange, restrictions);
		}else if(type.equals(GROUP_WEEK)){
			releaseCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, weekTimeRange, restrictions);
			restrictions.put(STATUS, ContentCheck.CHECKING);
			checkingCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, weekTimeRange, restrictions);
			commentCount=cmsStatisticSvc.statistic(CmsStatistic.COMMENT, weekTimeRange, restrictions);
			guestbookCount=cmsStatisticSvc.statistic(CmsStatistic.GUESTBOOK, weekTimeRange, restrictions);
			memberCount=cmsStatisticSvc.statistic(CmsStatistic.MEMBER, weekTimeRange, restrictions);
		}else if(type.equals(GROUP_MONTH)){
			releaseCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, monthTimeRange, restrictions);
			restrictions.put(STATUS, ContentCheck.CHECKING);
			checkingCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, monthTimeRange, restrictions);
			commentCount=cmsStatisticSvc.statistic(CmsStatistic.COMMENT, monthTimeRange, restrictions);
			guestbookCount=cmsStatisticSvc.statistic(CmsStatistic.GUESTBOOK, monthTimeRange, restrictions);
			memberCount=cmsStatisticSvc.statistic(CmsStatistic.MEMBER, monthTimeRange, restrictions);
		}else if(type.equals(GROUP_TOTAL)){
			releaseCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, totalTimeRange, restrictions);
			restrictions.put(STATUS, ContentCheck.CHECKING);
			checkingCount=cmsStatisticSvc.statistic(CmsStatistic.CONTENT, totalTimeRange, restrictions);
			commentCount=cmsStatisticSvc.statistic(CmsStatistic.COMMENT, totalTimeRange, restrictions);
			guestbookCount=cmsStatisticSvc.statistic(CmsStatistic.GUESTBOOK, totalTimeRange, restrictions);
			memberCount=cmsStatisticSvc.statistic(CmsStatistic.MEMBER, totalTimeRange, restrictions);
		}
		if(response!=null){
			JSONObject json =new JSONObject();
			try {
				json.put("releaseCount", releaseCount);
				json.put("checkingCount", checkingCount);
				json.put("commentCount", commentCount);
				json.put("guestbookCount", guestbookCount);
				json.put("memberCount", memberCount);
			} catch (JSONException e) {
				//e.printStackTrace();
			}
			ResponseUtils.renderJson(response, json.toString());
		}else{
			model.addAttribute("releaseCount", releaseCount);
			model.addAttribute("checkingCount", checkingCount);
			model.addAttribute("commentCount", commentCount);
			model.addAttribute("guestbookCount", guestbookCount);
			model.addAttribute("memberCount", memberCount);
		}
	}
	
	
	
	@Autowired
	private CmsUserMenuMng userMenuMng;
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private CmsStatisticSvc cmsStatisticSvc;
	@Autowired
	private CmsSiteAccessMng cmsAccessMng;
	@Autowired
	private CmsSiteAccessStatisticMng cmsAccessStatisticMng;
}
