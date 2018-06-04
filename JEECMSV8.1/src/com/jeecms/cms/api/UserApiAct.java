package com.jeecms.cms.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsWebservice;
import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.entity.main.ApiUserLogin;
import com.jeecms.cms.entity.main.CmsThirdAccount;
import com.jeecms.cms.manager.assist.CmsWebserviceMng;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.cms.manager.main.ApiUserLoginMng;
import com.jeecms.cms.manager.main.CmsThirdAccountMng;
import com.jeecms.cms.service.ImageSvc;
import com.jeecms.common.security.encoder.PwdEncoder;
import com.jeecms.common.util.AES128Util;
import com.jeecms.common.util.DateUtils;
import com.jeecms.common.util.Num62;
import com.jeecms.common.util.PropertyUtils;
import com.jeecms.common.web.HttpClientUtil;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.common.web.session.SessionProvider;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.entity.CmsConfig;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.CmsUserExt;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.manager.CmsConfigMng;
import com.jeecms.core.manager.CmsUserExtMng;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.manager.UnifiedUserMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class UserApiAct {
	
	private final String WEIXIN_JSCODE_2_SESSION_URL="weixin.jscode2sessionUrl";
	
	/**
	 * 添加会员用户
	 * @param username 用户名   必选
	 * @param email 邮箱 非必选
	 * @param loginPassword 密码  必选
	 * @param realname 真实姓名 非必选
	 * @param gender 性别 非必选
	 * @param birthdayStr 生日 格式"yyyy-MM-dd" 例如"1980-01-01" 非必选
	 * @param phone  电话 非必选
	 * @param mobile 手机 非必选
	 * @param qq qq号  非必选
	 * @param userImg 用户头像  非必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/add.jspx")
	public void userAdd(
			String username, String email, String loginPassword,
			String realname,Boolean gender,String birthdayStr,
			String phone,String mobile,String qq,String userImg,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,username,loginPassword);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				user=cmsUserMng.findByUsername(username);
				if(user==null){
					String ip = RequestUtils.getIpAddr(request);
					Map<String,String>attrs=RequestUtils.getRequestMap(request, "attr_");
					boolean disabled=false;
					CmsSite site = CmsUtils.getSite(request);
					CmsConfig config = site.getConfig();
					if(config.getMemberConfig().isCheckOn()){
						disabled=true;
					}
					CmsUserExt userExt=new CmsUserExt();
					if(StringUtils.isNotBlank(birthdayStr)){
						userExt.setBirthday(DateUtils.parseDayStrToDate(birthdayStr));
					}
					userExt.setGender(gender);
					userExt.setMobile(mobile);
					userExt.setPhone(phone);
					userExt.setQq(qq);
					userExt.setRealname(realname);
					userExt.setUserImg(userImg);
					user=cmsUserMng.registerMember(username, email, loginPassword, ip, null,null,disabled,userExt,attrs);
					cmsWebserviceMng.callWebService("false",username, loginPassword, email, userExt,CmsWebservice.SERVICE_TYPE_ADD_USER);
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/user/add.jspx",sign);
					body="{\"id\":"+"\""+user.getId()+"\"}";
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}else{
					//用户名已存在
					message=Constants.API_MESSAGE_USERNAME_EXIST;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 修改会员资料
	 * @param username 用户名   必选
	 * @param realname 真实姓名 非必选
	 * @param gender 性别 非必选
	 * @param birthdayStr 生日 格式"yyyy-MM-dd" 例如"1980-01-01" 非必选
	 * @param phone  电话 非必选
	 * @param mobile 手机 非必选
	 * @param qq qq号  非必选
	 * @param userImg 用户头像 非必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/edit.jspx")
	public void userEdit(
			String username, String realname,Boolean gender,
			String birthdayStr,String phone,String mobile,String qq,
			String userImg,String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,username);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				user=cmsUserMng.findByUsername(username);
				if(user!=null){
					CmsUserExt userExt=user.getUserExt();
					if(StringUtils.isNotBlank(birthdayStr)){
						userExt.setBirthday(DateUtils.parseDayStrToDate(birthdayStr));
					}
					userExt.setGender(gender);
					if(StringUtils.isNotBlank(mobile)){
						userExt.setMobile(mobile);
					}
					if(StringUtils.isNotBlank(phone)){
						userExt.setPhone(phone);
					}
					if(StringUtils.isNotBlank(qq)){
						userExt.setQq(qq);
					}
					if(StringUtils.isNotBlank(realname)){
						userExt.setRealname(realname);
					}
					if(StringUtils.isNotBlank(userImg)){
						userExt.setUserImg(userImg);
					}
					cmsUserExtMng.update(userExt, user);
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/user/edit.jspx",sign);
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}else{
					//用户不存在
					message=Constants.API_MESSAGE_USER_NOT_FOUND;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 修改会员密码
	 * @param username 用户名   必选
	 * @param email 邮箱 非必选
	 * @param origPwd 原密码  必选
	 * @param newPwd 新密码 必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/pwd.jspx")
	public void pwdEdit(
			String username, String origPwd,String newPwd,String email,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,username);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				user=cmsUserMng.findByUsername(username);
				if(user!=null){
					//原密码错误
					if (!cmsUserMng.isPasswordValid(user.getId(), origPwd)) {
						message=Constants.API_MESSAGE_ORIGIN_PWD_ERROR;
					}else{
						cmsUserMng.updatePwdEmail(user.getId(), newPwd, email);
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/user/pwd.jspx",sign);
						message=Constants.API_MESSAGE_SUCCESS;
						status=Constants.API_STATUS_SUCCESS;
					}
				}else{
					//用户不存在
					message=Constants.API_MESSAGE_USER_NOT_FOUND;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 获取用户信息
	 * @param username 用户名必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/get.jspx")
	public void getUserInfo(Integer https,
			String username,String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if(https==null){
			https=Constants.URL_HTTP;
		}
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		CmsSite site=CmsUtils.getSite(request);
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,username);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				user=cmsUserMng.findByUsername(username);
				if(user!=null){
					//加密返回
					try {
						/*加密有中文乱码问题
						String aesKey=apiAccount.getAesKey();
						body=AES128Util.encrypt(
								user.convertToJson(site).toString(), aesKey);
								*/
						body=user.convertToJson(site,https).toString();
					} catch (Exception e) {
						e.printStackTrace();
					}
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}else{
					//用户不存在
					message=Constants.API_MESSAGE_USER_NOT_FOUND;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 用户登录API
	 * @param username 用户名 必选
	 * @param aesPassword 加密密码 必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/login.jspx")
	public void userLogin(
			String username,String aesPassword,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,username,aesPassword);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				user=cmsUserMng.findByUsername(username);
				if(user!=null){
					String aesKey=apiAccount.getAesKey();
					//解密用户输入的密码
					String encryptPass="";
					try {
						encryptPass = AES128Util.decrypt(aesPassword, aesKey,apiAccount.getIvKey());
					} catch (Exception e) {
						//e.printStackTrace();
					}
					//验证用户密码
					if(cmsUserMng.isPasswordValid(user.getId(), encryptPass)){
						//sessionID加密后返回 ,该值作为用户数据交互识别的关键值
						//调用接口端将该值保存，调用用户数据相关接口传递加密sessionID后的值，服务器端解密后查找用户
						//暂时用户无真实登陆需求，暂不予登陆
						String sessionKey=session.getSessionId(request, response);
						apiUserLoginMng.userLogin(username, sessionKey);
						try {
							//加密返回
							body="\""+AES128Util.encrypt(sessionKey, aesKey,apiAccount.getIvKey())+"\"";
						} catch (Exception e) {
							e.printStackTrace();
						}
						apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
								appId, "/api/user/login.jspx",sign);
						message=Constants.API_MESSAGE_SUCCESS;
						status=Constants.API_STATUS_SUCCESS;
					}else{
						//密码错误
						message=Constants.API_MESSAGE_PASSWORD_ERROR;
					}
				}else{
					//用户不存在
					message=Constants.API_MESSAGE_USER_NOT_FOUND;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 用户退出API
	 * @param username 用户名 必选
	 * @param sessionKey 会话标识 必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/logout.jspx")
	public void userLogout(
			String username,String sessionKey,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,username,sessionKey);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				user=cmsUserMng.findByUsername(username);
				if(user!=null){
					String aesKey=apiAccount.getAesKey();
					String decryptSessionKey="";
					try {
						decryptSessionKey=AES128Util.decrypt(sessionKey, aesKey,apiAccount.getIvKey());
					} catch (Exception e) {
						//e.printStackTrace();
					}
					ApiUserLogin userLogin=apiUserLoginMng.findUserLogin(username, decryptSessionKey);
					//已登录则退出
					if(userLogin!=null&&StringUtils.isNotBlank(decryptSessionKey)){
						apiUserLoginMng.userLogout(username);
					}
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}else{
					//用户不存在
					message=Constants.API_MESSAGE_USER_NOT_FOUND;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 获取用户状态API
	 * @param username 用户名 必选
	 * @param sessionKey 会话标识 必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/getStatus.jspx")
	public void userGetStatus(
			String username,String sessionKey,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		CmsUser user = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,username,sessionKey);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				user=cmsUserMng.findByUsername(username);
				if(user!=null){
					String aesKey=apiAccount.getAesKey();
					String decryptSessionKey = null;
					try {
						decryptSessionKey = AES128Util.decrypt(sessionKey, aesKey,apiAccount.getIvKey());
					} catch (Exception e) {
						//e.printStackTrace();
					}
					if(StringUtils.isNotBlank(decryptSessionKey)){
						ApiUserLogin userLogin=apiUserLoginMng.findUserLogin(username, decryptSessionKey);
						if(userLogin!=null&&StringUtils.isNotBlank(decryptSessionKey)){
							message="\"login\"";
						}else{
							message="\"no login\"";
						}
						status=Constants.API_STATUS_SUCCESS;
					}else{
						message=Constants.API_MESSAGE_PARAM_ERROR;
					}
				}else{
					//用户不存在
					message=Constants.API_MESSAGE_USER_NOT_FOUND;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 微信小程序-微信用户登录获取sessionKey和openid API
	 * @param js_code 微信小程序登录code 必选
	 * @param grant_type 非必选
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/weixinLogin.jspx")
	public void weixinAppLogin(
			String js_code,String grant_type,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					{
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors=WebErrors.create(request);
		if(StringUtils.isNotBlank(grant_type)){
			grant_type="authorization_code";
		}
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,js_code);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				initWeiXinJsCode2SessionUrl();
				Map<String,String>params=new HashMap<String, String>();
				CmsConfig config=configMng.get();
				params.put("appid", config.getWeixinAppId());
				params.put("secret", config.getWeixinAppSecret());
				params.put("js_code",js_code);
				params.put("grant_type",grant_type);
				String result=HttpClientUtil.postParams(getWeiXinJsCode2SessionUrl(),
						params);
				JSONObject json;
				Object openId = null;
				try {
					json = new JSONObject(result);
					openId=json.get("openid");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String openid=null;
				if(openId!=null){
					openid=(String)openId;
				}
				if(StringUtils.isNotBlank(openid)){
					body=thirdLoginGetSessionKey(apiAccount, openid,null, 
							Constants.THIRD_SOURCE_WEIXIN_APP, request, response);
				}
				message=Constants.API_MESSAGE_SUCCESS;
				status=Constants.API_STATUS_SUCCESS;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 第三方登录API
	 * @param thirdKey 第三方key 必选
	 * @param source 第三方来源 非必选 默认微信小程序
	 * @param username 为第三方用户指定创建的用户名
	 * @param appId appID 必选
	 * @param nonce_str 随机字符串 必选
	 * @param sign 签名必选
	 */
	@RequestMapping(value = "/api/user/thirdLogin.jspx")
	public void thirdLoginApi(
			String thirdKey,String source,String username,
			String appId,String nonce_str,String sign,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		if(StringUtils.isNotBlank(source)){
			source=Constants.THIRD_SOURCE_WEIXIN_APP;
		}
		WebErrors errors=WebErrors.create(request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign,thirdKey);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if(errors.hasErrors()){
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				body=thirdLoginGetSessionKey(apiAccount, thirdKey,
						username, source, request, response);
				apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
						appId, "/api/user/thirdLogin.jspx",sign);
				message=Constants.API_MESSAGE_SUCCESS;
				status=Constants.API_STATUS_SUCCESS;
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private String thirdLoginGetSessionKey(ApiAccount apiAccount,
			String thirdKey,String username,String source,
			HttpServletRequest request,HttpServletResponse response){
		String aesKey=apiAccount.getAesKey();
		thirdKey=pwdEncoder.encodePassword(thirdKey);
		CmsThirdAccount thirdAccount=thirdAccountMng.findByKey(thirdKey);
		if(thirdAccount!=null){
			username=thirdAccount.getUsername();
		}else{
			//用户不存在,则新建用户
			//若是没有传递用户名则随机用户
			if(StringUtils.isBlank(username)){
				username=getRandomUsername();
			}else{
				//若是传递的用户名存在则随机
				if(userExist(username)){
					username=getRandomUsername();
				}
			}
			CmsUserExt userExt=new CmsUserExt();
			//第三方授权来自微信小程序
			if(source.equals(Constants.THIRD_SOURCE_WEIXIN_APP)){
				String nickName =request.getParameter("nickName");
				String avatarUrl =request.getParameter("avatarUrl");
				String gender =request.getParameter("gender");
				String province =request.getParameter("province");
				String city =request.getParameter("city");
				String country =request.getParameter("country");
				if(StringUtils.isNotBlank(gender)){
					if(gender.equals(2)){
						userExt.setGender(false);
					}else if(gender.equals(1)){
						userExt.setGender(true);
					}else{
						userExt.setGender(null);
					}
				}
				if(StringUtils.isNotBlank(nickName)){
					userExt.setRealname(nickName);
				}
				String comefrom="";
				if(StringUtils.isNotBlank(country)){
					comefrom+=country;
				}
				if(StringUtils.isNotBlank(province)){
					comefrom+=province;
				}
				if(StringUtils.isNotBlank(city)){
					comefrom+=city;
				}
				userExt.setComefrom(comefrom);
				String imageUrl="";
				if(StringUtils.isNotBlank(avatarUrl)){
					CmsConfig config=configMng.get();
					CmsSite site=CmsUtils.getSite(request);
					Ftp ftp=site.getUploadFtp();
					imageUrl=imgSvc.crawlImg(avatarUrl, config.getContextPath(), config.getUploadToDb(), config.getDbFileUri(), ftp, site.getUploadPath());
				}
				userExt.setUserImg(imageUrl);
			}
			String ip = RequestUtils.getIpAddr(request);
			boolean disabled=false;
			CmsSite site = CmsUtils.getSite(request);
			CmsConfig config = site.getConfig();
			if(config.getMemberConfig().isCheckOn()){
				disabled=true;
			}
			CmsUser user=cmsUserMng.registerMember(username, null, thirdKey, ip, null,null,disabled,userExt,null);
			cmsWebserviceMng.callWebService("false",username, thirdKey, null, userExt,CmsWebservice.SERVICE_TYPE_ADD_USER);
			//绑定新建的用户
			thirdAccount=new CmsThirdAccount();
			thirdAccount.setUsername(username);
			thirdAccount.setAccountKey(thirdKey);
			thirdAccount.setSource(source);
			thirdAccount.setUser(user);
			thirdAccountMng.save(thirdAccount);
		}
		String sessionKey=session.getSessionId(request, response);
		apiUserLoginMng.userLogin(username, sessionKey);
		JSONObject json=new JSONObject();
		try {
			//加密返回
			json.put("sessionKey", AES128Util.encrypt(sessionKey, aesKey,apiAccount.getIvKey()));
			json.put("username",username);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return json.toString();
	}
	
	private  String getRandomUsername(){
		SimpleDateFormat fomat=new SimpleDateFormat("yyyyMMddHHmmss");
		String username=fomat.format(Calendar.getInstance().getTime())+RandomStringUtils.random(5,Num62.N10_CHARS);;
		if (userExist(username)) {
			return getRandomUsername();
		}else{
			return username;
		}
	}
	
	private  boolean userExist(String username){
		if (unifiedUserMng.usernameExist(username)) {
			return true;
		}else{
			return false;
		}
	}
	
	private void initWeiXinJsCode2SessionUrl(){
		if(getWeiXinJsCode2SessionUrl()==null){
			setWeiXinJsCode2SessionUrl(PropertyUtils.getPropertyValue(
					new File(realPathResolver.get(com.jeecms.cms.Constants.JEECMS_CONFIG)),WEIXIN_JSCODE_2_SESSION_URL));
		}
	}
	
	private String weiXinJsCode2SessionUrl;
	
	public String getWeiXinJsCode2SessionUrl() {
		return weiXinJsCode2SessionUrl;
	}

	public void setWeiXinJsCode2SessionUrl(String weiXinJsCode2SessionUrl) {
		this.weiXinJsCode2SessionUrl = weiXinJsCode2SessionUrl;
	}

	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private CmsUserMng cmsUserMng;
	@Autowired
	private CmsUserExtMng cmsUserExtMng;
	@Autowired
	private SessionProvider session;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
	@Autowired
	private CmsWebserviceMng cmsWebserviceMng;
	@Autowired
	private CmsThirdAccountMng thirdAccountMng;
	@Autowired
	private UnifiedUserMng unifiedUserMng;
	@Autowired
	private ImageSvc imgSvc;
	@Autowired
	private CmsConfigMng configMng;
	@Autowired
	private PwdEncoder pwdEncoder;
	@Autowired
	private RealPathResolver realPathResolver;
	
}

