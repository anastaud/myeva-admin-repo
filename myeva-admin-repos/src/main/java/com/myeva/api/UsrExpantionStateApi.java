package com.myeva.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myeva.model.UsrExpantionState;
import com.myeva.service.contract.UsrAdminService;
import com.myeva.service.contract.UsrExpantionStateService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ValidityException;


@RestController
@PreAuthorize("hasRole('ROLE_ADMIN_ORGAS')")
@RequestMapping("/private/api/usrexpantionstate")
public class UsrExpantionStateApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(UsrExpantionStateApi.class);
	
	@Autowired
	UsrExpantionStateService USREXPANTIONSTATE_SERVICE;
	
	@Autowired
	UsrAdminService USR_SERVICE;
	
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	@GetMapping
	public  ResponseEntity<UsrExpantionState> read() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START read()");  
		}	
			
		UsrExpantionState entity = null;
		try {
			final String username=SecurityContextHolder.getContext().getAuthentication().getName();
			final long usrId=USR_SERVICE.read(username).getId();
			entity=USREXPANTIONSTATE_SERVICE.read(usrId);
		}
		catch (Exception exception) {
			final String message="### read():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<UsrExpantionState>(entity,HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   read({})",entity);
		}
		return new ResponseEntity<UsrExpantionState>(entity,HttpStatus.OK);
	}

	@PutMapping
	public  ResponseEntity<Void> save(@RequestBody UsrExpantionState entity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START save({})",entity);  
		}	
			
		try {
			USREXPANTIONSTATE_SERVICE.save(entity);;
		}
		catch (CompletenessException exception) {
			final String message="### save():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.PRECONDITION_FAILED);
		}
		catch (ValidityException exception) {
			final String message="### save():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.PRECONDITION_REQUIRED);
		}
		catch (Exception exception) {
			final String message="### save():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   save({})",entity);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
