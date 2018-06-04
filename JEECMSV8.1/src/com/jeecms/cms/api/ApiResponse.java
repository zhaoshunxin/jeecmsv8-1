package com.jeecms.cms.api;

public class ApiResponse {
	
	public ApiResponse(String body, String message, String status) {
		super();
		this.body = body;
		this.message = message;
		this.status = status;
	}

	/**
	 * 返回信息主体
	 */
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	/**
	 * API调用提示信息
	 */
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * API接口调用状态
	 */
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "{\"body\":" + body + ", \"message\":" + message + ",\"status\":" + status + "}";
	}

	private String body;
	private String message;
	private String status;
}
