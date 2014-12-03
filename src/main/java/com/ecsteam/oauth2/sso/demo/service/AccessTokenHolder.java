package com.ecsteam.oauth2.sso.demo.service;

import org.springframework.util.Assert;

public class AccessTokenHolder {
	private static final InheritableThreadLocal<AccessToken> local = new InheritableThreadLocal<AccessToken>();

	private AccessTokenHolder() {
	}

	public static String getToken() {
		AccessToken token = local.get();

		if (token != null) {
			return token.getToken();
		}

		return null;
	}

	public static void setToken(String token) {
		Assert.hasText(token, "token must not be null");
		local.set(new AccessToken(token));
	}

	public static void reset() {
		local.remove();
	}

	private static class AccessToken {
		private String token;

		public AccessToken(String token) {
			this.token = token;
		}

		public String getToken() {
			return this.token;
		}
	}
}
