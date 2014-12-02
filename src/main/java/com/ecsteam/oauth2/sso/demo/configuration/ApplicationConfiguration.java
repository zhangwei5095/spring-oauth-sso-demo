package com.ecsteam.oauth2.sso.demo.configuration;

import java.util.Arrays;

import org.cloudfoundry.identity.uaa.client.ClientAuthenticationFilter;
import org.cloudfoundry.identity.uaa.client.OAuth2AccessTokenSource;
import org.cloudfoundry.identity.uaa.client.SocialClientUserDetailsSource;
import org.cloudfoundry.identity.uaa.oauth.RemoteTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.security.oauth2.OAuth2ClientProperties;
import org.springframework.cloud.security.oauth2.ResourceServerProperties;
import org.springframework.cloud.security.sso.OAuth2SsoConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.client.RestOperations;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.ecsteam.oauth2.sso.demo.controller.ItemCompositeController;
import com.ecsteam.oauth2.sso.demo.service.ItemService;

public class ApplicationConfiguration {

	@Configuration
	@EnableWebSecurity
	protected static class WebSecurityConfiguration extends OAuth2SsoConfigurerAdapter {

		@Autowired
		private OAuth2ClientContextFilter oauth2ClientFilter;

		@Autowired
		@Qualifier("socialClientFilter")
		private ClientAuthenticationFilter socialClientFilter;
		
		@Autowired
		@Qualifier("accessTokenFilter")
		private ClientAuthenticationFilter accessTokenFilter;

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.authorizeRequests()
					.antMatchers("/home").hasRole("USER")
					.and()
						.securityContext().securityContextRepository(securityContextRepository())
					.and()
						.addFilter(socialClientFilter)
						.addFilterBefore(oauth2ClientFilter, socialClientFilter.getClass())
				.authorizeRequests()
					.antMatchers("/service/item/**").hasRole("USER")
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
		
		private SecurityContextRepository securityContextRepository() {
			HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
			repo.setSpringSecurityContextKey("SOCIAL_SPRING_SECURITY_CONTEXT");
			repo.setAllowSessionCreation(true);
			
			return repo;
		}
	}

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

	@Configuration
	@EnableOAuth2Client
	@EnableConfigurationProperties({ OAuth2ClientProperties.class, ResourceServerProperties.class })
	protected static class ServiceBeanConfiguration {
		@Autowired
		private ResourceServerProperties resource;
		
		@Autowired
		private OAuth2ClientContext oauth2ClientContext;

		@Bean
		@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
		public ItemService itemService(@Value("${demoapp.url:http://localhost:8080}") String appUrl,
				RestOperations restTemplate) {

			ItemService service = new ItemService();
			service.setBaseUri(appUrl);
			service.setRestTemplate(restTemplate);

			return service;
		}

		@Bean
		@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
		public ItemCompositeController itemCompositeController(ItemService service) {
			ItemCompositeController controller = new ItemCompositeController();
			controller.setItemService(service);

			return controller;
		}

		@Bean(name = "socialClientFilter")
		@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
		public ClientAuthenticationFilter socialClientFilter(RestOperations restTemplate) {
			SocialClientUserDetailsSource source = new SocialClientUserDetailsSource();
			source.setRestTemplate(restTemplate);
			source.setUserInfoUrl(resource.getUserInfoUri());

			ClientAuthenticationFilter filter = new ClientAuthenticationFilter("/login");
			filter.setPreAuthenticatedPrincipalSource(source);

			return filter;
		}
		
		@Bean(name = "accessTokenFilter")
		@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
		public ClientAuthenticationFilter accessTokenFilter(OAuth2RestTemplate restTemplate, OAuth2AuthenticationManager manager) {
			OAuth2AccessTokenSource source = new OAuth2AccessTokenSource();
			source.setRestTemplate(restTemplate);
					
			ClientAuthenticationFilter filter = new ClientAuthenticationFilter("/login");
			filter.setPreAuthenticatedPrincipalSource(source);
			filter.setAuthenticationManager(manager);
			
			return filter;
		}

		@Bean
		public RemoteTokenServices remoteTokenServices() {
			RemoteTokenServices rts = new RemoteTokenServices();
			
			rts.setCheckTokenEndpointUrl(resource.getTokenInfoUri());
			rts.setClientId(resource.getClient().getClientId());
			rts.setClientSecret(resource.getClient().getClientSecret());
			
			return rts;
		}
		
		@Bean
		public OAuth2AuthenticationManager oauth2AuthenticationManager(RemoteTokenServices remoteTokenServices) {
			OAuth2AuthenticationManager oam = new OAuth2AuthenticationManager();
			oam.setTokenServices(remoteTokenServices);
			
			return oam;
		}
		
		@Bean
		@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
		public OAuth2RestTemplate restTemplate() {
			AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
			details.setAccessTokenUri(resource.getClient().getTokenUri());
			details.setId("uaa");
			details.setClientId(resource.getClient().getClientId());
			details.setClientSecret(resource.getClient().getClientSecret());
			details.setUserAuthorizationUri(resource.getClient().getAuthorizationUri());
			details.setUseCurrentUri(true);
			
			return new OAuth2RestTemplate(details);
		}
	}
}
