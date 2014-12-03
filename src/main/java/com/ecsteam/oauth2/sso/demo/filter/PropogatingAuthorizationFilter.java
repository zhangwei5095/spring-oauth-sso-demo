package com.ecsteam.oauth2.sso.demo.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecsteam.oauth2.sso.demo.service.AccessTokenHolder;

public class PropogatingAuthorizationFilter extends OncePerRequestFilter {

	private OAuth2RestTemplate restTemplate;
	
	private static final Log log = LogFactory.getLog(PropogatingAuthorizationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		final String method = "doFilterInternal";
		
		log.info("ENTER " + method);
		
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
			} else {
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
			
			chain.doFilter(request, response);
		}
		catch (ServletException e) {
			throw e;
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
		finally {
			AccessTokenHolder.reset();
			log.info("EXIT " + method);
		}
	}

	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
