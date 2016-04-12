package com.w.card.store;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.w.card.domain.Account;
import com.w.card.domain.Bank;
import com.w.card.domain.Item;
import com.w.card.domain.User;

public class JavaSerializationStoreTests {

	@Test
	public void testUsage() throws Exception {
		final File testFile = File.createTempFile("bank", "test");
		final Store store = new JavaSerializationStore(testFile.getAbsolutePath());
		
		final Bank bank = store.loadBank();
		assertNotNull(bank);
		assertTrue(bank.getUsers().isEmpty());
		
		Account acc1 = new Account("123456");
		User user = new User("test");
		user.getAccounts().add(acc1);
		bank.getUsers().add(user);
		acc1.getItems().add(new Item(50));
		
		store.saveBank(bank);
		
		final Bank bank2 = store.loadBank();
		assertEquals(1, bank2.getUsers().get(0).getAccounts().get(0).getItems().size());
		assertTrue(50 == bank2.getUsers().get(0).getAccounts().get(0).calculateBanlance());
		
		testFile.delete();
	}

}
