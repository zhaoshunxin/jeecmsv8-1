package com.jeecms.cms.lucene;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.util.PropertyUtils;
import com.jeecms.common.web.springmvc.RealPathResolver;

@Service
@Transactional
public class LuceneContentSvcImpl implements LuceneContentSvc {

	/*@Autowired
	private RealPathResolver realPathResolver;
	private String baseUrl = "";*/
	public SolrClient solr;
	//private ContentMng contentMng;
	private LuceneContentDao luceneContentDao;



	/**
	 * 按照查询条件
	 * @param siteId
	 * @param channelId
	 * @param startDate
	 * @param endDate
	 * @param startId
	 * @param max
	 * @param dir
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@Transactional(readOnly = true)
	public Integer createIndex(Integer siteId, Integer channelId, Date startDate, Date endDate, Integer startId, Integer max, Directory dir) throws IOException, ParseException {
		Integer id = luceneContentDao.index(siteId, channelId,
				startDate, endDate, startId, max);
		return id;
	}

	@Transactional(readOnly = true)
	public void createIndex(Content content, Directory dir) throws IOException {
		luceneContentDao.add(content);
	}

	@Transactional(readOnly = true)
	public void deleteIndex(Integer contentId, Directory dir) throws IOException, ParseException {
		try {
			solr.deleteById(contentId.toString());
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	@Transactional(readOnly = true)
	public void updateIndex(Content content, Directory dir) throws IOException, ParseException {
		Integer id = content.getId();
		if(id != null) {
			deleteIndex(id, dir);
		}
		createIndex(content);
	}

	@Transactional(readOnly = true)
	public void updateIndex(Content content) throws IOException, ParseException {
		// 复用
		updateIndex(content, null);
	}

	@Transactional(readOnly = true)
	public void createIndex(Content content) throws IOException {
		// 复用
		createIndex(content, null);
	}

	@Transactional(readOnly = true)
	public void deleteIndex(Integer contentId) throws IOException, ParseException {
		// 复用
		deleteIndex(contentId, null);
	}

	@Transactional(readOnly = true)
	public Integer createIndex(Integer siteId, Integer channelId, Date startDate, Date endDate, Integer startId, Integer max) throws IOException, ParseException {
		// 复用
		return createIndex(siteId, channelId, startDate, endDate, startId, max, null);
	}



	/****去除查询功能***/
	public Pagination searchPage(String path, String queryString, String category, String workplace, Integer siteId, Integer channelId, Date startDate, Date endDate, int pageNo, int pageSize) throws CorruptIndexException, IOException, ParseException {
		return null;
	}

	public Pagination searchPage(Directory dir, String queryString, String category, String workplace, Integer siteId, Integer channelId, Date startDate, Date endDate, int pageNo, int pageSize) throws CorruptIndexException, IOException, ParseException {
		return null;
	}

	public List<Content> searchList(String path, String queryString, String category, String workplace, Integer siteId, Integer channelId, Date startDate, Date endDate, int pageNo, int pageSize) throws CorruptIndexException, IOException, ParseException {
		return null;
	}

	public List<Content> searchList(Directory dir, String queryString, String category, String workplace, Integer siteId, Integer channelId, Date startDate, Date endDate, int first, int max) throws CorruptIndexException, IOException, ParseException {
		return null;
	}


	@Autowired
	public void setLuceneContentDao(LuceneContentDao luceneContentDao) {
		this.luceneContentDao = luceneContentDao;
	}

	/*@Autowired
	public void setContentMng(ContentMng contentMng) {
		this.contentMng = contentMng;
	}*/
}
