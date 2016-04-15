package com.w.card.store;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.w.card.domain.Account;
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
		String userName = "UserB";
		Statement statement = conn.createStatement();
		String delete = "delete from User";
		statement.executeUpdate(delete);
		assertEquals(false, msql.removeUser(userName));
		statement.executeUpdate("insert into User (name) values('" + userName + "')");
		assertEquals(true, msql.removeUser(userName));
		String sql = ("delete from User where name = '" + userName + "'");
		assertEquals(0, statement.executeUpdate(sql));
		statement.executeUpdate(delete);
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
		// List<Account> lAccount = msql.listAccounts(userName);
		// assertEquals(2, lAccount.size());
		// assertEquals(userName, lUser.get(0).getName());
		// statement.executeUpdate(delete);
	}

	@Test
	public void testAddAccount() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveAccount() {
		fail("Not yet implemented");
	}

	@Test
	public void testListItems() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddItem() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveItem() {
		fail("Not yet implemented");
	}

	@Test
	public void testAccountBalance() {
		fail("Not yet implemented");
	}

	@Test
	public void testUserBalance() {
		fail("Not yet implemented");
	}

}
