package com.w.card.domain.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Optional;

import com.w.card.domain.Account;
import com.w.card.domain.Bank;
import com.w.card.domain.Item;
import com.w.card.domain.User;
import com.w.card.store.Store;

public class CMDUserInterface implements UserInterface {

	@Override
	public void show(final Store store) throws Exception {
		final Bank bank = store.loadBank();
		User currentUser = null;
		Account currentAccount = null;

		final BufferedReader bis = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Welcome!");

		while (true) {
			System.out.print("> ");
			final String command = bis.readLine();
			if ("exit".equals(command)) {
				System.out.println("Bye!");
				System.exit(0);
			} else if ("list".equals(command)) {
				if (null == currentUser) {
					for (Iterator<User> it = bank.getUsers().iterator(); it.hasNext();) {
						System.out.println(it.next().getName());
					}
				} else if (null == currentAccount) {
					for (Iterator<Account> ia = currentUser.getAccounts().iterator(); ia.hasNext();) {
						Account acc = ia.next();
						System.out.println(acc.getNumber() + "-" + acc.calculateBanlance());
					}

				}
			} else if (command.startsWith("select ")) {
				final String key = command.substring(7);
				if (null == currentUser) {
					Iterator<User> iu = bank.getUsers().iterator();
					while (iu.hasNext()) {
						User user = iu.next();
						if (user.getName().equals(key)) {
							currentUser = user;
							break;
						}
					}

					if (currentUser == null) {
						currentUser = new User(key);
						bank.getUsers().add(currentUser);
					}
				} else if (null == currentAccount) {
					if ("^".equals(key)) {
						currentUser = null;
						continue;
					}
					for (Iterator<Account> ia = currentUser.getAccounts().iterator(); ia.hasNext();) {
						Account acc = ia.next();
						if (acc.getNumber().equals(key)) {
							currentAccount = acc;
							break;
						}
					}
					Optional<Account> account = currentUser.getAccounts().stream()
							.filter(acc -> acc.getNumber().equals(key)).findFirst();
					if (account.isPresent()) {
						currentAccount = account.get();
					} else {
						currentAccount = new Account(key);
						currentUser.getAccounts().add(currentAccount);
					}
				} else if ("^".equals(key)) {
					currentAccount = null;
					continue;
				}
			} else if (command.startsWith("add ") && null != currentAccount) {
				final float amount = Float.valueOf(command.substring(4));
				final Item item = new Item(amount);
				store.AddItem(currentAccount, item);
				System.out.println("Balance: " + currentAccount.calculateBanlance());
			}
		}
	}

}
