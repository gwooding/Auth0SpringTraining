package com.auth0.example;

import com.auth0.spring.security.api.Auth0AuthenticationFilter;
import com.auth0.spring.security.api.Auth0CORSFilter;
import com.auth0.example.MyFilter;
import com.auth0.spring.security.api.Auth0SecurityConfig;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class AppConfig extends Auth0SecurityConfig {

	/**
	 * Provides Auth0 API access
	 */
	@Bean
	public Auth0Client auth0Client() {
		return new Auth0Client(clientId, issuer);
	}
	
	//Attempt 1 to register MyFilter
	@Bean
	public FilterRegistrationBean myFilterRegistration() {

	    FilterRegistrationBean registration = new FilterRegistrationBean();
	    registration.setFilter(myFilter());
	    registration.addUrlPatterns("/**");
	    registration.setName("myFilter");
	    registration.setOrder(1);
	    return registration;
	} 

	@Bean(name = "myFilter")
	public Filter myFilter() {
	    return new MyFilter();
	}

//  Attempt 2, using corsFilter.
//	@Bean
//	public Filter corsFilter() {
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.addAllowedOrigin("*");
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("OPTIONS");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");
//        
//        System.out.println(config);
//        
//        source.registerCorsConfiguration("/**", config);
//		
//		return new CorsFilter(source);
//	}

//  Alternative solution suggested on stack overflow
//	@Bean
//    public FilterRegistrationBean corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.addAllowedOrigin("*");
//
//        config.addAllowedHeader("*");       
//        config.addAllowedMethod("*");        
//        source.registerCorsConfiguration("/**", config);

//        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
//        bean.setOrder(0);
//        return bean;
//
//    }
	
//  4th Attempt at corsFilter method.
//	@Bean
//	public FilterRegistrationBean corsFilter() {
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//	    config.addAllowedOrigin("*");
//	    config.addAllowedHeader("Content-Type");
//	    config.addAllowedHeader("x-xsrf-token");
//	    config.addAllowedHeader("Authorization");
//	    config.addAllowedHeader("Access-Control-Allow-Headers");
//	    config.addAllowedHeader("Origin");
//	    config.addAllowedHeader("Accept");
//	    config.addAllowedHeader("X-Requested-With");
//	    config.addAllowedHeader("Access-Control-Request-Method");
//	    config.addAllowedHeader("Access-Control-Request-Headers");
//	    config.addAllowedMethod("OPTIONS");
//	    config.addAllowedMethod("GET");
//	    config.addAllowedMethod("PUT");
//	    config.addAllowedMethod("POST");
//	    config.addAllowedMethod("DELETE");
//		source.registerCorsConfiguration("/**", config);
//		@SuppressWarnings("deprecation")
//		FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
//		bean.setOrder(0);
//		return bean;
//	}

	/**
	 * Our API Configuration - for Profile CRUD operations
	 *
	 * Here we choose not to bother using the `auth0.securedRoute` property
	 * configuration and instead ensure any unlisted endpoint in our config is
	 * secured by default
	 */
	@Override
	protected void authorizeRequests(final HttpSecurity http) throws Exception {
		// include some Spring Boot Actuator endpoints to check metrics
		// add others or remove as you choose, this is just a sample config to
		// illustrate
		// most specific rules must come - order is important (see Spring
		// Security docs)

		http.authorizeRequests().antMatchers("/ping").permitAll().antMatchers("/").permitAll().anyRequest()
				.authenticated();

	}

	/*
	 * Only required for sample purposes..
	 */
	String getAuthorityStrategy() {
		return super.authorityStrategy;
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		// Disable CSRF for JWT usage
		http.csrf().disable();

		// Add Auth0 Authentication Filter
		http.addFilterAfter(auth0AuthenticationFilter(auth0AuthenticationEntryPoint()),
				SecurityContextPersistenceFilter.class)
		        //.addFilterAfter(myFilter(), MyFilter.class); 
		.addFilterAfter(simpleCORSFilter(), Auth0AuthenticationFilter.class);
		// Apply the Authentication and Authorization Strategies your
		// application endpoints require
		authorizeRequests(http);
		// STATELESS - we want re-authentication of JWT token on every request
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//    Attempt 6:
		
//		http.headers().addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Headers", "x-xsrf-token"));
//    Attempt 7:
//		http.addFilter(new MyFilter()); 
		
		
//      Attempt 8:
//		http.cors();
		
//      Attempt 9
//		http.headers().addHeaderWriter(new StaticHeadersWriter("x-xsrf-token",
//				"Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With"));
//
//
//      Attempt 10:
//		System.out.println(http.headers());
	}
}
