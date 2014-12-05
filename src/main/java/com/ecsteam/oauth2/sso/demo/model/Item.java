package com.ecsteam.oauth2.sso.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A simple pojo model used by service calls
 * 
 * @author Josh Ghiloni
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
	private String id;

	private String description;

	private double price;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
