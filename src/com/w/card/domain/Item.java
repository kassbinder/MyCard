package com.w.card.domain;

import java.io.Serializable;
import java.util.Date;

public class Item implements Serializable {

	private float amount;

	private Date createdAt = new Date();

	public Item(float amount) {
		this.amount = amount;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
