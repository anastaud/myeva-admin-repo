package com.myeva.proxy;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
import org.springframework.web.client.RestTemplate;

import com.myeva.model.UsrAdminLog;
import com.myeva.service.contract.UsrAdminLogService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;

import jakarta.annotation.PostConstruct;

@Service
public class UsrAdminLogServiceProxyImpl implements UsrAdminLogService {
	
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
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/usradminlogs";
    }

	@Override
	public List<UsrAdminLog> reads(long usrId) throws ServiceException {
		List<UsrAdminLog> entitys=new ArrayList<UsrAdminLog>();
		ResponseEntity<List<UsrAdminLog>> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{usrId}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<UsrAdminLog>>(){}
				,usrId
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entitys.addAll(responseEntity.getBody());
		}
		
		return entitys;
	}

	/*@Override
	public Date readLastConnexionDate(long usrId) throws ServiceException {
		ResponseEntity<Date> responseEntity;
		Date entity = null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/lastconnexiondate/{usrId}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Date.class
		    	,usrId
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
	}*/
	
	@Override
	public void create(UsrAdminLog entity) throws ServiceException {
		ResponseEntity<UsrAdminLog> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,UsrAdminLog.class
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.PRECONDITION_FAILED){
			throw new CompletenessException();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.CONFLICT){
			throw new UnicityException();
		}

	}
}
