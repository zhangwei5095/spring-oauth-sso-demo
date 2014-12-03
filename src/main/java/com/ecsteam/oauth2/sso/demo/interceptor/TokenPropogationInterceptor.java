package com.ecsteam.oauth2.sso.demo.interceptor;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.ecsteam.oauth2.sso.demo.service.AccessTokenHolder;

public class TokenPropogationInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		String authHeader = AccessTokenHolder.getToken();
		if (authHeader != null) {
			System.out.println("Adding authorization header to request: " + authHeader);
			request.getHeaders().add("Authorization", authHeader);
			
			System.out.println("Adding session id cookie");
			request.getHeaders().add("Cookie", String.format("%s=%s", AccessTokenHolder.getSessionIdName(), AccessTokenHolder.getSessionId()));
					
			
			
		} else {
			System.out.println("Token not present!");
		}

		return execution.execute(request, body);
	}
}
