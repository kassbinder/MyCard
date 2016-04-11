package com.w.card.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.w.card.domain.Account;
import com.w.card.domain.Bank;
import com.w.card.domain.Item;

public class JavaSerializationStore implements Store {

	private final String path;
	
	private Bank bank;

	public JavaSerializationStore(final String path) {
		this.path = path;
	}

	@Override
	public Bank loadBank() throws Exception {
		final File file = new File(path);
		if (!file.exists()) {
			this.bank = new Bank();
			return this.bank;
		}
		final FileInputStream fis = new FileInputStream(path);
		if(fis.available() == 0) {
			fis.close();
			this.bank = new Bank();
			return this.bank;
		}
		final ObjectInputStream ois = new ObjectInputStream(fis);
		this.bank = (Bank) ois.readObject();
		ois.close();
		fis.close();
		return bank;
	}

	@Override
	public void AddItem(final Account account, final Item item) throws Exception {
		account.getItems().add(item);
		final FileOutputStream fos = new FileOutputStream(path);
		final ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this.bank);
		oos.close();
		fos.close();
	}

}
