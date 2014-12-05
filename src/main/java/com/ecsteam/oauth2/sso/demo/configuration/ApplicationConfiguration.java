package com.ecsteam.oauth2.sso.demo.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.cloudfoundry.identity.uaa.client.ClientAuthenticationFilter;
import org.cloudfoundry.identity.uaa.client.OAuth2AccessTokenSource;
import org.cloudfoundry.identity.uaa.client.SocialClientUserDetailsSource;
import org.cloudfoundry.identity.uaa.oauth.RemoteTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.security.oauth2.OAuth2ClientProperties;
import org.springframework.cloud.security.oauth2.ResourceServerProperties;
import org.springframework.cloud.security.sso.OAuth2SsoConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.client.RestOperations;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.ecsteam.oauth2.sso.demo.filter.SessionCreationFilter;
import com.ecsteam.oauth2.sso.demo.interceptor.CookiePropagationInterceptor;
import com.ecsteam.oauth2.sso.demo.interceptor.TokenPropagationInterceptor;
import com.ecsteam.oauth2.sso.demo.listener.CookiePropagationListener;

/**
 * This class provides all necessary spring configuration to show endpoints, and tie them in with a remote OAuth2 Server
 * 
 * @author Josh Ghiloni
 *
 */
public class ApplicationConfiguration {

	/**
	 * This class secures endpoints, and sets the necessary filter chains to handle redirects to the OAuth server for
	 * login and token assignment
	 * 
	 * @author Josh Ghiloni
	 */
	@Configuration
	@EnableWebSecurity
	protected static class WebSecurityConfiguration extends OAuth2SsoConfigurerAdapter {
		/**
		 * This filter is to check user info for authentication info
		 */
		@Autowired
		@Qualifier("socialClientFilter")
		private ClientAuthenticationFilter socialClientFilter;

		/**
		 * This filter is used on backend endpoints
		 */
		@Autowired
		@Qualifier("accessTokenFilter")
		private ClientAuthenticationFilter accessTokenFilter;

