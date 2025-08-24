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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.myeva.service.contract.HealthService;

import jakarta.annotation.PostConstruct;

@Service
public class HealthServiceProxyImpl implements HealthService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HealthServiceProxyImpl.class);
	
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
	    SERVICE_URL="http://"+SERVICE_HOST+":"+SERVICE_PORT+"/actuator/health";
    }
		
	@Override
	public boolean isUp() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START isUp()");
		}

		ResponseEntity<String> responseEntity=null;
		boolean isUp = false;
		try {
			responseEntity=REST_TEMPLATE.exchange(
				SERVICE_URL
	            ,HttpMethod.GET
	            ,new HttpEntity<>(SERVICE_HEADERS)
		    	,String.class
		    );
		}
		catch(Throwable exception) {
			final String message="### isUp():"+getStackTrace(exception);
			LOGGER.error(message);
		}
		
		if (responseEntity!=null && responseEntity.getStatusCode().is2xxSuccessful()) {			
			isUp=true;//"UP".equals(responseEntity.getBody());
		}
		
		LOGGER.info("--- myeva-service {}",(isUp ? "OPERATIONAL" : "NOT OPERATIONAL"));
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("<<<   END isUp({})",isUp);
		}
		return  isUp;
	}
}
