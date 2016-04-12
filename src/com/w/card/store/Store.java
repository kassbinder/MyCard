package com.w.card.store;

import com.w.card.domain.Bank;

public interface Store {

	public Bank loadBank() throws Exception;
	
	public void saveBank(Bank bank) throws Exception;;

}
