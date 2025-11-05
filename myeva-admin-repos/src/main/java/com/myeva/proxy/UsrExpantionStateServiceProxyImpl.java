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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.myeva.model.UsrExpantionState;
import com.myeva.service.contract.UsrExpantionStateService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;
import com.myeva.service.exception.ValidityException;

import jakarta.annotation.PostConstruct;

@Service
public class UsrExpantionStateServiceProxyImpl implements UsrExpantionStateService {

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
	
	private void throwServiceException(HttpClientErrorException exception) throws ServiceException{
		if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
			throw new ObjectNotFoundException(exception);
	    }else if (exception.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
			throw new CompletenessException(exception);
	    }
		else if (exception.getStatusCode() == HttpStatus.PRECONDITION_REQUIRED) {
			throw new ValidityException(exception);
	    }
		else if (exception.getStatusCode() == HttpStatus.CONFLICT) {
			throw new UnicityException(exception);
	    }
		else if (exception.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
			throw new ServiceException(exception);
	    }
	}
	
	@PostConstruct
    public void init() {
	    String auth = SERVICE_USER + ":" + SERVICE_PASSWORD;
	    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
	    SERVICE_HEADERS = new HttpHeaders();
	    SERVICE_HEADERS.set("Authorization", "Basic " + new String(encodedAuth));
	    SERVICE_HEADERS.setContentType(MediaType.APPLICATION_JSON);	
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/usrexpantionstates";
    }
	
	@Override
	public UsrExpantionState read(long usrId) throws ServiceException {
		
		UsrExpantionState entity=null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{usrId}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,UsrExpantionState.class
		    	,usrId
		    ).getBody();
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}

		return entity;
	}
	
	@Override
	public void save(UsrExpantionState entity) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Void.class
		    );
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
	}
}
