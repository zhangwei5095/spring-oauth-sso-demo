package com.ecsteam.oauth2.sso.demo.controller;

import org.springframework.cloud.security.resource.EnableOAuth2Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecsteam.oauth2.sso.demo.model.ItemPrice;

/** 
 * Trivial example of an oauth2-protected resource.
 * 
 * @author Josh Ghiloni
 *
 */
@RestController
@EnableOAuth2Resource
public class PriceController {
	@RequestMapping("/service/item/price/{id}")
	public ItemPrice getItemPrice(@PathVariable("id") String id) {
		ItemPrice price = new ItemPrice();
		price.setId(id);
		price.setPrice(id.length() + 0.99);
		
		return price;
	}
}
