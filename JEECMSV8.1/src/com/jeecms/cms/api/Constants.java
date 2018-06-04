package com.jeecms.cms.api;

public class Constants {
	public static final String THIRD_SOURCE_WEIXIN_APP="weixinApp";
	
	//内容地址返回http
	public static final int URL_HTTP=1;
	//内容地址返回https
	public static final int URL_HTTPS=0;
	/**
	 * 图片占位符开始
	 */
	public static final String 	API_PLACEHOLDER_IMAGE_BEGIN="_img_start_";
	/**
	 * 图片占位符结束
	 */
	public static final String 	API_PLACEHOLDER_IMAGE_END="_img_end_";
	/**
	 * 视频占位符开始
	 */
	public static final String 	API_PLACEHOLDER_VIDEO_BEGIN="_video_start_";
	/**
	 * 视频占位符结束
	 */
	public static final String 	API_PLACEHOLDER_VIDEO_END="_video_end_";
	/**
	 * API接口调用状态-成功
	 */
	public static final String 	API_STATUS_SUCCESS="\"true\"";
	/**
	 * API接口调用状态失败
	 */
	public static final String 	API_STATUS_FAIL="\"false\"";
	/**
	 * API接口消息-成功
	 */
	public static final String 	API_MESSAGE_SUCCESS="\"success\"";
	/**
	 * API接口消息-缺少参数
	 */
	public static final String 	API_MESSAGE_PARAM_REQUIRED="\"param required\"";
	/**
	 * API接口消息-参数错误
	 */
	public static final String 	API_MESSAGE_PARAM_ERROR="\"param error\"";
	
	/**
	 * API接口消息-参数错误
	 */
	public static final String 	API_MESSAGE_APP_PARAM_ERROR="\"appId not exist or appId disabled\"";
	/**
	 * API接口消息-用户未找到
	 */
	public static final String 	API_MESSAGE_USER_NOT_FOUND="\"user not found\"";
	/**
	 * API接口消息-内容未找到
	 */
	public static final String 	API_MESSAGE_CONTENT_NOT_FOUND="\"content not found\"";
	/**
	 * API接口消息-用户未登录
	 */
	public static final String 	API_MESSAGE_USER_NOT_LOGIN="\"user not login\"";
	/**
	 * API接口消息-SESSION错误
	 */
	public static final String 	API_MESSAGE_SESSION_ERROR="\"session error\"";
	/**
	 * API接口消息-密码错误
	 */
	public static final String 	API_MESSAGE_PASSWORD_ERROR="\"password error\"";
	/**
	 * API接口消息-用户名已存在
	 */
	public static final String 	API_MESSAGE_USERNAME_EXIST="\"username exist\"";
	/**
	 * API接口消息-原密码错误已存在
	 */
	public static final String 	API_MESSAGE_ORIGIN_PWD_ERROR="\"origin password invalid\"";
	/**
	 * API接口消息-原密码错误已存在
	 */
	public static final String 	API_MESSAGE_UPLOAD_ERROR="\"upload file error!\"";
	/**
	 * API接口消息-订单编号已经使用
	 */
	public static final String 	API_MESSAGE_ORDER_NUMBER_USED="\"order number used\"";
	/**
	 * API接口消息-订单编号错误
	 */
	public static final String 	API_MESSAGE_ORDER_NUMBER_ERROR="\"order number error\"";
	/**
	 * API接口消息-订单金额不足
	 */
	public static final String 	API_MESSAGE_ORDER_AMOUNT_NOT_ENOUGH="\"order amount not enough\"";
	/**
	 * API接口消息-重复请求API
	 */
	public static final String 	API_MESSAGE_REQUEST_REPEAT="\"request api repeat\"";
	
	
	public static final String 	API_ARRAY_SPLIT_STR=",";
	public static final String 	API_LIST_SPLIT_STR=";";
}
