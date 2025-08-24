package com.myeva;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;

import com.myeva.handler.CustomAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class AdminConfig {
		
	@Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/img/**", "/js/**").permitAll()
            .requestMatchers("/public/**").permitAll()
            .requestMatchers("/private/**").hasRole("ADMIN_REPOS")
            .requestMatchers("/private/api/**").hasRole("ADMIN_REPOS")
            .anyRequest().authenticated()
        )
        .formLogin(login -> login
            .loginPage("/public/authentication/login")
            .loginProcessingUrl("/login")
            .successHandler(customSuccessHandler())
            //.defaultSuccessUrl("/private/home", true)
            .failureUrl("/public/authentication/login?error")
            .permitAll()
        )
        .logout(logout -> logout
        	.logoutUrl("/private/authentication/logout")
            .logoutSuccessUrl("/public/authentication/login")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .clearAuthentication(true)
            .permitAll()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/private/api/**") // Désactivation du crf pour les API
        );
        //.csrf(csrf -> csrf.disable()); 

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean // ATTENTION: pas besoin de définir le UserDetailService, Spring Boot détectera automatiquement ton UserDetailsService et l'utilisera pour l'authentification
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
  
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
}
