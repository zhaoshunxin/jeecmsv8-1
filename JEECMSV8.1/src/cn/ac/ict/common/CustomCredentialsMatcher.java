package cn.ac.ict.common;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jeecms.common.security.encoder.PwdEncoder;

public class CustomCredentialsMatcher extends SimpleCredentialsMatcher{

	@Override
	public boolean doCredentialsMatch(AuthenticationToken authctoken, AuthenticationInfo info) {
		UsernamePasswordToken token = (UsernamePasswordToken)authctoken;
		Object accountCredentials = getCredentials(info);
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String key = PasswordSecurity.getKey(request.getSession());
		//System.out.println("!@!@" + String.valueOf(token.getPassword()));
		//System.out.println("@!@!" + pwsEncoder.encodePassword(key + "-" + accountCredentials));
		return equals(String.valueOf(token.getPassword()),pwsEncoder.encodePassword(key + "-" + accountCredentials));
	}
	
	@Autowired
	private PwdEncoder pwsEncoder;

	public PwdEncoder getPwsEncoder() {
		return pwsEncoder;
	}

	public void setPwsEncoder(PwdEncoder pwsEncoder) {
		this.pwsEncoder = pwsEncoder;
	}
	
	
}
