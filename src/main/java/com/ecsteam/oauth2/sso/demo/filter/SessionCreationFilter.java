package com.ecsteam.oauth2.sso.demo.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.filter.GenericFilterBean;

import com.ecsteam.oauth2.sso.demo.context.CookieJar;

/**
 * OAuth2 Authorization Code grant-based tokens are session-dependent. This filter ensures that a session 
 * is always created before any calls are made from one protected service to another. 
 * 
 * @author Josh Ghiloni
 */
public class SessionCreationFilter extends GenericFilterBean {

	private static final Log log = LogFactory.getLog(SessionCreationFilter.class);

	/**
	 * Create a session if one has not already been created, and save the session id for later use.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		final String method = "doFilter";
		
		log.debug(String.format("ENTER %s", method));
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = HttpServletRequest.class.cast(request);

			// get the session and return null if one has not been created
			HttpSession session = httpRequest.getSession(false);
			if (session == null) {
				log.debug("Session null, creating...");
				// get the session and create one if it's null
				session = httpRequest.getSession(true);
			} else {
				log.debug("Session not null");
			}
			
			if (session != null) {
				ServletContext context = getServletContext();
				if (context != null) {
					SessionCookieConfig scc = context.getSessionCookieConfig();
					if (scc != null) {
						// get the name of the session cookie (usually JSESSIONID)
						String name = scc.getName();
						if (name != null) {
							CookieJar.setSessionCookieName(name);
						}
					}
				}
				
				CookieJar.setSessionId(session.getId());
			} else {
				log.error("Session still null. This shouldn't happen.");
				throw new IllegalStateException();
			}
		}
		
		chain.doFilter(request, response);
		log.debug(String.format("EXIT %s", method));
	}
}
