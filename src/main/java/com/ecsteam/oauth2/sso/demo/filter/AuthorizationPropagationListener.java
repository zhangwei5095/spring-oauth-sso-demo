package com.ecsteam.oauth2.sso.demo.filter;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.ecsteam.oauth2.sso.demo.service.AccessTokenHolder;

public class AuthorizationPropagationListener implements ServletRequestListener {

	private OAuth2RestTemplate restTemplate;

	private static final Log log = LogFactory.getLog(AuthorizationPropagationListener.class);

	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		// TODO Auto-generated method stub
		AccessTokenHolder.reset();
	}

	@Override
	public void requestInitialized(ServletRequestEvent requestEvent) {

		final String method = "requestInitialized";
		log.info("ENTER " + method);

		if (!(requestEvent.getServletRequest() instanceof HttpServletRequest)) {
			throw new IllegalArgumentException("Request is not an HttpServletRequest: "
					+ requestEvent.getServletRequest());
		}

		HttpServletRequest request = HttpServletRequest.class.cast(requestEvent.getServletRequest());
		try {
			String token = request.getHeader("Authorization");

			HttpSession session = request.getSession(false);
			String sessionId = null;
			if (session != null) {
				sessionId = session.getId();
			}

			log.info("Session ID: " + sessionId);
			log.info("Cookies Header: " + request.getHeader("Cookie"));

			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					log.info(String.format("Cookie %s has value %s\n", cookie.getName(), cookie.getValue()));
				}
			}
			else {
				log.info("null cookies");
			}

			if (token != null && sessionId != null) {
				log.info("Access token on request, saving " + token);

				AccessTokenHolder.setToken(token, sessionId);
			}
			else if (restTemplate != null && sessionId != null) {
				log.info("Authorization header not present, fetching access token");
				OAuth2AccessToken accessToken = restTemplate.getAccessToken();

				if (accessToken != null) {
					AccessTokenHolder.setToken("Bearer " + accessToken.getValue(), sessionId);
				}
			}
		}
		catch (Throwable e) {
			log.error("Error setting local attributes", e);

			if (e instanceof Error || e instanceof RuntimeException) {
				throw e;
			}

			throw new RuntimeException(e);
		}
		finally {
			log.info("EXIT " + method);
		}
	}
}
