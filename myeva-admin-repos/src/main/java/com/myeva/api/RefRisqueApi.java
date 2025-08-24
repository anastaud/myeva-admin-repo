package com.myeva.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myeva.model.RefConditionExposition;
import com.myeva.model.RefMesurePrevention;
import com.myeva.model.RefRisque;
import com.myeva.service.contract.RefRisqueService;
import com.myeva.service.exception.ObjectNotFoundException;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN_ORGAS')")
@RequestMapping("/private/api/refrisque")
public class RefRisqueApi {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefRisqueApi.class);
	
	@Autowired
	RefRisqueService REFRISQUE_SERVICE;
	
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	@GetMapping("/{id}")
	public  ResponseEntity<RefRisque> read(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START read({})",id);  
		}	
			
		RefRisque entity=null;
		try {	
			entity=REFRISQUE_SERVICE.read(id);
		}
		catch (ObjectNotFoundException exception) {
			final String message="### read():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisque>(entity,HttpStatus.NOT_FOUND);
		}
		catch (Exception exception) {
			final String message="### read():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisque>(entity,HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   read({})",entity);
		}
		return new ResponseEntity<RefRisque>(entity,HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	public  ResponseEntity<Void> delete(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START delete(id={})",id);  
		}	
			
		try {
			REFRISQUE_SERVICE.delete(id);
		}
		catch (Exception exception) {
			final String message="### delete():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   delete(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@PostMapping("/condition")
	public  ResponseEntity<Void> add(@RequestBody RefConditionExposition child) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START add(RefRisque[id={}],{})",child.getParent(),child);	
		}	
			
		try {	
			REFRISQUE_SERVICE.add(child);
		}
		catch (Exception exception) {
			final String message="### add():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   add(RefRisque[id={}],{})",child.getParent(),child);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@PostMapping("/mesure")
	public  ResponseEntity<Void> add(@RequestBody RefMesurePrevention child) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START add(RefRisque[id={}],{})",child.getParent(),child);	
		}	
			
		try {	
			REFRISQUE_SERVICE.add(child);
		}
		catch (Exception exception) {
			final String message="### add():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   add(RefRisque[id={}],{})",child.getParent(),child);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@DeleteMapping("/condition/{id}")
	public  ResponseEntity<Void> deleteRefCondition(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START deleteRefCondition(id={})",id);  
		}	
			
		try {
			REFRISQUE_SERVICE.deleteRefCondition(id);
		}
		catch (Exception exception) {
			final String message="### deleteRefCondition():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   deleteRefCondition(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@DeleteMapping("/mesure/{id}")
	public  ResponseEntity<Void> deleteRefMesure(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START deleteRefMesure(id={})",id);  
		}	
			
		try {
			REFRISQUE_SERVICE.deleteRefMesure(id);
		}
		catch (Exception exception) {
			final String message="### deleteRefMesure():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   deleteRefMesure(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
