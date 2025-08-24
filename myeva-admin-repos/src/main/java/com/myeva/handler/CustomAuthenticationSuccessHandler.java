package com.myeva.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.myeva.model.UsrAdmin;
import com.myeva.model.UsrAdminLog;
import com.myeva.service.contract.UsrAdminLogService;
import com.myeva.service.contract.UsrAdminService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
	
	@Autowired
	UsrAdminService USR_SERVICE;
	
	@Autowired
	UsrAdminLogService USRLOG_SERVICE;
	
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,HttpServletResponse response,Authentication authentication ) throws IOException, ServletException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START onAuthenticationSuccess()");  
		}	
		
		String redirect = "/private/refpostes";
		try {
			final String username=authentication.getName();
			UsrAdmin usr=USR_SERVICE.read(username);
			UsrAdminLog log=new UsrAdminLog();
			log.setParent(usr.getId());
			log.setInOut("IN");
			log.setSessionId(request.getSession().getId());
			USRLOG_SERVICE.create(log);			
		}
		catch (Exception exception) {
			final String message="### onAuthenticationSuccess():"+getStackTrace(exception);
			LOGGER.error(message);
			redirect="/public/authentication/login?error";
		}
		      
		response.sendRedirect(redirect);
		  
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   onAuthenticationSuccess(sendRedirect={})",redirect);
		}
	}
}
