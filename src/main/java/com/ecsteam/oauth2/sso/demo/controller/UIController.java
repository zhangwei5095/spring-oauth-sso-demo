package com.ecsteam.oauth2.sso.demo.controller;

import org.springframework.cloud.security.resource.EnableOAuth2Resource;
import org.springframework.cloud.security.sso.EnableOAuth2Sso;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@EnableOAuth2Sso
public class UIController {

	@RequestMapping("/home")
	public String getHome(ModelAndView model) {
		return "home";
	}
}
