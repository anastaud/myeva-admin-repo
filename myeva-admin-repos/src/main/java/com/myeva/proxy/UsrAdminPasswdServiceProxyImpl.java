package com.myeva.proxy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.myeva.model.UsrAdminPasswd;
import com.myeva.service.contract.UsrAdminPasswdService;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;

import jakarta.annotation.PostConstruct;

@Service
public class UsrAdminPasswdServiceProxyImpl implements UsrAdminPasswdService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UsrAdminPasswdServiceProxyImpl.class);

	@Value("${custom.service.host}")
	private String SERVICE_HOST;
	@Value("${custom.service.port}")
	private String SERVICE_PORT;
	@Value("${custom.service.user}")
	private String SERVICE_USER;
	@Value("${custom.service.password}")
	private String SERVICE_PASSWORD;

	@Autowired
	private RestTemplate REST_TEMPLATE; 	
	
	private HttpHeaders SERVICE_HEADERS;
	private String SERVICE_URL;
	
	private String getStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	@PostConstruct
    public void init() {
	    String auth = SERVICE_USER + ":" + SERVICE_PASSWORD;
	    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
	    SERVICE_HEADERS = new HttpHeaders();
	    SERVICE_HEADERS.set("Authorization", "Basic " + new String(encodedAuth));
	    SERVICE_HEADERS.setContentType(MediaType.APPLICATION_JSON);	
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/usradmins/passwd";
    }
	
	@Override
	public UsrAdminPasswd read(long id) throws ServiceException {
		ResponseEntity<UsrAdminPasswd> responseEntity=null;
		UsrAdminPasswd entity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,UsrAdminPasswd.class
		    	,id
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### read(id):"+getStackTrace(exception);
			LOGGER.error(message);			
		}
		
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
		else if (responseEntity.getStatusCode()==HttpStatus.NOT_FOUND){
			throw new ObjectNotFoundException();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}

		return entity;
	}
	
	@Override
	public void update(UsrAdminPasswd entity) throws ServiceException {
		ResponseEntity<Void> responseEntity = null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Void.class
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### update():"+getStackTrace(exception);
			LOGGER.error(message);			
		}
		
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
	}
}
