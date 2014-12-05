package com.ecsteam.oauth2.sso.demo.interceptor;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

/**
 * An abstract interceptor that allows its subclasses to be skipped during RestTemplate execution. If a user adds the
 * header
 * 
 * x-skip-interceptor: fully.qualified.class.name.of.interceptor
 * 
 * To their request, that interceptor -- if it is a subclass of SkippableInterceptor -- will not be executed, and the
 * request will be passed up the interceptor chain
 * 
 * @author Josh Ghiloni
 *
 */
public abstract class SkippableInterceptor implements ClientHttpRequestInterceptor {
	private static final Log log = LogFactory.getLog(SkippableInterceptor.class);

	public static final String HEADER_NAME = "x-skip-interceptor";

	/**
	 * Standard method for intercepting requests
	 */
	@Override
	public final ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		final String method = "intercept";

		log.debug(String.format("ENTER %s", method));

		ClientHttpResponse response = null;
		if (shouldSkip(request)) {
			log.debug("Skipping interceptor code, executing directly");
			response = execution.execute(request, body);
		}
		else {
			log.debug("Executing interceptor code");
			response = interceptInternal(request, body, execution);

		}

		log.debug(String.format("EXIT %s", method));
		return response;
	}

	/**
	 * Decide if the interceptor should be skipped or not
	 * @param request The original request (original as it was presented to the interceptor)
	 * @return
	 */
	private boolean shouldSkip(HttpRequest request) {
		final String method = "shouldSkip";

		log.debug("ENTER " + method);

		boolean doSkip = false;

		HttpHeaders headers = request.getHeaders();
		List<String> values = headers.get(HEADER_NAME);

		String valueToCheck = getSkipValue();
		if (StringUtils.hasText(valueToCheck)) {

			if (values != null && values.size() > 0) {
				for (String skip : values) {
					// HTTP Spec indicates that headers are not case-sensitive, so we shouldn't be either.
					if (valueToCheck.equalsIgnoreCase(skip)) {
						doSkip = true;
						break;
					}
				}
			}
		}

		log.debug(String.format("EXIT %s: %s", method, doSkip));

		return doSkip;
	}

	private String getSkipValue() {
		return getClass().getName();
	}

	/**
	 * This is the method that subclasses will override to inject their interception logic
	 * 
	 * @param request
	 * @param body
	 * @param execution
	 * @return
	 * @throws IOException
	 */
	protected abstract ClientHttpResponse interceptInternal(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException;
}
