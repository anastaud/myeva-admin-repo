package com.myeva.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.myeva.model.UsrAdmin;
import com.myeva.model.UsrAdminLog;
import com.myeva.service.contract.HealthService;
import com.myeva.service.contract.UsrAdminLogService;
import com.myeva.service.contract.UsrAdminService;
import com.myeva.service.exception.ObjectNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping
public class AuthenticationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);
	
	@Autowired
	UsrAdminService USR_SERVICE;
	
	@Autowired
	UsrAdminLogService USRLOG_SERVICE;

	@Autowired
	HealthService HEALTH_SERVICE;
	
	@Autowired
    private MessageSource MESSAGE_OURCE;
	
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	// ATTENTION: ici l'url c'est /private/***
	@GetMapping("/private/authentication/logout")
    public String processLogout(HttpServletRequest request) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START processLogout()");
		}

		final String username=SecurityContextHolder.getContext().getAuthentication().getName();
		try {			
			UsrAdmin usr=USR_SERVICE.read(username);			
			UsrAdminLog log=new UsrAdminLog();
			log.setParent(usr.getId());
			log.setInOut("OUT");
			log.setSessionId(request.getSession().getId());
			USRLOG_SERVICE.create(log);
		}
		catch (Exception exception) {
			final String message="### processLogout():"+getStackTrace(exception);
			LOGGER.error(message);
		}
		
		
		LOGGER.info(">>> <<<< Lougout user {}",username);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<<   END processLogout({})",username);
		}
        return "redirect:/public/authentication/login?logout";
    }

	@GetMapping("/public/authentication/login")
    public String displayLogin(Model model,Locale locale) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START displayLogin()");
		}
		
		String page = "authentication-login";
		List<String> errorMessages = new ArrayList<String>();
		try {
			boolean serviceIsDown=!HEALTH_SERVICE.isUp();
			if (serviceIsDown) {
				errorMessages.add(MESSAGE_OURCE.getMessage("authentication.login.message.serviceIsDown", null, locale));
				model.addAttribute("errorMessages", errorMessages);				
			}
			model.addAttribute("serviceIsDown",serviceIsDown);
		}
		catch (Exception exception) {
			final String message="### displayLogin():"+getStackTrace(exception);
			LOGGER.error(message);
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<<   END displayLogin(page={})",page);
		}
        return page; 
    }
	
	
	@GetMapping("/public/authentication/reset")
    public String displayReset() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START displayReset()");
		}
		
		String page = "authentication-reset";
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<<   END displayReset(page={})",page);
		}
        return page; 
    }
	
	@PostMapping("/public/authentication/reset")
	public String sumbitReset(Model model,Locale locale,@ModelAttribute UsrAdmin entity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START sumbitReset({})",entity);
		}
		
		String page = "authentication-reset";
		List<String> successMessages = new ArrayList<String>();
		List<String> errorMessages = new ArrayList<String>();
		
		if (entity.getEmail()==null || entity.getEmail().length()==0) {
			errorMessages.add(MESSAGE_OURCE.getMessage("authentication.reset.label.reset.completeness", null, locale));
		}
		else {
			boolean found=true;
			try {
				entity=USR_SERVICE.read(entity.getEmail());
			}
			catch (ObjectNotFoundException exception) {
				found=false;
			}
			catch (Exception exception) {
				final String message="### sumbitReset():"+getStackTrace(exception);
				LOGGER.error(message);
			}
			
			if (!found || entity.getId()==0) {
				errorMessages.add(MESSAGE_OURCE.getMessage("authentication.reset.label.reset.consistency", null, locale)+": "+entity.getEmail());
			}
			else if (isDateWithinLastHour(entity.getLastResetDate())) {
				errorMessages.add(MESSAGE_OURCE.getMessage("authentication.reset.label.reset.spam", null, locale));
			}
			else {
				try {
					USR_SERVICE.reset(entity.getId());
					successMessages.add(MESSAGE_OURCE.getMessage("authentication.reset.label.reset.acknowledgement", null, locale));
				}
				catch (Exception exception) {
					final String message="### sumbitReset():"+getStackTrace(exception);
					LOGGER.error(message);
				}
			}
		}
		model.addAttribute("errorMessages", errorMessages);
		model.addAttribute("successMessages", successMessages);
		
		if (LOGGER.isDebugEnabled()) {
			for (String message:successMessages) LOGGER.debug("--- successMessages: {}",message);
			for (String message:errorMessages) LOGGER.debug("--- errorMessages: {}",message);
			LOGGER.debug("<<< END   sumbitReset(page={})",page);
		}		
		return page; 
	}
	
	private boolean isDateWithinLastHour(Date dateToCheck) {
        Date now = new Date();
        long differenceInMillis = now.getTime() - dateToCheck.getTime();
        return differenceInMillis < 3600 * 1000;
    }
}
