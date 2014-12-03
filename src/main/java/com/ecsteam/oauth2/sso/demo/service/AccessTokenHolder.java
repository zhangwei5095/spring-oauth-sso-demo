package com.ecsteam.oauth2.sso.demo.service;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class AccessTokenHolder {
	private static final InheritableThreadLocal<AccessToken> local = new InheritableThreadLocal<AccessToken>();
	
	protected static final String JSESSIONID = "JSESSIONID";

	private AccessTokenHolder() {
	}

	public static String getToken() {
		AccessToken token = local.get();

		if (token != null) {
			return token.getToken();
		}

		return null;
	}
	
	public static String getSessionId() {
		AccessToken token = local.get();

		if (token != null) {
			return token.getSessionId();
		}
		
		return null;
	}
	
	public static String getSessionIdName() {
		AccessToken token = local.get();

		if (token != null) {
			return token.getSessionIdName();
		}
		
		return JSESSIONID;
	}

	public static void setToken(String token, String sessionId) {
		Assert.hasText(token, "token must not be null");
		Assert.hasText(sessionId, "session id must not be null");
		AccessToken accessToken = new AccessToken();
		accessToken.setToken(token);
		accessToken.setSessionId(sessionId);
	}
	
	public static void setSessionIdName(String sessionIdName) {
		AccessToken token = local.get();
		if (token == null) {
			throw new IllegalStateException("Set up the access token before setting the session id name");
		}
		
		token.setSessionIdName(sessionIdName);
	}

	public static void reset() {
		local.remove();
	}

	private static class AccessToken {

		private String token;

		private String sessionIdName = JSESSIONID;

		private String sessionId;

		public AccessToken() {
		}

		public String getSessionIdName() {
			return sessionIdName;
		}

		public void setSessionIdName(String sessionIdName) {
			if (StringUtils.hasText(sessionIdName)) {
				this.sessionIdName = sessionIdName;
			}
		}

		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getToken() {
			return this.token;
		}
	}
}
