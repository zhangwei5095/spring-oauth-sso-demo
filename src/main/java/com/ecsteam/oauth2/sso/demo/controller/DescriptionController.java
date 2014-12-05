package com.ecsteam.oauth2.sso.demo.controller;

import org.springframework.cloud.security.resource.EnableOAuth2Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecsteam.oauth2.sso.demo.model.ItemDescription;

/**
 * A trivial REST Controller that returns the "description" of a fictional item. Used to prove OAuth2 security.
 * 
 * @author Josh Ghiloni
 *
 */
@RestController
@EnableOAuth2Resource
public class DescriptionController {
	/**
	 * Given an arbitrary string as an id, will return "Description for item id {id}"
	 * 
	 * @param id
	 * @return
	 */	
	@RequestMapping("/service/item/description/{id}")
	public ItemDescription getItemDescription(@PathVariable("id") String id) {
		ItemDescription Description = new ItemDescription();
		Description.setId(id);
		Description.setDescription(String.format("Description for item id %s", id));

		return Description;
	}
}
