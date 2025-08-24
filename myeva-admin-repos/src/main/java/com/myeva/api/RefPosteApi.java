package com.myeva.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myeva.model.RefPoste;
import com.myeva.model.RefProfession;
import com.myeva.model.RefRisque;
import com.myeva.model.RefRisqueReference;
import com.myeva.service.contract.RefPosteService;
import com.myeva.service.contract.RefProfessionService;
import com.myeva.service.contract.RefRisqueReferenceService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.UnicityException;
import com.myeva.service.exception.ValidityException;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN_ORGAS')")
@RequestMapping("/private/api/refposte")
public class RefPosteApi {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefPosteApi.class);
	
	@Autowired
	RefPosteService REFPOSTE_SERVICE;
	
	@Autowired
	RefProfessionService PROFESSION_SERVICE;
	
	@Autowired
	RefRisqueReferenceService REFRISQUEREFERENCE_SERVICE;
	
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	@GetMapping("/professions")
	ResponseEntity<List<RefProfession>> readProfessions() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START readProfessions()");	
		}

		List<RefProfession> entitys=null;
		try {
			entitys=PROFESSION_SERVICE.reads();
		}
		catch (Exception exception) {
			final String message="### readProfessions():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<List<RefProfession>>(entitys,HttpStatus.INTERNAL_SERVER_ERROR);
        }
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   readProfessions(size={})",entitys.size());	
		}
		return new ResponseEntity<List<RefProfession>>(entitys,HttpStatus.OK);
	}
	
	@GetMapping("/{id}/refrisquereferences/unused")
	public ResponseEntity<List<RefRisqueReference>> readsUnusedRisqueReferences(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START readsUnusedRisqueReferences()");
		}

		List<RefRisqueReference> notUsedRisqueReferences = new ArrayList<RefRisqueReference>();			
		try {
			// Récupération des risques du poste
			RefPoste poste=REFPOSTE_SERVICE.read(id);
			Set<String> risquesSet=new HashSet<String>();
			for (RefRisque risque:poste.getRisques()) risquesSet.add(risque.getLabel());
			
			// On retourne les risques de référence qui ne sont pas dans la liste des risques du poste
			List<RefRisqueReference> risquereferences=REFRISQUEREFERENCE_SERVICE.reads();
			for (RefRisqueReference risqueReference:risquereferences) {
				if (!risquesSet.contains(risqueReference.getLabel())) notUsedRisqueReferences.add(risqueReference);
			}
		} 
		catch (Exception exception) {
			final String message = "### readsUnusedRisqueReferences():" + getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<List<RefRisqueReference>>(notUsedRisqueReferences,HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (LOGGER.isDebugEnabled()) {
			//for (RefPoste element:entities) LOGGER.debug("--- {}",element);
			LOGGER.debug("<<< END   readsUnusedRisqueReferences(size={})",notUsedRisqueReferences.size());
		}
		return new ResponseEntity<List<RefRisqueReference>>(notUsedRisqueReferences,HttpStatus.OK);
	}
	
	@GetMapping
	ResponseEntity<List<RefPoste>> reads(@RequestParam(required = false,defaultValue = "false") boolean isnew) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START reads(isnew={})",isnew);	
		}

		List<RefPoste> entitys=null;
		try {
			entitys=REFPOSTE_SERVICE.reads().stream().filter(element -> element.getIsNew()==isnew ).collect(Collectors.toList());
		}
		catch (Exception exception) {
			final String message="### reads():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<List<RefPoste>>(entitys,HttpStatus.INTERNAL_SERVER_ERROR);
        }
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   reads(size={})",entitys.size());	
		}
		return new ResponseEntity<List<RefPoste>>(entitys,HttpStatus.OK);
	}
	
	@GetMapping("/id/{id}")
	public  ResponseEntity<RefPoste> read(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START read(id={})",id);  
		}	
			
		RefPoste entity = null;
		try {
			entity=REFPOSTE_SERVICE.read(id);
		}
		catch (ObjectNotFoundException exception) {
			final String message="### read():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.NOT_FOUND);
		}
		catch (Exception exception) {
			final String message="### read():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   read({})",entity);
		}
		return new ResponseEntity<RefPoste>(entity,HttpStatus.OK);
	}
	
	@PostMapping
	public  ResponseEntity<RefPoste> create(@RequestBody RefPoste entity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START create({})",entity);  
		}	
			
		try {
			entity=REFPOSTE_SERVICE.create(entity);
		}
		catch (CompletenessException exception) {
			final String message="### create():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.PRECONDITION_FAILED);
		}
		catch (ValidityException exception) {
			final String message="### create():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.PRECONDITION_REQUIRED);
		}
		catch (UnicityException exception) {
			final String message="### create():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.CONFLICT);
		}
		catch (Exception exception) {
			final String message="### create():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   create({})",entity);
		}
		return new ResponseEntity<RefPoste>(entity,HttpStatus.OK);
	}
	
	@PostMapping("/{id}/duplication")
	public  ResponseEntity<Void> duplicate(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START duplicate(id={})",id);  
		}	
			
		try {
			REFPOSTE_SERVICE.duplicate(id);
		}
		catch (Exception exception) {
			final String message="### duplicate():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   duplicate(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@PutMapping
	public  ResponseEntity<RefPoste> update(@RequestBody RefPoste entity) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START update({})",entity);  
		}	
			
		try {
			REFPOSTE_SERVICE.update(entity);
		}
		catch (CompletenessException exception) {
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.PRECONDITION_FAILED);
		}
		catch (ValidityException exception) {
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.PRECONDITION_REQUIRED);
		}
		catch (UnicityException exception) {
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.CONFLICT);
		}
		catch (Exception exception) {
			final String message="### update():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<RefPoste>(entity,HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   update({})",entity);
		}
		return new ResponseEntity<RefPoste>(entity,HttpStatus.OK);
	} 
	
	@PutMapping("/{id}/enabling")
	public  ResponseEntity<Void> enable(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START enable(id={})",id);  
		}	
			
		try {	
			REFPOSTE_SERVICE.enable(id);
		}
		catch (CompletenessException exception) {
			final String message="### enable():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.PRECONDITION_FAILED);
		}
		catch (UnicityException exception) {
			final String message="### enable():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		}
		catch (Exception exception) {
			final String message="### enable():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   enable(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@PutMapping("/{id}/disabling")
	public  ResponseEntity<Void> disable(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START disable(id={})",id);  
		}	
			
		try {	
			REFPOSTE_SERVICE.disable(id);
		}
		catch (CompletenessException exception) {
			final String message="### disable():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.PRECONDITION_FAILED);
		}
		catch (UnicityException exception) {
			final String message="### disable():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		}
		catch (Exception exception) {
			final String message="### disable():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   disable(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@PutMapping("/{id}/integration")
	public  ResponseEntity<Void> integrate(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START integrate(id={})",id);  
		}	
			
		try {	
			REFPOSTE_SERVICE.disable(id);
		}
		catch (CompletenessException exception) {
			final String message="### integrate():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.PRECONDITION_FAILED);
		}
		catch (UnicityException exception) {
			final String message="### integrate():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		}
		catch (Exception exception) {
			final String message="### integrate():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   integrate(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	
	@PostMapping("/{id}/risques")
	public  ResponseEntity<Void> add(@PathVariable("id") long id,@RequestBody List<RefRisque> risques) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START add(id={})",id);	
			if (risques!=null) for (RefRisque risque:risques) LOGGER.debug("--- {}",risque);
		}	
			
		try {	
			REFPOSTE_SERVICE.add(id,risques);
		}
		catch (Exception exception) {
			final String message="### add():"+getStackTrace(exception);
			LOGGER.error(message);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   add(id={})",id);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@DeleteMapping("/{id}")
	public  ResponseEntity<Void> delete(@PathVariable("id") long id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START delete(id={})",id);  
		}	
			
		try {
			REFPOSTE_SERVICE.delete(id);
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
}
