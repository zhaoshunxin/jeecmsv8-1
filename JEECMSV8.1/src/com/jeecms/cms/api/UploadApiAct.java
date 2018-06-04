package com.jeecms.cms.api;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.jeecms.cms.entity.main.ApiAccount;
import com.jeecms.cms.entity.main.ApiRecord;
import com.jeecms.cms.manager.main.ApiAccountMng;
import com.jeecms.cms.manager.main.ApiRecordMng;
import com.jeecms.common.image.ImageScale;
import com.jeecms.common.image.ImageUtils;
import com.jeecms.common.upload.FileRepository;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.entity.CmsGroup;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.entity.MarkConfig;
import com.jeecms.core.manager.CmsGroupMng;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.manager.DbFileMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class UploadApiAct {
	
	/**
	 * 文件上传API
	 * @param siteId 站点id 非必选 默认当前站
	 * @param mark 水印 true有 false 无  非必选 默认则系统默认配置
	 * @param type 上传类型  图片image 视频vedio  附件attach 必选
	 * @param file 文件 必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 */
	@RequestMapping("/api/upload/o_upload.jspx")
	public void upload(
			Integer siteId,Boolean mark,String type,
			@RequestParam(value = "uploadFile", required = false) MultipartFile file,
			String appId,String nonce_str,String sign,
			HttpServletRequest request, HttpServletResponse response) {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors = validate(type,file, request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if (errors.hasErrors()) {
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				CmsSite site = CmsUtils.getSite(request);
				if(siteId!=null){
					site=siteMng.findById(siteId);
				}
				MarkConfig conf = site.getConfig().getMarkConfig();
				if (mark == null) {
					mark = conf.getOn();
				}
				String origName = file.getOriginalFilename();
				String ext = FilenameUtils.getExtension(origName).toLowerCase(
						Locale.ENGLISH);
				try {
					String fileUrl;
					if (site.getConfig().getUploadToDb()) {
						String dbFilePath = site.getConfig().getDbFileUri();
						if (mark&&type.equals("image")) {
							File tempFile = mark(file, conf);
							fileUrl = dbFileMng.storeByExt(site.getUploadPath(),
									ext, new FileInputStream(tempFile));
							tempFile.delete();
						} else {
							fileUrl = dbFileMng.storeByExt(site.getUploadPath(),
									ext, file.getInputStream());
						}
						// 加上访问地址
						fileUrl = request.getContextPath() + dbFilePath + fileUrl;
					} else if (site.getUploadFtp() != null) {
						Ftp ftp = site.getUploadFtp();
						String ftpUrl = ftp.getUrl();
						if (mark&&type.equals("image")) {
							File tempFile = mark(file, conf);
							fileUrl = ftp.storeByExt(site.getUploadPath(), ext,
									new FileInputStream(tempFile));
							tempFile.delete();
						} else {
							fileUrl = ftp.storeByExt(site.getUploadPath(), ext, file
									.getInputStream());
						}
						// 加上url前缀
						fileUrl = ftpUrl + fileUrl;
					} else {
						if (mark&&type.equals("image")) {
							File tempFile = mark(file, conf);
							fileUrl = fileRepository.storeByExt(site.getUploadPath(), ext,
									tempFile);
							tempFile.delete();
						} else {
							fileUrl = fileRepository.storeByExt(site.getUploadPath(), ext,
									file);
						}
						String ctx = request.getContextPath();
						if(StringUtils.isNotBlank(ctx)){
							fileUrl = ctx + fileUrl;
						}
					}
					body="{\"uploadPath\":"+"\""+fileUrl+"\"}";
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/upload/o_upload_image.jspx",sign);
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}catch (Exception e) {
					e.printStackTrace();
					message=Constants.API_MESSAGE_UPLOAD_ERROR;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 文库文档上传API
	 * @param siteId 站点id 非必选 默认当前站
	 * @param file 文件 必选
	 * @param appId appid 必选
	 * @param nonce_str 随机字符串  必选
	 * @param sign 签名 必选
	 */
	@RequestMapping("/api/upload/o_upload_doc.jspx")
	public void uploadDoc(
			Integer siteId,
			@RequestParam(value = "uploadFile", required = false) MultipartFile file,
			String appId,String nonce_str,String sign,
			HttpServletRequest request, HttpServletResponse response) {
		String body="\"\"";
		String message="\"\"";
		String status=Constants.API_STATUS_FAIL;
		WebErrors errors = validateDoc(file, request);
		ApiAccount apiAccount = null;
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, appId,
				nonce_str,sign);
		if(!errors.hasErrors()){
			apiAccount=apiAccountMng.findByAppId(appId);
			//验证签名
			errors=ApiValidate.validateSign(request, errors, apiAccount, sign);
		}
		if (errors.hasErrors()) {
			message="\""+errors.getErrors().get(0)+"\"";
		}else{
			//签名数据不可重复利用
			ApiRecord record=apiRecordMng.findBySign(sign, appId);
			if(record!=null){
				message=Constants.API_MESSAGE_REQUEST_REPEAT;
			}else{
				CmsSite site = CmsUtils.getSite(request);
				if(siteId!=null){
					site=siteMng.findById(siteId);
				}
				String origName = file.getOriginalFilename();
				String ext = FilenameUtils.getExtension(origName).toLowerCase(
						Locale.ENGLISH);
				try {
					String fileUrl;
					fileUrl = fileRepository.storeByExt(site.getLibraryPath(), ext,
							file);
					body="{\"uploadPath\":"+"\""+fileUrl+"\"}";
					apiRecordMng.callApiRecord(RequestUtils.getIpAddr(request),
							appId, "/api/upload/o_upload_doc.jspx",sign);
					message=Constants.API_MESSAGE_SUCCESS;
					status=Constants.API_STATUS_SUCCESS;
				}catch (Exception e) {
					e.printStackTrace();
					message=Constants.API_MESSAGE_UPLOAD_ERROR;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(body, message, status);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private WebErrors validate(String type,MultipartFile file,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (file == null) {
			errors.addErrorString("imageupload error noFileToUpload");
			return errors;
		}
		if(type==null){
			errors.addErrorString("upload error noSelectType");
			return errors;
		}
		if(!type.equals("image")&&!type.equals("attach")
				&&!type.equals("vedio")){
			errors.addErrorString("upload error noSupportType");
			return errors;
		}
		String filename=file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(filename);
		if(filename!=null&&(filename.contains("/")||filename.contains("\\")||filename.indexOf("\0")!=-1)){
			errors.addErrorString("upload error filename");
			return errors;
		}
		if(type.equals("image")){
			//图片校验
			if (!ImageUtils.isValidImageExt(ext)) {
				errors.addErrorString("imageupload error notSupportExt");
				return errors;
			}
			try {
				if (!ImageUtils.isImage(file.getInputStream())) {
					errors.addErrorString("imageupload error notImage");
					return errors;
				}
			} catch (IOException e) {
				errors.addErrorString("imageupload error ioError");
				return errors;
			}
		}else{
			CmsGroup defGroup=groupMng.getRegDef();
			//非允许的后缀
			if(!defGroup.isAllowSuffix(ext)){
				errors.addErrorString("upload error invalidsuffix");
				return errors;
			}
		}
		return errors;
	}
	
	private WebErrors validateDoc(MultipartFile file,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (file == null) {
			errors.addErrorString("imageupload error noFileToUpload");
			return errors;
		}
		String filename=file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(filename);
		if(filename!=null&&(filename.contains("/")||filename.contains("\\")||filename.indexOf("\0")!=-1)){
			errors.addErrorString("upload error filename");
			return errors;
		}
		CmsGroup defGroup=groupMng.getRegDef();
		//非允许的后缀
		if(!defGroup.isAllowSuffix(ext)){
			errors.addErrorString("upload error invalidsuffix");
			return errors;
		}
		//单文库文件最大值
		Integer maxSize=defGroup.getAllowFileSize();
		if (!maxSize.equals(0) && file.getSize() > maxSize * 1024) {
			errors.addErrorString("error uploadOverSize");
		}
		return errors;
	}

	private File mark(MultipartFile file, MarkConfig conf) throws Exception {
		String path = System.getProperty("java.io.tmpdir");
		File tempFile = new File(path, String.valueOf(System
				.currentTimeMillis()));
		file.transferTo(tempFile);
		boolean imgMark = !StringUtils.isBlank(conf.getImagePath());
		if (imgMark) {
			File markImg = new File(realPathResolver.get(conf.getImagePath()));
			imageScale.imageMark(tempFile, tempFile, conf.getMinWidth(), conf
					.getMinHeight(), conf.getPos(), conf.getOffsetX(), conf
					.getOffsetY(), markImg);
		} else {
			imageScale.imageMark(tempFile, tempFile, conf.getMinWidth(), conf
					.getMinHeight(), conf.getPos(), conf.getOffsetX(), conf
					.getOffsetY(), conf.getContent(), Color.decode(conf
					.getColor()), conf.getSize(), conf.getAlpha());
		}
		return tempFile;
	}

	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private DbFileMng dbFileMng;
	@Autowired
	private ImageScale imageScale;
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private ApiRecordMng apiRecordMng;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private CmsSiteMng siteMng;
	@Autowired
	private CmsGroupMng groupMng;
}

