package com.ecsteam.oauth2.sso.demo.configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.request.RequestContextListener;

public class RequestContextInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		System.out.println("Initializing RequestContextListener!");
		servletContext.addListener(RequestContextListener.class);
	}

}
