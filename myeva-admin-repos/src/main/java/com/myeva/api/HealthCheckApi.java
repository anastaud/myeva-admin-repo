package com.myeva.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myeva.service.contract.HealthService;

@RestController
@RequestMapping("/public/api/healthcheck")
public class HealthCheckApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckApi.class);
	   
	@Autowired
	    
	HealthService HEALTH_SERVICE;
	
	@GetMapping
	ResponseEntity<Void> healthCheck() {
		boolean serviceIsUp = false;
		try {
			serviceIsUp = HEALTH_SERVICE.isUp();
		}
		catch (Exception exception) {
			serviceIsUp = false;
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
		if (LOGGER.isDebugEnabled()) LOGGER.debug(">>> <<< serviceIsUp={}",serviceIsUp);	
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