		/**
		 * This filter redirects users if they are not authenticated, and should be injected into the filter chain
		 * immediately after the ClientAuthenticationFilter. For this example, all points have this filter, but in
		 * general, services that do not interact directly with users should not use it, and access should simply be
		 * denied to those who do not have an appropriate token.
		 */
		@Autowired
		private OAuth2ClientContextFilter oauth2ClientFilter;

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.authorizeRequests().antMatchers("/favicon.ico", "/resources/**").anonymous();
			http.addFilterBefore(new SessionCreationFilter(), ExceptionTranslationFilter.class)				
				.authorizeRequests()
					.antMatchers("/home").fullyAuthenticated()
					.and()
						.addFilter(socialClientFilter)
						.addFilterBefore(oauth2ClientFilter, socialClientFilter.getClass())
				.authorizeRequests()
					.antMatchers("/service/item/**").fullyAuthenticated()
					.and()
						.addFilter(accessTokenFilter)
						.addFilterAfter(oauth2ClientFilter, accessTokenFilter.getClass())
				.authorizeRequests()
					.anyRequest().permitAll()
				.and()	
					.logout()
						.logoutUrl("/logout.do").permitAll();
			// @formatter:on
		}
	}

	/**
	 * This class sets up operations for this set of service, and is not related to the OAuth2 security of the class
	 * 
	 * @author Josh Ghiloni
	 *
	 */
	@Configuration
	@EnableWebMvc
	protected static class WebMvcConfiguration extends WebMvcConfigurerAdapter {

		@Bean
		public ContentNegotiatingViewResolver contentViewResolver() throws Exception {
			ContentNegotiationManagerFactoryBean contentNegotiationManager = new ContentNegotiationManagerFactoryBean();
			contentNegotiationManager.addMediaType("json", MediaType.APPLICATION_JSON);
			contentNegotiationManager.addMediaType("xml", MediaType.APPLICATION_XML);
			contentNegotiationManager.addMediaType("html", MediaType.TEXT_HTML);

			InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
			viewResolver.setPrefix("/WEB-INF/jsp/");
			viewResolver.setSuffix(".jsp");

			MappingJackson2JsonView defaultView = new MappingJackson2JsonView();
			defaultView.setExtractValueFromSingleKeyModel(true);

			ContentNegotiatingViewResolver contentViewResolver = new ContentNegotiatingViewResolver();
			contentViewResolver.setContentNegotiationManager(contentNegotiationManager.getObject());
			contentViewResolver.setViewResolvers(Arrays.<ViewResolver> asList(viewResolver));
			contentViewResolver.setDefaultViews(Arrays.<View> asList(defaultView));
			return contentViewResolver;
		}

		@Override
		public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
			configurer.enable();
		}
	}

	/**
	 * This class generates all OAuth2-related beans to be used by other configurations or components.
	 * 
	 * Note that the EnableOAuth2Client annotation should only be added to those apps that interact with the user
	 * directly.
	 * 
	 * @author Josh Ghiloni
	 *
	 */
	@Configuration
	@EnableOAuth2Client
	@EnableConfigurationProperties({ OAuth2ClientProperties.class, ResourceServerProperties.class })
	protected static class ServiceBeanConfiguration {
		@Autowired
		private ResourceServerProperties resource;

		@Autowired
		private OAuth2ClientContext oauth2ClientContext;

		/**
		 * Set up the socialClientFilter bean to find the user info URL
		 * 
		 * @param restTemplate The RestTemplate object used to make the remote calls behind the scenes
		 * @return the bean
		 */
		@Bean(name = "socialClientFilter")
		public ClientAuthenticationFilter socialClientFilter(RestOperations restTemplate) {
			SocialClientUserDetailsSource source = new SocialClientUserDetailsSource();
			source.setRestTemplate(restTemplate);
			source.setUserInfoUrl(resource.getUserInfoUri());

			ClientAuthenticationFilter filter = new ClientAuthenticationFilter("/login");
			filter.setPreAuthenticatedPrincipalSource(source);

			return filter;
		}

		/**
		 * Set up the accessTokenFilter to be able to fetch access tokens for authenticated users
		 * 
		 * @param restTemplate The RestTemplate object used to make the remote calls behind the scenes
		 * @param manager
		 * @return the bean
		 */
		@Bean(name = "accessTokenFilter")
		public ClientAuthenticationFilter accessTokenFilter(OAuth2RestTemplate restTemplate,
				OAuth2AuthenticationManager manager) {
			OAuth2AccessTokenSource source = new OAuth2AccessTokenSource();
			source.setRestTemplate(restTemplate);

			ClientAuthenticationFilter filter = new ClientAuthenticationFilter("/login");
			filter.setPreAuthenticatedPrincipalSource(source);
			filter.setAuthenticationManager(manager);

			return filter;
		}

		/**
		 * Create a ServletContextInitializer bean used by Spring to add information to the servlet context. This is
		 * used in place of the &lt;listener&gt;...&lt;/listener&gt; elements of web.xml.
		 * 
		 * <b>Note</b>: This bean must be created as early as possible, hence the Order(0) annotation. The
		 * RequestContextListener instance adds request information to the current thread, allowing for session- and
		 * request-scoped beans to be created, which is necessary for OAuth2 to function properly.
		 * 
		 * @return
		 */
		@Bean
		@Order(0)
		public ServletContextInitializer authzPropInitializer() {
			return new ServletContextInitializer() {
				@Override
				public void onStartup(ServletContext servletContext) throws ServletException {
					/*
					 * 
					 * There are three method signatures for addListener that shoud -- theoretically -- work identically
					 * to a consumer of the API. However, there is an issue with the embedded Tomcat used by Spring Boot
					 * and Cloud Foundry that cause an issue if you pass the Class object or its name, and your service
					 * will fail to start. For now, just use the instance-based signature which bypasses the affected
					 * code
					 */
					servletContext.addListener(new RequestContextListener());
					servletContext.addListener(new CookiePropagationListener());
				}
			};
		}

		/**
		 * Create a token services bean with info about the remote OAuth Server
		 * @return
		 */
		@Bean
		public RemoteTokenServices remoteTokenServices() {
			RemoteTokenServices rts = new RemoteTokenServices();

			rts.setCheckTokenEndpointUrl(resource.getTokenInfoUri());
			rts.setClientId(resource.getClient().getClientId());
			rts.setClientSecret(resource.getClient().getClientSecret());

			return rts;
		}

		/**
		 * Create an authentication manager that uses the remote OAuth Server. Used by the accessTokenServer
		 * 
		 * @param remoteTokenServices the RemoteTokenServices bean
		 * @return
		 */
		@Bean
		public OAuth2AuthenticationManager oauth2AuthenticationManager(RemoteTokenServices remoteTokenServices) {
			OAuth2AuthenticationManager oam = new OAuth2AuthenticationManager();
			oam.setTokenServices(remoteTokenServices);

			return oam;
		}

		/**
		 * Create a RestTemplate bean with information about the OAuth2 client, OAuth2 server, and custom interceptors
		 * to ensure that the current OAuth2 access token and cookies -- including the session id which is necessary for
		 * our implementation -- are persisted to any calls made by this application
		 *
		 * @return
		 */
		@Bean
		public OAuth2RestTemplate restTemplate() {
			AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
			details.setAccessTokenUri(resource.getClient().getTokenUri());
			details.setId(resource.getId());
			details.setClientId(resource.getClient().getClientId());
			details.setClientSecret(resource.getClient().getClientSecret());
			details.setUserAuthorizationUri(resource.getClient().getAuthorizationUri());
			details.setUseCurrentUri(true);

			OAuth2RestTemplate template = new OAuth2RestTemplate(details, oauth2ClientContext);

			List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
			interceptors.add(new TokenPropagationInterceptor(template));
			interceptors.add(new CookiePropagationInterceptor());

			template.setInterceptors(interceptors);

			return template;
		}
	}
}
