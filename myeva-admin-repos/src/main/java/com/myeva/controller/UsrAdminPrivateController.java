package com.myeva.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.myeva.model.UsrAdminPrivate;
import com.myeva.service.contract.UsrAdminPrivateService;
import com.myeva.service.contract.UsrAdminService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN_ORGAS')")
@RequestMapping("/private/private")
public class UsrAdminPrivateController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UsrAdminPrivateController.class);
	
	@Autowired
	UsrAdminService USR_SERVICE;
	
	@Autowired
	UsrAdminPrivateService USRPRIVATE_SERVICE;
		
	@Autowired
    private MessageSource MESSAGE_SOURCE;
	
	private final String PAGE = "private";

			
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	private boolean isNullOrEmpty(final String str) {
		return str==null || str.length()==0 || str.isEmpty();
	}
	
	@GetMapping
	public String display(Model model,Locale locale,HttpServletRequest request) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START display()");  
		}	
		
		String forward = PAGE;
		try {
			
			////////// Récupération de l'utilisateur
			final String username=SecurityContextHolder.getContext().getAuthentication().getName();
			UsrAdminPrivate entity=USRPRIVATE_SERVICE.readByLogin(username);	
			
			// Affichage du formulaire vide (on ne ramene pas le mot de passe de la base)
			model.addAttribute("entity",entity);
		}
		catch (Exception exception) {
			final String message="### display():"+getStackTrace(exception);
			LOGGER.error(message);
			model.addAttribute("error", MESSAGE_SOURCE.getMessage("message.serviceOver", null, locale));
		}

		model.addAttribute("currentUrl", request.getRequestURI());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   display(forward={},currentUrl={})",forward,request.getRequestURI());
		}
		return forward;
	}	
	
	@PostMapping
	public String submit(
			@ModelAttribute("entity") UsrAdminPrivate entity
			,BindingResult bindingResult // ATTENTION: le BindingResult doit venir immédiatement après @ModelAttribute
			,Model model,Locale locale) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START submit({})",entity);
		}
		
		String forward = PAGE;;
		List<String> successMessages = new ArrayList<String>();
		List<String> errorMessages = new ArrayList<String>();

				
		/////////// Controles de complétude
		if (isNullOrEmpty(entity.getFirstname())) {
			errorMessages.add(MESSAGE_SOURCE.getMessage("private.message.completeness.firstname", null, locale));
		}
		if (isNullOrEmpty(entity.getLastname())) {
			errorMessages.add(MESSAGE_SOURCE.getMessage("private.message.completeness.lastname", null, locale));
		}
		if (isNullOrEmpty(entity.getPhone())) {
			errorMessages.add(MESSAGE_SOURCE.getMessage("private.message.completeness.phone", null, locale));
		}
					
		////////// Modification des données personnelles
		if (errorMessages.size()==0) {					
			try {
				final String username=SecurityContextHolder.getContext().getAuthentication().getName();
				entity.setLogin(username);
				USRPRIVATE_SERVICE.update(entity);								
				successMessages.add(MESSAGE_SOURCE.getMessage("private.message.save.acknowledgment", null, locale));				
			}
			catch (Exception exception) {
				final String message="### submit():"+getStackTrace(exception);
				LOGGER.error(message);
			}
		}

		model.addAttribute("errorMessages", errorMessages);
		model.addAttribute("successMessages", successMessages);
		
		if (LOGGER.isDebugEnabled()) {
			for (String message:successMessages) LOGGER.debug("--- successMessages: {}",message);
			for (String message:errorMessages) LOGGER.debug("--- errorMessages: {}",message);
			LOGGER.debug("<<< END   submit(forward={})",forward);
		}		
		return forward; 
	}
}

