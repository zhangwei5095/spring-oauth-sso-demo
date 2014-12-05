package com.ecsteam.oauth2.sso.demo.interceptor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * This interceptor retrieves the OAuth2 access token from the current security context or, if it is not available from
 * the current context, from the OAuth2 Authorization server specified in the application config.
 * 
 * @author Josh Ghiloni
 *
 */
public class TokenPropagationInterceptor extends SkippableInterceptor {
	private static final Log log = LogFactory.getLog(TokenPropagationInterceptor.class);

	private OAuth2RestTemplate restTemplate;

	public TokenPropagationInterceptor(OAuth2RestTemplate template) {
		this.restTemplate = template;
	}

	@Override
	protected ClientHttpResponse interceptInternal(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		final String method = "intercept";

		log.debug("ENTER " + method);

		// You cannot modify the headers of an existing HttpRequest object, so wrap it. 
		HttpRequestWrapper wrapper = new HttpRequestWrapper(request);

		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication auth = context.getAuthentication();
			if (auth != null) {
				Object details = auth.getDetails();
				String authHeader = null;
				// try to get the token off the authentication object
				if (details instanceof OAuth2AuthenticationDetails) {
					OAuth2AuthenticationDetails oauthDetails = OAuth2AuthenticationDetails.class.cast(details);
					authHeader = oauthDetails.getTokenValue();
				}

				// if it's not there, get it from the auth server
				if (authHeader == null) {
					log.debug("Token not present, fetch");
					try {
						OAuth2AccessToken token = restTemplate.getAccessToken();
						authHeader = token.getValue();
					}
					catch (UserRedirectRequiredException e) {
						// this means they're not correctly authenticated
						log.error("Token not retrievable", e);
						authHeader = null;
					}
				}

				// if we have a bearer token, add it to the outgoing request
				if (authHeader != null) {
					log.debug("Adding authorization header to request: " + authHeader);
					wrapper.getHeaders().add("Authorization", "Bearer " + authHeader);
				}
				else {
					log.debug(String.format("Token was not retrievable from details class: %s", details));
				}
			}
			else {
				log.debug("Authentication was null");
			}
		}
		else {
			log.debug("No security context present");
		}

		log.debug("EXIT " + method);

		return execution.execute(wrapper, body);
	}
}
