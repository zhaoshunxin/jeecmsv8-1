package com.jeecms.cms.dao.main;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.Content.ContentStatus;
import com.jeecms.cms.entity.main.ContentChannel;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.common.page.Pagination;

/**
 * 内容DAO接口。
 * 
 * 用于的排序参数
 * <ul>
 * <li>0：ID降序
 * <li>1：ID升序
 * <li>2：发布时间降序
 * <li>3：发布时间升序
 * <li>4：固顶级别降序,发布时间降序
 * <li>5：固顶级别降序,发布时间升序
 * <li>6：日访问降序
 * <li>7：周访问降序
 * <li>8：月访问降序
 * <li>9：总访问降序
 * <li>10：日评论降序
 * <li>11：周评论降序
 * <li>12：月评论降序
 * <li>13：总评论降序
 * <li>14：日下载降序
 * <li>15：周下载降序
 * <li>16：月下载降序
 * <li>17：总下载降序
 * <li>18：日顶降序
 * <li>19：周顶降序
 * <li>20：月顶降序
 * <li>21：总顶降序
 * </ul>
 */
public interface ContentChannelDao {
		
	public ContentChannel updateByUpdater(Updater<ContentChannel> updater);
	
	public ContentChannel findById(Integer contentid, Integer channelid);
}