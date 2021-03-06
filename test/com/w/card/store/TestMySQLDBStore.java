package com.w.card.store;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.w.card.domain.Account;
import com.w.card.domain.Item;
import com.w.card.domain.User;

public class TestMySQLDBStore {
	Connection conn;
	MySQLDBStore msql;

	public Connection getConnection() {

		try {
			return DriverManager.getConnection("jdbc:mysql://192.168.0.115:3306/MyCard", "root", "root");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void setUp() throws Exception {
		conn = this.getConnection();
		msql = new MySQLDBStore();

	}

	@After
	public void tearDown() throws Exception {
		conn.close();
	}

	@Test
	public void testListUsers() throws Exception {
		String userName = "UserA";
		Statement statement = conn.createStatement();
		String delete = "delete from User";
		statement.executeUpdate(delete);
		String mySQL = "insert into User (name) values ('" + userName + "')";
		int insertCount = statement.executeUpdate(mySQL);
		assertEquals(1, insertCount);
		List<User> lUser = msql.listUsers();
		assertEquals(1, lUser.size());
		assertEquals(userName, lUser.get(0).getName());
		statement.executeUpdate(delete);
	}

	@Test
	public void testAddUser() throws Exception {
		String userName = "UserB";
		Statement statement = conn.createStatement();
		String delete = "delete from User";
		statement.executeUpdate(delete);
		assertEquals(true, msql.addUser(userName));
		assertEquals(false, msql.addUser(userName));
		List<User> lUser = msql.listUsers();
		assertEquals(1, lUser.size());
		assertEquals(userName, lUser.get(0).getName());
		statement.executeUpdate(delete);
	}

	@Test
	public void testRemoveUser() throws Exception {
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
		String user = "UserB";
		String user2 = "UserC";
		String account2a = "C001";
		Float amount2a = 200.00f;
		msql.addUser(user);
		assertTrue(msql.removeUser(user));
		msql.addAccount(user2, account2a);
		msql.addItem(account2a, amount2a);
		assertFalse(msql.removeUser(user2));
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
	}

	@Test
	public void testListAccounts() throws Exception {
		String userName = "UserC";
		String addUser = "insert into User (name,id) values('" + userName + "',1)";
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		assertEquals(1, statement.executeUpdate(addUser));
		String cAccount1 = "UC001";
		String cAccount2 = "UC002";
		String addAccount1 = "INSERT INTO Account (number,user_id) values('" + cAccount1 + "',1)";
		assertEquals(1, statement.executeUpdate(addAccount1));
		String addAccount2 = "INSERT INTO Account (number,user_id) values('" + cAccount2 + "',1)";
		assertEquals(1, statement.executeUpdate(addAccount2));
		List<Account> lAccount = msql.listAccounts(userName);
		assertEquals(2, lAccount.size());
		assertEquals(cAccount1, lAccount.get(0).getNumber());
		assertEquals(cAccount2, lAccount.get(1).getNumber());
		assertEquals(1, lAccount.get(0).getUserId());
		assertEquals(1, lAccount.get(1).getUserId());
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
	}

	@Test
	public void testAddAccount() throws Exception {
		String userName = "UserD";
		String accountNumber = "D001";
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		String addUser = "INSERT INTO User (name,id) values('" + userName + "',1)";
		assertEquals(1, statement.executeUpdate(addUser));
		String addAccount = "INSERT INTO Account (number,user_id) values('" + accountNumber + "',1)";
		assertEquals(1, statement.executeUpdate(addAccount));
		assertEquals(false, msql.addAccount(userName, accountNumber));
		String userName2 = "UserE";
		String accountNumber2 = "E001";
		assertEquals(true, msql.addAccount(userName2, accountNumber2));
		assertEquals(false, msql.addAccount(userName2, accountNumber2));
		assertEquals(false, msql.addAccount(userName2, accountNumber));
		List<User> lUser = msql.listUsers();
		List<Account> lAccount = msql.listAccounts(userName2);
		assertEquals(2, lUser.size());
		assertEquals(userName2, lUser.get(1).getName());
		assertEquals(1, lAccount.size());
		assertEquals(accountNumber2, lAccount.get(0).getNumber());
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
	}

	@Test
	public void testRemoveAccount() throws Exception {
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
		String user = "UserF";
		String account = "F001";
		String user2 = "UserG";
		String accountA = "G001";
		Float amountA = 3.00f;
		msql.addAccount(user, account);
		List<Account> lAccount = msql.listAccounts(user);
		assertEquals(1, lAccount.size());
		int accountId = lAccount.get(0).getId();
		System.out.println("accountId�ǣ�" + accountId);
		System.out.println("�˻�����ǣ�" + msql.accountBalance(accountId));
		assertTrue(msql.removeAccount(accountId));
		List<Account> lAccount1 = msql.listAccounts(user);
		assertTrue(lAccount1.isEmpty());
		msql.addAccount(user2, accountA);
		msql.addItem(accountA, amountA);
		List<Account> lAccount2 = msql.listAccounts(user2);
		int accountAId = lAccount2.get(0).getId();
		assertFalse(msql.removeAccount(accountAId));
		String accountB = "G002";
		Float amountB = 0.00f;
		msql.addAccount(user2, accountB);
		msql.addItem(accountB, amountB);
		List<Account> lAccount3 = msql.listAccounts(user2);
		int accountBId = lAccount3.get(1).getId();
		assertTrue(msql.removeAccount(accountBId));
		List<Account> lAccount4 = msql.listAccounts(user2);
		assertEquals(1, lAccount4.size());
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
	}

	@Test
	public void testTransfer() throws Exception {
		String user = "UserA";
		String account = "A001";
		Float amount = 1000.00f;
		String user2 = "UserB";
		String account2 = "B001";
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
		msql.addAccount(user, account);
		msql.addAccount(user2, account2);
		msql.addItem(account, amount);
		msql.transfer(account, account2, amount);
		Float user1B = msql.userBalance(user);
		assertTrue(user1B == 0f);
		Float user2B = msql.userBalance(user2);
		assertEquals(amount, user2B);
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
	}

	@Test
	public void testListItems() throws Exception {
		String user = "UserG";
		String account = "G001";
		int userId = 1;
		int accountId = 1;
		int itemId = 1;
		Float amount = 100.86f;
		Date javaDate = new Date();
		Timestamp ts = new Timestamp(javaDate.getTime());
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
		String addUser = "INSERT INTO User (id,name) VALUES (" + userId + ",'" + user + "')";
		assertEquals(1, statement.executeUpdate(addUser));
		String addAccount = "INSERT INTO Account (id,user_id,number) values (" + accountId + "," + userId + ",'"
				+ account + "')";
		assertEquals(1, statement.executeUpdate(addAccount));
		String addItem = "INSERT INTO Item (id,account_id,amount,createdAt) VALUES (" + itemId + "," + accountId + ","
				+ amount + ",'" + ts + "')";
		assertEquals(1, statement.executeUpdate(addItem));
		List<Item> lItem = msql.listItems(account);
		assertEquals(1, lItem.size());
		assertEquals(accountId, lItem.get(0).getAccountId());
		assertEquals(amount, lItem.get(0).getAmount());
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
	}

	@Test
	public void testAddItem() throws Exception {
		String user = "H";
		String account = "H001";
		Float amount = 888.86f;
		Date javaDate = new Date();
		Timestamp ts = new Timestamp(javaDate.getTime());
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
		msql.addAccount(user, account);
		msql.addItem(account, amount);
		List<Item> lItem = msql.listItems(account);
		assertEquals(1, lItem.size());
		assertEquals(amount, lItem.get(0).getAmount());
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
	}

	@Test
	public void testAccountBalance() throws Exception {
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
		String user = "J";
		String account = "J001";
		Float in = 100.00f;
		Float out = -10.00f;
		Float balance = in + out;
		msql.addAccount(user, account);
		msql.addItem(account, in);
		List<Item> lItem = msql.listItems(account);
		assertEquals(in, lItem.get(0).getAmount());
		msql.addItem(account, out);
		int accountId = lItem.get(0).getAccountId();
		assertEquals(balance, msql.accountBalance(accountId));
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
	}

	@Test
	public void testUserBalance() throws Exception {
		Statement statement = conn.createStatement();
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");
		String user = "K";
		String account1 = "K001";
		Float balance1 = 300.00f;
		String account2 = "K002";
		Float balance2 = 600.00f;
		Float userBalance = balance1 + balance2;
		msql.addAccount(user, account1);
		msql.addAccount(user, account2);
		msql.addItem(account1, balance1);
		msql.addItem(account2, balance2);
		List<User> lUser = msql.listUsers();
		int userId = lUser.get(0).getId();
		assertEquals(userBalance, msql.userBalance(user));
		statement.executeUpdate("delete from User");
		statement.executeUpdate("delete from Account");
		statement.executeUpdate("delete from Item");

	}

}
