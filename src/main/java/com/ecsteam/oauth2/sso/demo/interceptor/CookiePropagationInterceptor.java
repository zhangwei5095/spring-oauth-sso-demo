package com.ecsteam.oauth2.sso.demo.interceptor;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import com.ecsteam.oauth2.sso.demo.context.CookieJar;

/**
 * An interceptor that will take all cookies on the current request and persist them to the request about to be made.
 * Designed for ensuring the persistence of sessions across calls, but will copy all cookies.
 * @author Josh Ghiloni
 *
 */
public class CookiePropagationInterceptor extends SkippableInterceptor {
	private static final Log log = LogFactory.getLog(CookiePropagationInterceptor.class);

	@Override
	protected ClientHttpResponse interceptInternal(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		final String method = "intercept";

		log.info("ENTER " + method);

		// You cannot modify the headers of an existing HttpRequest object, so wrap it. 
		HttpRequestWrapper wrapper = new HttpRequestWrapper(request);

		/*
		 * most http servers only work with one Cookie: header, so combine all cookies into a single header using the
		 * standard syntax; that is:
		 * 
		 * Cookie: name1=value1; name2=value2; ...
		 */
		Collection<String> cookies = CookieJar.getCookieNames();
		StringBuilder builder = new StringBuilder();
		if (cookies != null) {
			for (String cookieName : cookies) {
				String cookieValue = CookieJar.findCookieByName(cookieName);
				builder.append(cookieName).append('=').append(cookieValue).append("; ");
			}
		}

		if (builder.length() > 0) {
			wrapper.getHeaders().add("Cookie", builder.toString());
		}

		log.info("EXIT " + method);

		return execution.execute(wrapper, body);
	}
}
