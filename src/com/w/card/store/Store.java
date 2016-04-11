package com.w.card.store;

import com.w.card.domain.Account;
import com.w.card.domain.Bank;
import com.w.card.domain.Item;

public interface Store {

	public Bank loadBank() throws Exception;

	public void AddItem(Account account, Item item) throws Exception;

}
