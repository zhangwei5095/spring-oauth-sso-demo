package com.ecsteam.oauth2.sso.demo.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.security.sso.OAuth2SsoConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
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

//		@Override
//		public void configure(WebSecurity web) throws Exception {
//			web.ignoring().antMatchers("/resources/**");
//		}

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.authorizeRequests()
				.antMatchers("/service/item/**", "/home").hasRole("USER")
				.anyRequest().permitAll()
			.and()
				.addFilterAfter(oauth2ClientFilter, ExceptionTranslationFilter.class)
			.logout()
				.logoutUrl("/logout.do").permitAll()
			.and()
				.formLogin()
					.loginPage("http://wv4-demo-login.cfapps.digitalglobe.com");
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
	protected static class ServiceBeanConfiguration {
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
	}
}
