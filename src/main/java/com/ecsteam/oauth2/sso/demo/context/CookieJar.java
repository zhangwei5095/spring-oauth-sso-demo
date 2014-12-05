package com.ecsteam.oauth2.sso.demo.context;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.springframework.util.StringUtils;

/**
 * A context class with thread-local information about the current request's cookies. Used to abstract the HTTP layer
 * out of Spring requests so this could theoretically be used in non-servlet contexts.
 * 
 * @author Josh Ghiloni
 *
 */
public class CookieJar {
	private static String sessionCookieName = "JSESSIONID";

	private static final InheritableThreadLocal<CookieHolder> context = new InheritableThreadLocal<CookieHolder>() {
		@Override
		protected CookieHolder initialValue() {
			// TODO Auto-generated method stub
			return new CookieHolder();
		}
	};

	/**
	 * Ensure that the threadlocal is always populated
	 * @return
	 */
	private static CookieHolder safeGet() {
		CookieHolder holder = context.get();
		if (holder == null) {
			holder = new CookieHolder();
			context.set(holder);
		}

		return holder;
	}

	/**
	 * Return the list of available cookies for this thread
	 * @return
	 */
	public static Collection<String> getCookieNames() {
		return safeGet().safeGetCookies().keySet();
	}

	/**
	 * Clear all cookies for this thread
	 */
	public static void resetCookies() {
		safeGet().safeGetCookies().clear();
	}

	/**
	 * Store a cookie for later use in this thread
	 * @param cookie
	 */
	public static void addCookie(Cookie cookie) {
		safeGet().safeGetCookies().put(cookie.getName(), cookie.getValue());
	}

	/**
	 * Find a cookie value based on its name
	 * @param cookieName
	 * @return
	 */
	public static String findCookieByName(String cookieName) {
		return safeGet().safeGetCookies().get(cookieName);
	}

	/**
	 * Store the name of the cookie used by this app to denote the session ID. By default this is JSESSIONID, but it is
	 * configurable on a per-application basis.
	 * 
	 * @param cookieName
	 */
	public static void setSessionCookieName(String cookieName) {
		if (StringUtils.hasText(cookieName)) {
			sessionCookieName = cookieName;
		}
	}

	/**
	 * Store the session id directly. This will likely come from an HttpSession object, and will overwrite any actual cookie
	 * with that name. 
	 * 
	 * @param sessionId
	 */
	public static void setSessionId(String sessionId) {
		safeGet().safeGetCookies().put(sessionCookieName, sessionId);
	}

	/**
	 * Use this class because ThreadLocals only allow one instance per type per thread. Using this custom class avoids conflicts.
	 * 
	 * @author Josh Ghiloni
	 *
	 */
	protected static class CookieHolder {
		private Map<String, String> cookies = null;

		/** 
		 * Get the cookie map, and ensure that it is never null
		 * @return
		 */
		public Map<String, String> safeGetCookies() {
			if (cookies == null) {
				cookies = new LinkedHashMap<String, String>();
			}

			return cookies;
		}
	}
}
