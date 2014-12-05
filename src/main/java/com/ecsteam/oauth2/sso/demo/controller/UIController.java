package com.ecsteam.oauth2.sso.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.security.sso.EnableOAuth2Sso;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ecsteam.oauth2.sso.demo.model.Item;
import com.ecsteam.oauth2.sso.demo.service.ItemService;

/**
 * Example of a controller that renders a UI that can be used to make backend calls
 * 
 * @author Josh Ghiloni
 *
 */
@Controller
@EnableOAuth2Sso
public class UIController {
	@Autowired
	private ItemService service;
	
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String getHome(ModelAndView model) {
		return "home";
	}
	
	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public String postHome(@RequestParam("id") String id, ModelAndView model) {
		Item item = service.getItem(id);
		model.addObject("item", item);
		
		return "home";
	}
	
	@RequestMapping("/")
	public String redirectToHome(ModelAndView model) {
		return "redirect:/home";
	}
}
