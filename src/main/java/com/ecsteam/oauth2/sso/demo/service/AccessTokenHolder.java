package com.ecsteam.oauth2.sso.demo.service;

public class AccessTokenHolder {
	private static final InheritableThreadLocal<AccessToken> local = new InheritableThreadLocal<AccessToken>();
	
	private AccessTokenHolder() {}
	
	public static String get() {
		AccessToken token = local.get();
		
		if (token != null) {
			return token.getTokenValue();
		}
		
		return null;
	}
	
	public static void set(String token) {
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
		
		public String getTokenValue() {
			return this.token;
		}
	}
}
