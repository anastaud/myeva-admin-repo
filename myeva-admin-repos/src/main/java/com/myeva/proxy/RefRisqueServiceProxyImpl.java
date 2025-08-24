package com.myeva.proxy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

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

import com.myeva.model.RefConditionExposition;
import com.myeva.model.RefMesurePrevention;
import com.myeva.model.RefRisque;
import com.myeva.service.contract.RefRisqueService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;
import com.myeva.service.exception.ValidityException;

import jakarta.annotation.PostConstruct;

@Service
public class RefRisqueServiceProxyImpl implements RefRisqueService {
	
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
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/refrisques";
    }

	@Override
	public RefRisque head(long id) throws ServiceException {
		RefRisque entity = null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/head"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,RefRisque.class
		    	,id
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
	public RefRisque read(long id) throws ServiceException {
		RefRisque entity = null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,RefRisque.class
		    	,id
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
	public RefRisque create(RefRisque entity) throws ServiceException {		
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,RefRisque.class
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
	public void update(RefRisque entity) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Void.class
		    ).getBody();
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
	}

	@Override
	public void delete(long id) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}"
	            ,HttpMethod.DELETE
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );	
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
	}

	@Override
	public void deletes(List<Long> ids) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/many"
	            ,HttpMethod.DELETE
	            ,new HttpEntity<>(ids,SERVICE_HEADERS)
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
	
	@Override
	public void add(RefConditionExposition child) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/condition"
	            ,HttpMethod.POST
	            ,new HttpEntity<>(child,SERVICE_HEADERS)
		    	,Void.class
		    ).getBody();
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
	}

	@Override
	public void add(RefMesurePrevention child) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/mesure"
	            ,HttpMethod.POST
	            ,new HttpEntity<>(child,SERVICE_HEADERS)
		    	,Void.class
		    ).getBody();
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
	}
	
	@Override
	public void deleteRefCondition(long id) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/condition/{id}"
	            ,HttpMethod.DELETE
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );	
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
	}
	
	@Override
	public void deleteRefMesure(long id) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/mesure/{id}"
	            ,HttpMethod.DELETE
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
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
