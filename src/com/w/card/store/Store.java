package com.w.card.store;

import java.util.List;

import com.w.card.domain.Account;
import com.w.card.domain.Item;
import com.w.card.domain.User;

public interface Store {

	public List<User> listUsers() throws Exception;

	public Boolean addUser(String userName) throws Exception;

	public Boolean removeUser(String userName) throws Exception;

	public List<Account> listAccounts(String userName) throws Exception;

	public Boolean addAccount(String userName, String accountNumber) throws Exception;

	public Boolean removeAccount(String accountNumber) throws Exception;

	public List<Item> listItems(String accountNumber) throws Exception;

	public Boolean addItem(String accountNumber, float amount) throws Exception;

	public Boolean removeItem(int itemID) throws Exception;

	public Float accountBalance(int accountID) throws Exception;

	public Float userBalance(int userID) throws Exception;

}
