package com.w.card.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {
	
	private String number;
	
	private List<Item> items = new ArrayList<>();

	public Account(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	public float calculateBanlance() {
		float balance = 0;
		for(Item item : this.items) {
			balance += item.getAmount();
		}
		return balance;
	}

}
