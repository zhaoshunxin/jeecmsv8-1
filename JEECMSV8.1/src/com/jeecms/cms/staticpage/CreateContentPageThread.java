package com.jeecms.cms.staticpage;

import static com.jeecms.common.web.Constants.UTF8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentPicture;
import com.jeecms.cms.manager.assist.CmsKeywordMng;
import com.jeecms.common.hibernate4.HibernateSimpleDao;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.SimplePage;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.Constants;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.manager.FtpMng;
import com.jeecms.core.web.util.FrontUtils;
import com.jeecms.core.web.util.URLHelper;
import com.jeecms.core.web.util.URLHelper.PageInfo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


public class CreateContentPageThread implements Runnable{
	private CmsKeywordMng cmsKeywordMng;
	private RealPathResolver realPathResolver;
	private Logger log;
	private FtpMng ftpMng;
	private ExecutorService es;
	private Map<String, Object> data;
	private Template tpl;
	private Content c;
	private Integer pageNo;
	private ContentPicture pic;

	public CreateContentPageThread(ContentPicture pic,Integer pageNo,Content c,Template tpl,Map<String, Object> data,ExecutorService es, FtpMng ftpMng, Logger log, RealPathResolver realPathResolver,CmsKeywordMng cmsKeywordMng) {
		this.cmsKeywordMng = cmsKeywordMng;
		this.realPathResolver = realPathResolver;
		this.log = log;
		this.ftpMng = ftpMng;
		this.es = es;
		this.tpl = tpl;
		this.c = c;
		this.pageNo = pageNo;
		this.data = data;
		this.pic = pic;
	}

	@Override
	public void run() {
		try {
			createContentPage(es,data,tpl,false,c,pageNo);
		} catch (IOException | TemplateException e) {
			//e.printStackTrace();
		}
	}
	
	private void createContentPage(ExecutorService es,Map<String, Object> data,Template tpl,boolean mobile,Content c,Integer pageNo) throws TemplateException, IOException{
		String url, real;
		File file, parent;
		CmsSite site;
		PageInfo info;
		Writer out = null;
		site = c.getSite();
		List<Ftp> list_ftps = ftpMng.getList();
		String txt = c.getTxtByNo(pageNo);
		txt = cmsKeywordMng.attachKeyword(site.getId(), txt);
		Paginable pagination = new SimplePage(pageNo, 1, c.getPageCount());
		data.put("pagination", pagination);
		url = c.getUrlStatic(pageNo);
		info = URLHelper.getPageInfo(url.substring(url.lastIndexOf("/")),
				null);
		FrontUtils.putLocation(data, url);
		FrontUtils.frontPageData(pageNo, info.getHref(), info
				.getHrefFormer(), info.getHrefLatter(), data);
		data.put("title", c.getTitleByNo(pageNo));
		data.put("txt", txt);
		data.put("pic", pic);
		if(mobile){
			real = realPathResolver.get(c.getMobileStaticFilename(pageNo));
		}else{
			real = realPathResolver.get(c.getStaticFilename(pageNo));
		}
		file = new File(real);
		if (pageNo == 1) {
			parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
		}
		try {
			// FileWriter不能指定编码确实是个问题，只能用这个代替了。
			out = new OutputStreamWriter(new FileOutputStream(file), UTF8);
			tpl.process(data, out);
			log.info("create static file: {}", file.getAbsolutePath());
		} finally {
			if (out != null) {
				out.close();
			}
		}
		String filename;
		if(mobile){
			filename=c.getMobileStaticFilename(pageNo);
		}else{
			filename=c.getStaticFilename(pageNo);
		}
		//开始：kjb同步所有ftp列表中的fpt服务，而不是只同步一个
		if(es!=null&&list_ftps.size()>0){//if(es!=null&&syncPageFtp!=null){
			for(Ftp syncPageFtp : list_ftps){
				ExecutorService es_ftp = Executors.newFixedThreadPool(Constants.DISTRIBUTE_THREAD_COUNT);
				es_ftp.execute(new DistributionThread(syncPageFtp,filename, new FileInputStream(file)));
				//System.out.println("filepath!!!: " + c.getAttachmentPaths()[0]);
				String[] attachs = c.getAttachmentPaths();
				if(attachs != null && attachs.length > 0){//ftp同步附件
					for(int attach_i = 0; attach_i < attachs.length; attach_i++){
						Executors.newFixedThreadPool(Constants.DISTRIBUTE_THREAD_COUNT).execute(new DistributionThread(syncPageFtp,attachs[attach_i], new FileInputStream(new File(realPathResolver.get(attachs[attach_i])))));
					}
				}
			}
		}
		//结束ftp同步
	}
}
