package cn.ac.ict.common;

import java.util.Random;

import javax.servlet.http.HttpSession;

import cn.ac.ict.tools.Security;

public class PasswordSecurity {

	public static final String ENC_PASSWORD_KEY = "LOGIN_PASSWORD_RANDOM_ENCSTR";
	private static final String ENC_CHARACTER_LIST = "abcdefghigklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ@#$^*()_+-=";
	private static final int ENC_PASSWORD_KEY_LENGTH = 32;

	/**
	 * 生成随机字符串并保存到session中
	 * 
	 * @param session
	 * @return
	 */
	public static String generatorKey(HttpSession session) {
		String dynamicKey = getDynamicKey();
		session.setAttribute(ENC_PASSWORD_KEY, dynamicKey);
		return dynamicKey;
	}

	/**
	 * 获取用户动态随机数
	 * 
	 * @param session
	 * @return
	 */
	public static String getKey(HttpSession session) {
		return (String) session.getAttribute(ENC_PASSWORD_KEY);
	}

	/**
	 * 将密码+KEY组合进行MD5处理
	 * @param md5Password
	 * @param session
	 * @return 组合MD5值
	 */
	public static String processPassword(String md5Password, HttpSession session) {
		return processPassword(md5Password, getKey(session));
	}
	
	/**
	 * 将密码+KEY组合进行MD5处理
	 * @param md5Password
	 * @param key
	 * @return 组合MD5值
	 */
	public static String processPassword(String md5Password, String key) {
		String password = key + "-" + md5Password;
		return Security.makMd5Digest(password);
	}

	/**
	 * 按照ENC_PASSWORD_KEY_LENGTH长度，随机生成由ENC_CHARACTER_LIST中内容组成的随机数
	 * 
	 * @return 随机字符串，包括字母、数字、特殊字符
	 */
	public static String getDynamicKey() {
		Random r = new Random();
		String key = "";
		for (int i = 0; i < ENC_PASSWORD_KEY_LENGTH; i++) {
			key += ENC_CHARACTER_LIST.charAt(r.nextInt(ENC_CHARACTER_LIST
					.length()));
		}
		return key;
	}
	
	public static void main(String[] args) {
		System.out.println(Security.makMd5Digest("1234567890"));
	}

}
