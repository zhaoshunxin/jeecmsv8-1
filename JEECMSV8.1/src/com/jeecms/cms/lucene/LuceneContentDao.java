package com.jeecms.cms.lucene;

import com.jeecms.cms.entity.main.Content;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface LuceneContentDao {

	public Integer index(IndexWriter writer, Integer siteId, Integer channelId,
			Date startDate, Date endDate, Integer startId, Integer max)
			throws CorruptIndexException, IOException;

	public Integer index(Integer siteId, Integer channelId,
							   Date startDate, Date endDate, Integer startId, Integer max)
			throws CorruptIndexException, IOException;

	public boolean add(Content content);
}
