package com.ecsteam.oauth2.sso.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import com.ecsteam.oauth2.sso.demo.model.Item;
import com.ecsteam.oauth2.sso.demo.model.ItemDescription;
import com.ecsteam.oauth2.sso.demo.model.ItemPrice;

/**
 * A service class that controllers can delegate to for making service-to-service calls. 
 * 
 * @author Josh Ghiloni
 */
@Service
public class ItemService {
	@Autowired
	private RestOperations restTemplate;

	@Value("${demoapp.url:http://localhost:8080}")
	private String baseUri;
	
	public Item getItem(String itemId) {
		final String pattern = "{baseUri}/service/item/byId/{itemId}";
		return restTemplate.getForObject(pattern, Item.class, baseUri, itemId);
	}

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
