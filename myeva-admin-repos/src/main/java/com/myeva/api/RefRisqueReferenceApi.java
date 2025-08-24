package com.myeva.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.myeva.model.RefRisqueReference;
import com.myeva.model.RefRisqueReferenceCondition;
import com.myeva.model.RefRisqueReferenceMesure;
import com.myeva.service.contract.RefRisqueReferenceService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.UnicityException;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN_ORGAS')")
@RequestMapping("/private/api/refrisquereference")
public class RefRisqueReferenceApi { 
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefRisqueReferenceApi.class);
	
	@Autowired
	RefRisqueReferenceService ENTITY_SERVICE;
	
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	@GetMapping("/head")
	public ResponseEntity<List<RefRisqueReference>> heads() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START heads()");
		}

		List<RefRisqueReference> entitys = new ArrayList<RefRisqueReference>();
		try {			
			entitys.addAll(ENTITY_SERVICE.heads());
		} 
		catch (Exception exception) {
			final String message = "### heads():" + getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<List<RefRisqueReference>>(entitys,HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			//for (RefRisqueReference element:entities) LOGGER.debug("--- {}",element);
			LOGGER.debug("<<< END   heads(size={})",entitys.size());
		}
		return new ResponseEntity<List<RefRisqueReference>>(entitys,HttpStatus.OK);
	}	

	@GetMapping
	public ResponseEntity<List<RefRisqueReference>> reads() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START reads()");
		}

		List<RefRisqueReference> entitys = new ArrayList<RefRisqueReference>();
		try {			
			entitys.addAll(ENTITY_SERVICE.reads());
		} 
		catch (Exception exception) {
			final String message = "### reads():" + getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<List<RefRisqueReference>>(entitys,HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			//for (RefRisqueReference element:entities) LOGGER.debug("--- {}",element);
			LOGGER.debug("<<< END   reads(size={})",entitys.size());
		}
		return new ResponseEntity<List<RefRisqueReference>>(entitys,HttpStatus.OK);
	}	
	
	@GetMapping("/{id}")
	public  ResponseEntity<RefRisqueReference> read(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START read(id={})",id);  
		}	
			
		RefRisqueReference entity=null;
		try {	
			entity=ENTITY_SERVICE.read(id);
		}
		catch (ObjectNotFoundException exception) {
			final String message="### read():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReference>(entity,HttpStatus.NOT_FOUND);
		}
		catch (Exception exception) {
			final String message="### read():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReference>(entity,HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   read({})",entity);
		}
		return new ResponseEntity<RefRisqueReference>(entity,HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<RefRisqueReference> create(@RequestBody RefRisqueReference entity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START create({})",entity);
		}
			
		try {	
			entity=ENTITY_SERVICE.create(entity);						
		}
		catch (CompletenessException exception) {
			final String message="### create():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReference>(entity,HttpStatus.PRECONDITION_FAILED);
		}
		catch (UnicityException exception) {
			final String message="### create():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReference>(entity,HttpStatus.CONFLICT);
		}
		catch (Exception exception) { 
			final String message="### create():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReference>(entity,HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   create({})",entity);
		}
		return new ResponseEntity<RefRisqueReference>(entity,HttpStatus.OK);
	}
	
	@PostMapping("/condition") 
	public ResponseEntity<RefRisqueReferenceCondition> addCondition(@RequestBody RefRisqueReferenceCondition leaf) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START addCondition({})",leaf);
		}
			
		try {	
			ENTITY_SERVICE.addCondition(leaf);						
		}
		catch (CompletenessException exception) {
			final String message="### addCondition():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReferenceCondition>(leaf,HttpStatus.PRECONDITION_FAILED);
		}
		catch (UnicityException exception) {
			final String message="### addCondition():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReferenceCondition>(leaf,HttpStatus.CONFLICT);
		}
		catch (Exception exception) { 
			final String message="### addCondition():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReferenceCondition>(leaf,HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   addCondition({})",leaf);
		}
		return new ResponseEntity<RefRisqueReferenceCondition>(leaf,HttpStatus.OK);
	}
	
	@PostMapping("/mesure") 
	public ResponseEntity<RefRisqueReferenceMesure> addMesure(@RequestBody RefRisqueReferenceMesure leaf) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START addMesure({})",leaf);
		}
			
		try {	
			ENTITY_SERVICE.addMesure(leaf);						
		}
		catch (CompletenessException exception) {
			final String message="### addMesure():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReferenceMesure>(leaf,HttpStatus.PRECONDITION_FAILED);
		}
		catch (UnicityException exception) {
			final String message="### addMesure():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReferenceMesure>(leaf,HttpStatus.CONFLICT);
		}
		catch (Exception exception) { 
			final String message="### addMesure():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefRisqueReferenceMesure>(leaf,HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   addMesure({})",leaf);
		}
		return new ResponseEntity<RefRisqueReferenceMesure>(leaf,HttpStatus.OK);
	}
	
	@PutMapping
	public ResponseEntity<Void> update(@RequestBody RefRisqueReference entity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START update({})",entity);
		}
				
		try {	
			ENTITY_SERVICE.update(entity);						
		}
		catch (CompletenessException exception) {
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.PRECONDITION_FAILED);
		}
		catch (UnicityException exception) {
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		}
		catch (Exception exception) { 
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   update({})",entity);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@PutMapping("/seq") 
	public ResponseEntity<Void> update(@RequestBody List<RefRisqueReference> entitys) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START update(List<RefRisqueReference>)");
		}
				
		try {	
			ENTITY_SERVICE.update(entitys);						
		}
		catch (Exception exception) { 
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   update(ResponseEntity<Void>)");
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}") 
	public ResponseEntity<Void> delete(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START delete(id={})",id);  
		}
				
		try {	
			ENTITY_SERVICE.delete(id);						
		}
		catch (Exception exception) { 
			final String message="### delete():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   update(ResponseEntity<Void>)");
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
		
	@DeleteMapping("/condition/{id}") 
	public ResponseEntity<Void> deleteCondition(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START deleteCondition()"); 
		}
				
		try {	
			ENTITY_SERVICE.deleteConditions(List.of(id));						
		}
		catch (Exception exception) { 
			final String message="### deleteCondition():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   deleteCondition()");
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
		
	@DeleteMapping("/mesure/{id}") 
	public ResponseEntity<Void> deleteMesure(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START deleteMesure()");  
		}
				
		try {	
			ENTITY_SERVICE.deleteMesures(List.of(id));						
		}
		catch (Exception exception) { 
			final String message="### deleteMesure():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   deleteMesure()");
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
