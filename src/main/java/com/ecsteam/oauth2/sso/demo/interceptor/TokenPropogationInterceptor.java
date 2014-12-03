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
			request.getHeaders().add("Authorization", authHeader);
		} 

		return execution.execute(request, body);
	}
}
