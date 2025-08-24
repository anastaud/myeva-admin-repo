package com.myeva.api;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN_ORGAS')")
@RequestMapping("/private/api/refnode")
public class RefNodeApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefNodeApi.class);
	
	/*private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}*/
	
	private final String EXPANDED_NODES = "expandedNodes";
	
	@GetMapping("/expandeds")
	public  ResponseEntity<Set<String>> getExpandeds(HttpSession session) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START getExpandeds()");  
		}	
			
		@SuppressWarnings("unchecked")
		Set<String> expandeds=(Set<String>)session.getAttribute(EXPANDED_NODES);
		if (expandeds ==null)  {
			expandeds = new HashSet<String>();
			session.setAttribute(EXPANDED_NODES,expandeds);
		}
		
		if (LOGGER.isDebugEnabled()) {
			for (String element:expandeds) LOGGER.debug("--- {}",element);
			LOGGER.debug("<<< END   getExpandeds(size={})",expandeds.size());
		}
		return new ResponseEntity<Set<String>>(expandeds,HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/{id}/expantion")
	public  ResponseEntity<Boolean> getNodeIsExpanded(@PathVariable("key") String key,HttpSession session) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START getNodeIsExpanded(key={})",key);  
		}	
			
		boolean isExpanded = false;
		Set<String> expandeds=(Set<String>)session.getAttribute(EXPANDED_NODES);
		if (expandeds ==null)  session.setAttribute(EXPANDED_NODES, new HashSet<String>());
		else isExpanded=expandeds.contains(key);
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   getNodeIsExpanded(key={})",key);
		}
		return new ResponseEntity<Boolean>(isExpanded,HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@PutMapping("/{key}/expantion")
	public  ResponseEntity<Void> setNodeExpanded(@PathVariable("key") String key,HttpSession session) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START setNodeExpanded(key={})",key);  
		}	
			
		Set<String> expandeds=(Set<String>)session.getAttribute(EXPANDED_NODES);
		if (expandeds ==null)  session.setAttribute(EXPANDED_NODES, new HashSet<String>());
		else expandeds.add(key);
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   setNodeExpanded(key={})",key);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@PutMapping("/{key}/collapsing")
	public  ResponseEntity<Void> setNodeCollapsed(@PathVariable("key") String key,HttpSession session) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START setNodeCollapsed(key={})",key);  
		}	
			
		Set<String> expandeds=(Set<String>)session.getAttribute(EXPANDED_NODES);
		if (expandeds ==null)  session.setAttribute(EXPANDED_NODES, new HashSet<String>());
		else expandeds.remove(key);
				
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<< END   setNodeCollapsed(key={})",key);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
