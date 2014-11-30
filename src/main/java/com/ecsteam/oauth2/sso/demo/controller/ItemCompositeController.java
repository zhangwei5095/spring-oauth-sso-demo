package com.ecsteam.oauth2.sso.demo.controller;

import org.springframework.cloud.security.resource.EnableOAuth2Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecsteam.oauth2.sso.demo.model.Item;
import com.ecsteam.oauth2.sso.demo.service.ItemService;

@RestController
@EnableOAuth2Resource
public class ItemCompositeController {
	private ItemService service;

	public ItemCompositeController() {
	}

	public void setItemService(ItemService service) {
		this.service = service;
	}

	@RequestMapping("/service/item/byId/{id}")
	public Item getItem(@PathVariable("id") String id) {
		Item item = new Item();
		item.setId(id);
		item.setPrice(service.getItemPrice(id));
		item.setDescription(service.getItemDescription(id));

		return item;
	}
}
