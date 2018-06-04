package com.jeecms.cms.lucene;

import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentCheck;
import com.jeecms.common.hibernate4.Finder;
import com.jeecms.common.hibernate4.HibernateBaseDao;
import com.jeecms.common.util.PropertyUtils;
import com.jeecms.common.web.springmvc.RealPathResolver;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class LuceneContentDaoImpl extends HibernateBaseDao<Content, Integer>
		implements LuceneContentDao {

	private String baseUrl = "";
	public SolrClient solr = null;

	/*public LuceneContentDaoImpl() {
		baseUrl = PropertyUtils.getPropertyValue(new File(realPathResolver.get(com.jeecms.cms.Constants.JEECMS_CONFIG)),"lucene.sorl.baseurl");
		solr = new HttpSolrClient.Builder(baseUrl).build();

	}*/
	public Integer index(IndexWriter writer, Integer siteId, Integer channelId,
			Date startDate, Date endDate, Integer startId, Integer max)
			throws CorruptIndexException, IOException {
		Finder f = Finder.create("select bean from Content bean");
		if (channelId != null) {
			f.append(" join bean.channel channel, Channel parent");
			f.append(" where channel.lft between parent.lft and parent.rgt");
			f.append(" and channel.site.id=parent.site.id");
			f.append(" and parent.id=:parentId");
			f.setParam("parentId", channelId);
		} else if (siteId != null) {
			f.append(" where bean.site.id=:siteId");
			f.setParam("siteId", siteId);
		} else {
			f.append(" where 1=1");
		}
		if (startId != null) {
			f.append(" and bean.id > :startId");
			f.setParam("startId", startId);
		}
		if (startDate != null) {
			f.append(" and bean.contentExt.releaseDate >= :startDate");
			f.setParam("startDate", startDate);
		}
		if (endDate != null) {
			f.append(" and bean.contentExt.releaseDate <= :endDate");
			f.setParam("endDate", endDate);
		}
		f.append(" and bean.status=" + ContentCheck.CHECKED);
		f.append(" order by bean.id asc");
		if (max != null) {
			f.setMaxResults(max);
		}
		Session session = getSession();
		ScrollableResults contents = f.createQuery(getSession()).setCacheMode(
				CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
		int count = 0;
		Content content = null;
		while (contents.next()) {
			content = (Content) contents.get(0);
			writer.addDocument(LuceneContent.createDocument(content));
			if (++count % 20 == 0) {
				session.clear();
			}
		}
		if (count < max || content == null) {
			// 实际数量小于指定数量，代表生成结束。如果没有任何数据，也代表生成结束。
			return null;
		} else {
			// 返回最后一个ID
			return content.getId();
		}

	}

	public Integer index(Integer siteId, Integer channelId,
						 Date startDate, Date endDate, Integer startId, Integer max)
			throws CorruptIndexException, IOException {
		Finder f = Finder.create("select bean from Content bean");
		if (channelId != null) {
			f.append(" join bean.channel channel, Channel parent");
			f.append(" where channel.lft between parent.lft and parent.rgt");
			f.append(" and channel.site.id=parent.site.id");
			f.append(" and parent.id=:parentId");
			f.setParam("parentId", channelId);
		} else if (siteId != null) {
			f.append(" where bean.site.id=:siteId");
			f.setParam("siteId", siteId);
		} else {
			f.append(" where 1=1");
		}
		if (startId != null) {
			f.append(" and bean.id > :startId");
			f.setParam("startId", startId);
		}
		if (startDate != null) {
			f.append(" and bean.contentExt.releaseDate >= :startDate");
			f.setParam("startDate", startDate);
		}
		if (endDate != null) {
			f.append(" and bean.contentExt.releaseDate <= :endDate");
			f.setParam("endDate", endDate);
		}
		f.append(" and bean.status=" + ContentCheck.CHECKED);
		f.append(" order by bean.id asc");
		if (max != null) {
			f.setMaxResults(max);
		}
		Session session = getSession();
		ScrollableResults contents = f.createQuery(getSession()).setCacheMode(
				CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
		int count = 0;
		Content content = null;
		List<Map<String, Object>> mapList = new LinkedList<Map<String, Object>>();
		while (contents.next()) {
			content = (Content) contents.get(0);
			//writer.addDocument(LuceneContent.createDocument(content));
			add(content);
			if (++count % 20 == 0) {
				session.clear();
			}
			// 每100条提交一次
			if(count % 100 == 0) {
				try {
					getSolr().commit();
				} catch (SolrServerException e) {
					e.printStackTrace();
				}
			}
		}
		// 最后提交一次
		try {
			getSolr().commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		if (count < max || content == null) {
			// 实际数量小于指定数量，代表生成结束。如果没有任何数据，也代表生成结束。
			return null;
		} else {
			// 返回最后一个ID
			return content.getId();
		}

	}

	public Map<String, Object> convert(Content content) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		map.put("id", ""+content.getId()); // ID
		map.put("type", "site");
		map.put("title",content.getTitle());// 标题
		map.put("site", content.getSiteId());// 站点
		map.put("channel_id", content.getChannel().getId());//栏目ID
		map.put("channel_name",content.getChannel().getName());//栏目名称
		//map.put("channel_id", 1230);//栏目ID
		//map.put("channel_name","测试栏目");//栏目名称
		map.put("channel_ids", content.getChannelIds());//栏目层级ID
		map.put("release_date", content.getDate());//发布日期
		map.put("sort_date", content.getDate());//排序日期
		map.put("txt", content.getTxt());// 内容
		map.put("txt1", content.getTxt1());// 内容1
		map.put("txt2", content.getTxt2());// 内容2
		map.put("url", content.getUrl());  // 访问地址
		map.put("origin", content.getOrigin()); // 来源

		// 设置扩展字段
		map.put("pub_date",content.getAttr().get("pub_date"));
		map.put("impl_date",content.getAttr().get("impl_date"));

		//法律法规相关字段
		if(content.getAttr().get("publishing_organ") != null){//发布机关
			map.put("publishing_organ",content.getAttr().get("publishing_organ"));
		}
		if(content.getAttr().get("info_num") != null){//文号
			map.put("info_num",content.getAttr().get("info_num"));
		}
		if(content.getAttr().get("pub_date") != null){//发文日期
			map.put("pub_date",content.getAttr().get("pub_date"));
		}
		if(content.getAttr().get("impl_date") != null){//实施日期
			map.put("impl_date",content.getAttr().get("impl_date"));
		}
		if(content.getAttr().get("legal_effect") != null){//法规效力
			map.put("legal_effect",content.getAttr().get("legal_effect"));
		}
		if(content.getAttr().get("effectiveness") != null){//有效性
			map.put("effectiveness",content.getAttr().get("effectiveness"));
		}
		if(content.getAttr().get("effect_description") != null){//效力说明
			map.put("effect_description",content.getAttr().get("effect_description"));
		}
		
		return map;
	}

	/**
	 * 将数据批量添加到Solr中
	 * @return 操作结果
	 */
	public boolean add(Content content) {
		Map<String, Object> map = convert(content);
		return add(map);
	}

	/**
	 * 将数据批量添加到Solr中
	 * @return 操作结果
	 */
	public boolean add(Map<String, Object> map ) {
		try {

			SolrInputDocument doc = createDocument(map);
			getSolr().add(doc);
		} catch (SolrServerException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * 按照属性清单创建Solr文档对象
	 * @param map 属性清单
	 * @return Solr文档对象
	 */
	public SolrInputDocument createDocument(Map<String, Object> map) {
		SolrInputDocument document = new SolrInputDocument();

		for (Map.Entry<String, Object> entry :map.entrySet()) {
			String solrName = formatName(entry.getKey(), entry.getValue());
			document.addField(solrName, entry.getValue());
		}

		// 如果参数中没有id属性，则随机生成
		Object objId = map.get("id");
		if(objId == null || StringUtils.isEmpty(objId.toString())) {
			document.addField("id", UUID.randomUUID());
		}

		return document;
	}

	/**
	 * 按照属性值类型格式化属性名称
	 * @param name 属性名称
	 * @param value 属性值
	 * @return 格式化后的属性名称
	 */
	public String formatName(String name, Object value) {
		if("id".equals(name))
			return "id";

		String type = "txt_cn";
		if(value instanceof  Integer) {
			type = "i";
		} else if(value instanceof  Double) {
			type = "d";
		} else if(value instanceof  Float) {
			type = "f";
		} else if(value instanceof  Long) {
			type = "l";
		} else if(value instanceof  Boolean) {
			type = "b";
		} else if(value instanceof  Date) {
			type = "dt";
		} else if(value instanceof  Integer[]) {
			type = "is";
		} else if(value instanceof  Double[]) {
			type = "ds";
		} else if(value instanceof  Float[]) {
			type = "fs";
		} else if(value instanceof  Long[]) {
			type = "ls";
		} else if(value instanceof  Boolean[]) {
			type = "bs";
		} else if(value instanceof  String[]) {
			type = "ss";
		}

		return name + "_" + type;
	}

	@Override
	protected Class<Content> getEntityClass() {
		return Content.class;
	}
	
	@Autowired
	private RealPathResolver realPathResolver;

	public SolrClient getSolr() {
		if(solr != null){
			return solr;
		}else{
			baseUrl = PropertyUtils.getPropertyValue(new File(realPathResolver.get(com.jeecms.cms.Constants.JEECMS_CONFIG)),"lucene.sorl.baseurl");
			solr = new HttpSolrClient.Builder(baseUrl).build();
			return solr;
		}
		
	}
	
	
}
