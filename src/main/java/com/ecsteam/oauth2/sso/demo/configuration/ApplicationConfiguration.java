package com.ecsteam.oauth2.sso.demo.configuration;

import java.util.Arrays;

import javax.annotation.Resource;

import org.cloudfoundry.identity.uaa.client.ClientAuthenticationFilter;
import org.cloudfoundry.identity.uaa.client.SocialClientUserDetailsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.security.oauth2.OAuth2ClientProperties;
import org.springframework.cloud.security.oauth2.ResourceServerProperties;
import org.springframework.cloud.security.sso.OAuth2SsoConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.access.ExceptionTranslationFilter;
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

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.authorizeRequests()
				.antMatchers("/service/item/**", "/home").hasRole("USER")
				.anyRequest().permitAll()
			.and()
				.addFilterAfter(oauth2ClientFilter, ExceptionTranslationFilter.class)
				.addFilterAfter(socialClientFilter, oauth2ClientFilter.getClass())
			.logout()
				.logoutUrl("/logout.do").permitAll();
			// @formatter:on
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

		@Resource
		@Qualifier("accessTokenRequest")
		private AccessTokenRequest accessTokenRequest;

		@Bean
		public ItemService itemService(@Value("${demoapp.url:http://localhost:8080}") String appUrl,
				RestOperations restTemplate) {

			ItemService service = new ItemService();
			service.setBaseUri(appUrl);
			service.setRestTemplate(restTemplate);

			return service;
		}

		@Bean
		public ItemCompositeController itemCompositeController(ItemService service) {
			ItemCompositeController controller = new ItemCompositeController();
			controller.setItemService(service);

			return controller;
		}

		@Bean(name = "socialClientFilter")
		public ClientAuthenticationFilter socialClientFilter(RestOperations restTemplate) {
			SocialClientUserDetailsSource source = new SocialClientUserDetailsSource();
			source.setRestTemplate(restTemplate);
			source.setUserInfoUrl(resource.getUserInfoUri());

			ClientAuthenticationFilter filter = new ClientAuthenticationFilter("/login");
			filter.setPreAuthenticatedPrincipalSource(source);

			return filter;
		}

		@Bean
		public RestOperations restTemplate() {
			AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
			details.setAccessTokenUri(resource.getClient().getTokenUri());
			details.setId("uaa");
			details.setClientId(resource.getClient().getClientId());
			details.setClientSecret(resource.getClient().getClientSecret());
			details.setUserAuthorizationUri(resource.getClient().getAuthorizationUri());

			OAuth2ClientContext oauth2ClientContext = new DefaultOAuth2ClientContext(accessTokenRequest);
			
			return new OAuth2RestTemplate(details, oauth2ClientContext);
		}
	}
}
