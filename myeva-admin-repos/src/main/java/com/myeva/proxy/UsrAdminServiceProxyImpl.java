package com.myeva.proxy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.myeva.model.UsrAdmin;
import com.myeva.service.contract.UsrAdminService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;

import jakarta.annotation.PostConstruct;

@Service
public class UsrAdminServiceProxyImpl implements UsrAdminService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UsrAdminServiceProxyImpl.class);
	
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
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/usradmins";
    }
	
	private List<UsrAdmin> readEntitys(String usrSuffix) throws ServiceException {
		
		//////// Appel du web service
		List<UsrAdmin> entitys=new ArrayList<UsrAdmin>();
		ResponseEntity<List<UsrAdmin>> responseEntity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+usrSuffix
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<UsrAdmin>>(){}
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### readEntitys():"+getStackTrace(exception);
			LOGGER.error(message);			
		}
			
		///////// Traitement des erreurs HTTP
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
		else if (responseEntity.getStatusCode()==HttpStatus.NOT_FOUND){
			throw new ObjectNotFoundException();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entitys.addAll(responseEntity.getBody());
		}
		
		return entitys;
	}
	
	@Override
	public List<UsrAdmin> readAll() throws ServiceException {
		return readEntitys("/all");
	}

	@Override
	public List<UsrAdmin> readRoots() throws ServiceException {
		return readEntitys("/roots");
	}


	@Override
	public List<UsrAdmin> readAdminRepos() throws ServiceException {
		return readEntitys("/repos");
	}
	
	@Override
	public List<UsrAdmin> readAdminOrgas() throws ServiceException {
		return readEntitys("/orgas");
	}
		
	@Override
	public UsrAdmin read(String login) throws ServiceException {
		
		//////// Appel du web service
		ResponseEntity<UsrAdmin> responseEntity=null;
		UsrAdmin entity=null;
		String message =null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/login/{login}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,UsrAdmin.class
		    	,login
		    );

		} 
		catch (HttpClientErrorException.NotFound exeption) {
			throw new ObjectNotFoundException(exeption);
		}
		catch(Exception exception) { 
			message="### read(login):"+getStackTrace(exception);
			LOGGER.error(message);	
			throw new ServiceException(message);
		}
		
		///////// Récupération du corp
		if (responseEntity!=null && responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}

		return entity;
	}
	
	@Override
	public UsrAdmin read(long id) throws ServiceException {
		ResponseEntity<UsrAdmin> responseEntity=null;
		UsrAdmin entity=null;
		String message =null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/id/{id}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,UsrAdmin.class
		    	,id
		    );
		}
		catch (HttpClientErrorException.NotFound exeption) {
			throw new ObjectNotFoundException(exeption);
		}
		catch(Exception exception) { 
			message="### read(id):"+getStackTrace(exception);
			LOGGER.error(message);	
			throw new ServiceException(message);
		}
		
		///////// Récupération du corp
		if (responseEntity!=null && responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}

		return entity;
	}
	
	@Override
	public UsrAdmin create(UsrAdmin entity) throws ServiceException {
		
		
		/////////// Appel du web service
		ResponseEntity<UsrAdmin> responseEntity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,UsrAdmin.class
		    );
		}
		catch (HttpClientErrorException exception) {
			if (exception.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {  // 412
				throw new CompletenessException(exception);
		    } 
			else if (exception.getStatusCode() == HttpStatus.CONFLICT) {  // 409
				throw new UnicityException(exception);
		    } 
		}
		catch(Exception exception) { 
			message="### read(login):"+getStackTrace(exception);
			LOGGER.error(message);	
			throw new ServiceException(message);
		}
		
		///////// Récupération du body
		if (responseEntity!=null && responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}
		
		return entity;
	}
	
	@Override
	public void updateHasLogged(long usrId, boolean hasLogged) throws ServiceException {
		ResponseEntity<Void> responseEntity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{usrId}/haslogged/{hasLogged}"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,usrId
		    	,hasLogged
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### updateHasLogged():"+getStackTrace(exception);
			LOGGER.error(message);			
		}
		
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
	}

	@Override
	public void disable(long id) throws ServiceException {	
		ResponseEntity<Void> responseEntity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/disabling"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### disable():"+getStackTrace(exception);
			LOGGER.error(message);			
		}
		
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
	}

	@Override
	public void enable(long id) throws ServiceException {
		ResponseEntity<Void> responseEntity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/enabling"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### enable():"+getStackTrace(exception);
			LOGGER.error(message);			
		}
		
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
	}
	
	@Override
	public void reset(long id) throws ServiceException {
		ResponseEntity<Void> responseEntity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/reset"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### reset():"+getStackTrace(exception);
			LOGGER.error(message);			
		}
		
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
	}

	@Override
	public void delete(long id) throws ServiceException {
		ResponseEntity<Void> responseEntity=null;
		String message = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}"
	            ,HttpMethod.DELETE
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) { // On ne lève pas d'exception ici pour gérer les erreurs HTTP plus bas
			message="### delete():"+getStackTrace(exception);
			LOGGER.error(message);			
		}
		
		if (responseEntity==null || responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(message);
		}
	}
}
