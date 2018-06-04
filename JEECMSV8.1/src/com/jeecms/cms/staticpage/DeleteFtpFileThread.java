package com.jeecms.cms.staticpage;

import java.io.IOException;
import java.io.InputStream;

import com.jeecms.core.entity.Ftp;

public class DeleteFtpFileThread  implements Runnable{
	private String path;
	private InputStream in;
	private Ftp ftp;

	public DeleteFtpFileThread(Ftp ftp,String path) {
		this.ftp = ftp;
		this.path = path;
	}

	@Override
	public void run() {
		if(ftp!=null){
			ftp.deleteFileByPath(path);
		}
	}
	
}
