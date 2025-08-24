package com.myeva.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/private/refpostes")
public class RefPosteController { 

	private static final Logger LOGGER = LoggerFactory.getLogger(RefPosteController.class);
	
	@Autowired
    private MessageSource MESSAGE_OURCE;
		
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	
	private static final String PAGE ;
	static {
		PAGE = "refpostes";
	}	

	@PreAuthorize("hasAnyRole('ROLE_ADMIN_REPOS')")
	@RequestMapping(method = RequestMethod.GET)
	public String display(Model model,HttpServletRequest request,Locale locale) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START display()");  
		}	
		
		try {
			
		}
		catch (Exception exception) {
			final String message="### display():"+getStackTrace(exception);
			LOGGER.error(message);
			model.addAttribute("errorMessages", MESSAGE_OURCE.getMessage("message.serviceOver", null, locale));
		}
		
		model.addAttribute("page",PAGE);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   display(retrun='"+PAGE+"')");
		}
		return PAGE;
	}

}
