package com.myeva.service;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.myeva.model.Role;
import com.myeva.model.UsrAdmin;
import com.myeva.model.UsrAdminPasswd;
import com.myeva.service.contract.UsrAdminPasswdService;
import com.myeva.service.contract.UsrAdminService;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailServiceImpl.class);
	
	public UserDetailServiceImpl(){
		LOGGER.info("UserDetailServiceImpl instanciated");
	}
	
	@Autowired
	UsrAdminService USRADMIN_SERVICE;
	
	@Autowired
	UsrAdminPasswdService USRADMINPASSWD_SERVICE;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(">>> START loadUserByUsername(username="+username+")");	
		}
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		User user=null;
		UsrAdmin usrAdmin = null;
		try {
			
			usrAdmin=USRADMIN_SERVICE.read(username);
			UsrAdminPasswd passwd=USRADMINPASSWD_SERVICE.read(usrAdmin.getId());
			usrAdmin.setPasswd(passwd);
			for (Role element:usrAdmin.getRoles()){
				authorities.add(new SimpleGrantedAuthority(element.getRole()));
			}	
			
			user=new User( username
					,usrAdmin.getPasswd().getValue()
					,usrAdmin.getIsEnabled()
					,!usrAdmin.getIsExpired()
					,!usrAdmin.getIsExpired()
					,usrAdmin.getIsEnabled()
					,authorities);
		} 
		catch (Exception exception) {			
			final String message="### loadUserByUsername():"+exception.getLocalizedMessage();
			LOGGER.error(message);
			throw new UsernameNotFoundException(message);
		} 		
		if (LOGGER.isDebugEnabled()) {
			StringBuffer buffer = new StringBuffer();
			for (Role element:usrAdmin.getRoles()) buffer.append(" ").append(element.getRole());
			LOGGER.debug("--- Found user UsrAdmin[id={}] with roles {}",usrAdmin.getId(),buffer);			
			LOGGER.debug("<<< END   loadUserByUsername()");	
		}
		return user;
	}
}
