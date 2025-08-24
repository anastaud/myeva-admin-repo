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

import com.myeva.model.UsrAdmin;
import com.myeva.model.UsrAdminPasswd;
import com.myeva.service.contract.PasswordPolicyService;
import com.myeva.service.contract.UsrAdminPasswdService;
import com.myeva.service.contract.UsrAdminService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN_ORGAS')")
@RequestMapping("/private/passwd")
public class UsrAdminPasswdController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UsrAdminPasswdController.class);
	
	@Autowired
	UsrAdminService USR_SERVICE;
	
	@Autowired
	UsrAdminPasswdService USRPASSWD_SERVICE;
	
	@Autowired
	PasswordPolicyService PASSWDPOLICY_SERVICE;
	
	@Autowired
    private MessageSource MESSAGE_OURCE;
	
	private final String PAGE = "passwd";

			
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	private boolean isNullOrEmpty(final String str) {
		return str==null || str.length()==0 || str.isEmpty();
	}
	
	@GetMapping
	public String displayPasswd(Model model,Locale locale,HttpServletRequest request) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START displayPasswd()");  
		}	
		
		String forward = PAGE;
		try {
			////////// Récupération de l'utuilisateur
			final String username=SecurityContextHolder.getContext().getAuthentication().getName();
			UsrAdmin usr=USR_SERVICE.read(username);
		
			///////// Si premiere connexion on modifie le statut de premiere connexion
			if (!usr.getHasLogged()){			
				usr.setHasLogged(true);
				USR_SERVICE.updateHasLogged(usr.getId(),true);
			}
			
			////////// Si le user n'a pas modifié le premier password on désactive le bouton "Fermer"
			model.addAttribute("hasUpdatePasswd",usr.getHasUpdatePasswd());
			
			// Affichage du formulaire vide (on ne ramene pas le mot de passe de la base)
			UsrAdminPasswd entity=new UsrAdminPasswd();
			entity.setParent(usr.getId());
			model.addAttribute("entity",entity);
		}
		catch (Exception exception) {
			final String message="### displayAdminOrgas():"+getStackTrace(exception);
			LOGGER.error(message);
			model.addAttribute("error", MESSAGE_OURCE.getMessage("message.serviceOver", null, locale));
		}

		model.addAttribute("currentUrl", request.getRequestURI());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   displayPasswd(forward={},currentUrl={})",forward,request.getRequestURI());
		}
		return forward;
	}	
	
	@PostMapping
	public String sumbitPasswd(
			@ModelAttribute("entity") UsrAdminPasswd newPasswd
			,BindingResult bindingResult // ATTENTION: le BindingResult doit venir immédiatement après @ModelAttribute
			,Model model,Locale locale) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START sumbitPasswd({})",newPasswd);
			LOGGER.debug("--- value={}",newPasswd.getValue());
			LOGGER.debug("--- previousValue={}",newPasswd.getPreviousValue());
		}
		
		String forward = PAGE;;
		List<String> successMessages = new ArrayList<String>();
		List<String> errorMessages = new ArrayList<String>();
		UsrAdminPasswd currentPasswd = null;
		UsrAdmin usr = null;
		
		/////////// Controles de complétude
		if (isNullOrEmpty(newPasswd.getValue())) {
			errorMessages.add(MESSAGE_OURCE.getMessage("passwd.message.completeness.value", null, locale));
		}
		if (isNullOrEmpty(newPasswd.getPreviousValue())) {
			errorMessages.add(MESSAGE_OURCE.getMessage("passwd.message.completeness.previousValue", null, locale));
		}
		
		////////// Controle de validité
		if (errorMessages.size()==0 && !PASSWDPOLICY_SERVICE.check(newPasswd.getValue())) {
			errorMessages.add(MESSAGE_OURCE.getMessage("passwd.message.validity.value", null, locale));
		}
		
		////////// Controle de cohérence
		else {
			
			// Récupération de l'utilisateur et du mot de passe courant
			try {				
				final String username=SecurityContextHolder.getContext().getAuthentication().getName();
				usr=USR_SERVICE.read(username);
				currentPasswd =USRPASSWD_SERVICE.read(usr.getId());
			}
			catch (Exception exception) {
				final String message="### sumbitPasswd():"+getStackTrace(exception);
				LOGGER.error(message);
			}
			
			// Vérification que le mot de nouveau mot de passe est différent du courant
			if (PASSWDPOLICY_SERVICE.checkEquals(newPasswd.getValue(), currentPasswd.getValue())) {
				errorMessages.add(MESSAGE_OURCE.getMessage("passwd.message.consistency.value", null, locale));
			}
			
			// Vérification que le champ "perviousValue" saisi du nouveau mot de passe (newPasswd.perviousValue) est égale au champ "value" du mot de passe courant (currentPasswd.value)
			if (!PASSWDPOLICY_SERVICE.checkEquals(newPasswd.getPreviousValue(), currentPasswd.getValue())) {
				errorMessages.add(MESSAGE_OURCE.getMessage("passwd.message.consistency.previousValue", null, locale));
			}			
		}
		
		////////// Modification du mot de passe	ssi il n'y a pas d'erreur
		if (errorMessages.size()==0) {					
			try {
				newPasswd.setParent(usr.getId());
				USRPASSWD_SERVICE.update(newPasswd);								
				successMessages.add(MESSAGE_OURCE.getMessage("passwd.message.save.acknowledgment", null, locale));				
			}
			catch (Exception exception) {
				final String message="### sumbitPasswd():"+getStackTrace(exception);
				LOGGER.error(message);
			}
		}

		model.addAttribute("errorMessages", errorMessages);
		model.addAttribute("successMessages", successMessages);
		
		if (LOGGER.isDebugEnabled()) {
			for (String message:successMessages) LOGGER.debug("--- successMessages: {}",message);
			for (String message:errorMessages) LOGGER.debug("--- errorMessages: {}",message);
			LOGGER.debug("<<< END   sumbitReset(forward={})",forward);
		}		
		return forward; 
	}
}

