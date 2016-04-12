package com.w.card.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Iterator;

import com.w.card.domain.Account;
import com.w.card.domain.Bank;
import com.w.card.domain.Item;
import com.w.card.domain.User;
import com.w.card.store.Store;

public class CMDFluntUserInterface implements UserInterface {

	private User scanUser(Bank bank, String name, Store store) throws Exception {
		for (Iterator<User> iu = bank.getUsers().iterator(); iu.hasNext();) {
			User user = iu.next();
			String userName = user.getName();
			if (name.equals(userName)) {
				return user;
			}
		}
		User user = new User(name);
		bank.getUsers().add(user);
		store.saveBank(bank);
		return user;
	}

	private Account scanAccount(Bank bank, String number, Store store, User user) throws Exception {
		for (Iterator<Account> ia = user.getAccounts().iterator(); ia.hasNext();) {
			Account account = ia.next();
			String accountNumber = account.getNumber();
			if (number.equals(accountNumber)) {
				return account;
			}
		}
		Account account = new Account(number);
		user.getAccounts().add(account);
		store.saveBank(bank);
		return account;
	}

	@Override
	public void show(Store store) throws Exception {
		// TODO Auto-generated method stub
		final Bank bank = store.loadBank();

		final BufferedReader bis = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Welcome!");

		while (true) {

			System.out.print("<");
			final String command = bis.readLine();
			if (command.trim().length() == 0) {
				// 列出所有用户
				// 遍历所有用户
				// 打印当前用户别的名字
				for (Iterator<User> iu = bank.getUsers().iterator(); iu.hasNext();) {
					User user = iu.next();
					String name = user.getName();
					System.out.println(name);
				}

			} else {
				// 分割字符串
				String[] s = command.split("\\s+");
				int l = s.length;
				if (l == 1) {
					// 判断这个User是否已经存在。
					// 如果用户不存在，就新建一个。

					User user = this.scanUser(bank, s[0], store);
					// 列出account.

					for (Iterator<Account> ia = user.getAccounts().iterator(); ia.hasNext();) {
						String number = ia.next().getNumber();
						System.out.println(number);
					}
				} else if (l == 2) {
					// 判断有没有这个User，如果没有，就新建一个。
					User user = this.scanUser(bank, s[0], store);
					// 判断有没有account,如果没有，就新建一个。
					Account account = this.scanAccount(bank, s[1], store, user);

					// 列出所有items.
					for (Iterator<Item> ii = account.getItems().iterator(); ii.hasNext();) {
						Item item = ii.next();
						Float amount = item.getAmount();
						Date date = item.getCreatedAt();
						System.out.println(amount + "-" + date);
					}
				} else if (l == 3) {
					User user = this.scanUser(bank, s[0], store);
					Account account = this.scanAccount(bank, s[1], store, user);
					Item item = new Item(Float.valueOf(s[2]));
					account.getItems().add(item);
					store.saveBank(bank);
					System.out.println(account.calculateBanlance());
				}

			}
		}
	}

}
