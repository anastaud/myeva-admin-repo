package com.myeva.proxy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

import com.myeva.model.UsrAdminPrivate;
import com.myeva.service.contract.UsrAdminPrivateService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;

import jakarta.annotation.PostConstruct;

@Service
public class UsrAdminPrivateServiceProxyImpl implements UsrAdminPrivateService {
	
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

	@PostConstruct
    public void init() {
	    String auth = SERVICE_USER + ":" + SERVICE_PASSWORD;
	    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
	    SERVICE_HEADERS = new HttpHeaders();
	    SERVICE_HEADERS.set("Authorization", "Basic " + new String(encodedAuth));
	    SERVICE_HEADERS.setContentType(MediaType.APPLICATION_JSON);	
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/usradmins/private";
    }

	@Override
	public UsrAdminPrivate readByLogin(String login) throws ServiceException {
		ResponseEntity<UsrAdminPrivate> responseEntity;
		UsrAdminPrivate entity=null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/login/{login}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,UsrAdminPrivate.class
		    	,login
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}

		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.NOT_FOUND){
			throw new ObjectNotFoundException();
		}
		return entity;
	}

	@Override
	public UsrAdminPrivate readById(long id) throws ServiceException {
		ResponseEntity<UsrAdminPrivate> responseEntity;
		UsrAdminPrivate entity=null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/id/{id}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,UsrAdminPrivate.class
		    	,id
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}

		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.NOT_FOUND){
			throw new ObjectNotFoundException();
		}
		return entity;
	}

	@Override
	public void update(UsrAdminPrivate entity) throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Void.class
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		if (responseEntity.getStatusCode()!=HttpStatus.OK) {
			if (responseEntity.getStatusCode()==HttpStatus.PRECONDITION_FAILED){
				throw new CompletenessException();
			}
			else if (responseEntity.getStatusCode()==HttpStatus.CONFLICT){
				throw new UnicityException();
			}	
			else {
				throw new ServiceException(responseEntity.getStatusCode().toString());
			}
		}
	}
}
