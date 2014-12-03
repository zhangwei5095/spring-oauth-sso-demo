package com.ecsteam.oauth2.sso.demo.interceptor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import com.ecsteam.oauth2.sso.demo.service.AccessTokenHolder;

public class TokenPropogationInterceptor implements ClientHttpRequestInterceptor {
	private static final Log log = LogFactory.getLog(TokenPropogationInterceptor.class);
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		final String method = "intercept";
		
		log.info("ENTER " + method);
		
		String authHeader = AccessTokenHolder.getToken();
		
		HttpRequestWrapper wrapper = new HttpRequestWrapper(request);
		
		if (authHeader != null) {
			log.info("Adding authorization header to request: " + authHeader);
			wrapper.getHeaders().add("Authorization", authHeader);

			String sessionCookie = String.format("%s=%s", AccessTokenHolder.getSessionIdName(),
					AccessTokenHolder.getSessionId());

			log.info("Adding session id cookie: " + sessionCookie);
			wrapper.getHeaders().add("Cookie", sessionCookie);

		}
		else {
			log.info("Token not present!");
		}

		log.info("EXIT " + method);
		
		return execution.execute(wrapper, body);
	}
}
