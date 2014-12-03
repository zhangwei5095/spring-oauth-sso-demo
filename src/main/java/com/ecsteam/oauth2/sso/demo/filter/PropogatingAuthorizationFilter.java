package com.ecsteam.oauth2.sso.demo.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecsteam.oauth2.sso.demo.service.AccessTokenHolder;

public class PropogatingAuthorizationFilter extends OncePerRequestFilter {

	private OAuth2RestTemplate restTemplate;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			String token = request.getHeader("Authorization");
			if (token != null) {
				AccessTokenHolder.setToken(token);
			}
			else if (restTemplate != null) {
				System.out.println("Authorization header not present, fetching access token");
				OAuth2AccessToken accessToken = restTemplate.getAccessToken();
				AccessTokenHolder.setToken(accessToken.getValue());
			}
			else {
				// TODO: hitting this, better listen to the debug statement, it means business
				System.out.println("restTemplate is null, fix your stupid code!");
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
		}
	}
	
	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
