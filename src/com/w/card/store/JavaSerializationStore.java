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

	public JavaSerializationStore(final String path) {
		this.path = path;
	}

	@Override
	public Bank loadBank() throws Exception {
		final File file = new File(path);
		if (!file.exists()) {
			return new Bank();
		}
		final FileInputStream fis = new FileInputStream(path);
		if (fis.available() == 0) {
			fis.close();
			return new Bank();
		}
		final ObjectInputStream ois = new ObjectInputStream(fis);
		Bank bank = (Bank) ois.readObject();
		ois.close();
		fis.close();
		return bank;
	}

	@Override
	public void saveBank(final Bank bank) throws Exception {
		final FileOutputStream fos = new FileOutputStream(path);
		final ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(bank);
		oos.close();
		fos.close();
	}

}
