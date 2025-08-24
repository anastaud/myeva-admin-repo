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
import com.myeva.service.contract.UsrService;
import com.myeva.service.exception.CompletenessException;
import com.myeva.service.exception.ObjectNotFoundException;
import com.myeva.service.exception.ServiceException;
import com.myeva.service.exception.UnicityException;
import com.myeva.service.exception.ValidityException;

import jakarta.annotation.PostConstruct;

@Service
public class UsrServiceProxyImpl implements UsrService {

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
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/api/usrs";
    }
	
	private List<Usr> readEntitys(String usrSuffix) throws ServiceException {
		List<Usr> entitys=new ArrayList<Usr>();
		ResponseEntity<List<Usr>> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+usrSuffix
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<Usr>>(){}
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entitys.addAll(responseEntity.getBody());
		}
		else if (responseEntity.getStatusCode()==HttpStatus.NOT_FOUND){
			throw new ObjectNotFoundException();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}
		return entitys;
	}
	
	@Override
	public List<Usr> readRoots() throws ServiceException {
		return readEntitys("/roots");
	}

	@Override
	public List<Usr> readAdmins() throws ServiceException {
		return readEntitys("/admins");
	}

	@Override
	public List<Usr> readAdminRepositorys() throws ServiceException {
		return readEntitys("/admins/repo");
	}
	
	@Override
	public List<Usr> readAdminOrgas() throws ServiceException {
		return readEntitys("/admins/orga");
	}

	@Override
	public List<Usr> readMonoCustomers() throws ServiceException {
		return readEntitys("/orgamonos");
	}

	@Override
	public List<Usr> readMultiCustomers() throws ServiceException {
		return readEntitys("/orgamultis");
	}
		
	@Override
	public List<Usr> readsPortfolio(long id) throws ServiceException {
		List<Usr> entitys=new ArrayList<Usr>();
		ResponseEntity<List<Usr>> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/portfolio"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,new ParameterizedTypeReference<List<Usr>>(){}
				,id
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}	
		
		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entitys.addAll(responseEntity.getBody());
		}
		else if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}

		return entitys;
	}
	
	@Override
	public Usr read(String login) throws ServiceException {
		ResponseEntity<Usr> responseEntity;
		Usr entity=null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/login/{login}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Usr.class
		    	,login
		    );
		} 
		catch (HttpClientErrorException.NotFound ex) {
			throw new ObjectNotFoundException(); 
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}
		/*else if (responseEntity.getStatusCode()==HttpStatus.NOT_FOUND){
			throw new ObjectNotFoundException();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}*/

		return entity;
	}
	
	@Override
	public Usr read(long id) throws ServiceException {
		ResponseEntity<Usr> responseEntity;
		Usr entity=null;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/id/{id}"
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Usr.class
		    	,id
		    );
		}
		catch (HttpClientErrorException.NotFound ex) {
			throw new ObjectNotFoundException(); 
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.OK) {
			entity=responseEntity.getBody();
		}
		/*else if (responseEntity.getStatusCode()==HttpStatus.NOT_FOUND){
			throw new ObjectNotFoundException();
		}
		else if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}*/

		return entity;
	}
	
	@Override
	public Usr create(Usr entity) throws ServiceException {
		
		ResponseEntity<Usr> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Usr.class
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
		else if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}

		return entity;
	}

	@Override
	public Usr subscribe(Usr entity) throws ServiceException {
		
		ResponseEntity<Usr> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/subscriptions"
	            ,HttpMethod.POST
	            ,new HttpEntity<>(entity,SERVICE_HEADERS)
		    	,Usr.class
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
		else if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}

		return entity;
	}
	
	@Override
	public void updateHasLogged(long usrId, boolean hasLogged) throws ServiceException {
		ResponseEntity<Void> responseEntity;
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
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		 if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}	
	}
		
	@Override
	public void updateProduct(long usrId, long productId) throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{usrId}/product/{productId}"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,usrId
		    	,productId
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
	public void updateProduct(String login, long productId) throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/login/{login}/product/{productId}"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,login
		    	,productId
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
	public void updateProductExpiration(long usrId, int productExtentionDays) throws ServiceException {
		try {
			REST_TEMPLATE.exchange(
				SERVICE_URL+"/{usrId}/productexpiration/{productExtentionDays}"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,usrId
		    	,productExtentionDays
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
	}
	
	@Override
	public void updatePortfolioUnitPrice(long ownerId,int unitPrice)  throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{ownerId}/portfoliounitprice/{unitPrice}"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,ownerId
		    	,unitPrice
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
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/disabling"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}
	}

	@Override
	public void enable(long id) throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/enabling"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}
	}
	
	@Override
	public void reset(long id) throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}/reset"
	            ,HttpMethod.PUT
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}
	}

	@Override
	public void delete(long id) throws ServiceException {
		ResponseEntity<Void> responseEntity;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL+"/{id}"
	            ,HttpMethod.DELETE
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,Void.class
		    	,id
		    );
		}
		catch(Exception exception) {
			throw new ServiceException(exception);
		}
		
		if (responseEntity.getStatusCode()==HttpStatus.INTERNAL_SERVER_ERROR){
			throw new ServiceException();
		}
	}
}
