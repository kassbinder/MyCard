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

	public Boolean removeAccount(int accountID) throws Exception;

	public Boolean transfer(String accountNumber1, String accountNumber2, Float amount) throws Exception;

	public List<Item> listItems(String accountNumber) throws Exception;

	public Boolean addItem(String accountNumber, float amount) throws Exception;

	public Float accountBalance(int accountID) throws Exception;

	public Float userBalance(String userName) throws Exception;

}
