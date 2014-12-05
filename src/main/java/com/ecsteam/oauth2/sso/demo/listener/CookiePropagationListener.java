package com.ecsteam.oauth2.sso.demo.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ecsteam.oauth2.sso.demo.context.CookieJar;

/**
 * A request listener to grab all cookies off the current request and save them for later use.
 * 
 * As a ServletRequestListener, this will be fired before the filter chain, so that the SessionCreationFilter can
 * overwrite any session cookie found here with the actual session id.
 * 
 * @author Josh Ghiloni
 *
 */
public class CookiePropagationListener implements ServletRequestListener {

	private static final Log log = LogFactory.getLog(CookiePropagationListener.class);

	/**
	 * Clear the cookies for the next request
	 */
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		CookieJar.resetCookies();

	}

	/**
	 * Grab all cookies available to this request and save them for later use
	 */
	@Override
	public void requestInitialized(ServletRequestEvent requestEvent) {
		final String method = "requestInitialized";
		log.debug("ENTER " + method);

		if (!(requestEvent.getServletRequest() instanceof HttpServletRequest)) {
			throw new IllegalArgumentException("Request is not an HttpServletRequest: "
					+ requestEvent.getServletRequest());
		}

		HttpServletRequest request = HttpServletRequest.class.cast(requestEvent.getServletRequest());

		Cookie[] cookies = request.getCookies();

		CookieJar.resetCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				log.debug(String.format("Saving cookie [%s=%s]", cookie.getName(), cookie.getValue()));

				CookieJar.addCookie(cookie);
			}
		}

		log.debug("EXIT " + method);
	}
}
