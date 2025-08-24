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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.myeva.model.RefPoste;
import com.myeva.model.RefRisque;
import com.myeva.service.contract.RefPosteService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;
import com.myeva.service.exception.ValidityException;

import jakarta.annotation.PostConstruct;

@Service
public class RefPosteServiceProxyImpl implements RefPosteService {
	
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
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/refpostes";
    }

	@Override
	public int count() throws ServiceException {
		int count=0;
		try {
			count=REST_TEMPLATE.exchange(
				SERVICE_URL+"/count"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<Integer>(){}
		    ).getBody();
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}		
		return count;
	}

	@Override
	public List<RefPoste> heads() throws ServiceException {
		List<RefPoste> entitys=new ArrayList<RefPoste>();
		try {
			entitys.addAll(REST_TEMPLATE.exchange(
				SERVICE_URL+"/head"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<RefPoste>>(){}
		    ).getBody());
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		return entitys;
	}
	
	@Override
	public List<RefPoste> reads() throws ServiceException {
		List<RefPoste> entitys=new ArrayList<RefPoste>();
		try {
			entitys.addAll(REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<RefPoste>>(){}
		    ).getBody());
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		return entitys;
	}

	@Override
	public RefPoste read(long id) throws ServiceException {
		RefPoste entity = null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,RefPoste.class
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
	public RefPoste head(long id) throws ServiceException {
		RefPoste entity = null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/head"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,RefPoste.class
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
	public RefPoste create(RefPoste entity) throws ServiceException {		
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,RefPoste.class
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
	public RefPoste duplicate(long id) throws ServiceException {	
		RefPoste entity = null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/duplication/{id}"
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,RefPoste.class
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
	public void update(RefPoste entity) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,RefPoste.class
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
	public void enable(long id) throws ServiceException {		
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/enabling"
	            ,HttpMethod.PUT
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
	public void disable(long id) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/disabling"
	            ,HttpMethod.PUT
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
	public void add(long id, List<RefRisque> risques) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/risques"
	            ,HttpMethod.POST
	            ,new HttpEntity<>(risques,SERVICE_HEADERS)
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
}
