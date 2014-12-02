package com.ecsteam.oauth2.sso.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.security.sso.EnableOAuth2Sso;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ecsteam.oauth2.sso.demo.service.ItemService;

@Controller
@EnableOAuth2Sso
public class UIController {
	@Autowired
	private ItemService service;
	
	@RequestMapping("/home")
	public String getHome(ModelAndView model) {
		return "home";
	}
}
