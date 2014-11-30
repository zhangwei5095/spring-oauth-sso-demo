package com.ecsteam.oauth2.sso.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import com.ecsteam.oauth2.sso.demo.model.ItemDescription;
import com.ecsteam.oauth2.sso.demo.model.ItemPrice;

@Service
public class ItemService {
	private RestOperations restTemplate;

	private String baseUri;

	public double getItemPrice(String itemId) {
		final String pattern = "{baseUri}/service/item/price/{itemId}";
		
		ItemPrice price = restTemplate.getForObject(pattern, ItemPrice.class, baseUri, itemId);
		
		return price.getPrice();
	}
	
	public String getItemDescription(String itemId) {
		final String pattern = "{baseUri}/service/item/description/{itemId}";
		
		ItemDescription desc = restTemplate.getForObject(pattern, ItemDescription.class, baseUri, itemId);
		
		return desc.getDescription();
	}
	
	public void setRestTemplate(RestOperations restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}
}
