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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.myeva.model.Usr;
import com.myeva.model.UsrProduct;
import com.myeva.service.contract.UsrCustomerService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;
import com.myeva.service.exception.ValidityException;

import jakarta.annotation.PostConstruct;

@Service
public class UsrCustomerServiceProxyImpl implements UsrCustomerService {

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
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/customers/multi";
    }
	
	@Override
	public List<Usr> reads() throws ServiceException {
		List<Usr> entitys=new ArrayList<Usr>();
		try {
			entitys.addAll(REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<Usr>>(){}
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
	public List<Usr> readsPortfolio(long id) throws ServiceException {
		List<Usr> entitys=new ArrayList<Usr>();
		try {
			entitys.addAll(REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/portfolio"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<Usr>>(){}
				,id
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
	public boolean contains(long ownerId, long ownedId) throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean exists(String login) throws ServiceException {
		Boolean entity=null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/exists/{login}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Boolean.class
		    	,login
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
	public Usr read(String login) throws ServiceException {
		Usr entity=null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/login/{login}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Usr.class
		    	,login
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
	public Usr read(long id) throws ServiceException {
		
		Usr entity=null;
		try {
			entity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/id/{id}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Usr.class
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
	
	/*@Override
	public Usr subscribe(Usr entity) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/subscription"
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Usr.class
		    );
		}
		catch(HttpClientErrorException exception) {
			throwServiceException(exception);
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}

		return entity;
	}*/
	
	@Override
	public Usr create(Usr entity) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Usr.class
		    );
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
	public void updateDcp(Usr entity) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/dcp"
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

	@Override
	public void updateHasLogged(long usrId, boolean hasLogged) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{usrId}/haslogged/{hasLogged}"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,usrId
		    	,hasLogged
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
	public void updateProductExpiration(long ownerId, int productExtentionDays) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{ownerId}/productexpiration/{productExtentionDays}"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,ownerId
		    	,productExtentionDays
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
	public void updatePortfolioUnitPrice(long ownerId,UsrProduct product)  throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{ownerId}/unitprice"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(product,SERVICE_HEADERS)
		    	,Void.class
		    	,ownerId
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}

		if (responseEntity.getStatusCode()==HttpStatus.PRECONDITION_FAILED){
			throw new ValidityException();
		}	
		else if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
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
	public void reset(long id) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/reset"
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
	public void publish(long id) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/publish"
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
	public void unpublish(long id) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/unpublish"
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
	public void assign(long ownedId,long ownerId) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{ownedId}/assignement/{ownerId}"
	            ,HttpMethod.POST
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,ownedId
		    	,ownerId
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
