package cn.ac.ict.common;

import java.util.Random;

import javax.servlet.http.HttpSession;

import cn.ac.ict.tools.Security;

public class PasswordSecurity {

	public static final String ENC_PASSWORD_KEY = "LOGIN_PASSWORD_RANDOM_ENCSTR";
	private static final String ENC_CHARACTER_LIST = "abcdefghigklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ@#$^*()_+-=";
	private static final int ENC_PASSWORD_KEY_LENGTH = 32;

	/**
	 * ��������ַ��������浽session��
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
	 * ��ȡ�û���̬�����
	 * 
	 * @param session
	 * @return
	 */
	public static String getKey(HttpSession session) {
		return (String) session.getAttribute(ENC_PASSWORD_KEY);
	}

	/**
	 * ������+KEY��Ͻ���MD5����
	 * @param md5Password
	 * @param session
	 * @return ���MD5ֵ
	 */
	public static String processPassword(String md5Password, HttpSession session) {
		return processPassword(md5Password, getKey(session));
	}
	
	/**
	 * ������+KEY��Ͻ���MD5����
	 * @param md5Password
	 * @param key
	 * @return ���MD5ֵ
	 */
	public static String processPassword(String md5Password, String key) {
		String password = key + "-" + md5Password;
		return Security.makMd5Digest(password);
	}

	/**
	 * ����ENC_PASSWORD_KEY_LENGTH���ȣ����������ENC_CHARACTER_LIST��������ɵ������
	 * 
	 * @return ����ַ�����������ĸ�����֡������ַ�
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
