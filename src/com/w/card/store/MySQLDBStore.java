package com.w.card.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.w.card.domain.Account;
import com.w.card.domain.Item;
import com.w.card.domain.User;

public class MySQLDBStore implements Store {

	public MySQLDBStore() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// ����һ����ӡ������
	private void printUi(String label1, String label2) {
		System.out.println("-----------------");
		System.out.println("����" + label1);
		System.out.println("-----------------");
		System.out.println(label1 + "\t" + label2);
		System.out.println("-----------------");
	}

	// ����һ���������������ݿ⣬�������
	private ResultSet getResultSet(Connection conn, String sql) throws Exception {
		Connection myConn = conn;
		Statement statement = myConn.createStatement();
		String mySQL = sql;
		ResultSet rs = statement.executeQuery(mySQL);
		return rs;
	}

	// ����һ��remove�����������ж���Ŀ�Ƿ���ڣ���������ھͷ��أ����ھ�ɾ����
	private boolean executeIfDelete(final String countSql, final String deleteSql) throws Exception {
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count <= 0) {
			System.out.println("������");
			return false;
		}
		int ifDelete = myConn.createStatement().executeUpdate(deleteSql);
		System.out.println("��ɾ��");
		myRS.close();
		myConn.close();
		return ifDelete == 1;
	}

	// ����һ��add�����������ж���Ŀ�Ƿ���ڣ��粻���ھʹ���һ����
	private boolean executeIfAbsent(final String countSql, final String updateSql) throws Exception {
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count > 0) {
			System.out.println("�Ѵ���");
			return false;
		}
		int ifInsert = myConn.createStatement().executeUpdate(updateSql);
		myRS.close();
		myConn.close();
		return ifInsert == 1;
	}

	// ����һ��getBalance����������������
	private Float getBalance(final String countSql, final String amountSql) throws Exception {
		Float balance = 0f;
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count == 0) {
			System.out.println("�˺Ų����ڣ�");
			myRS.close();
			myConn.close();
			return balance;
		}
		myRS = this.getResultSet(myConn, amountSql);
		while (myRS.next()) {
			balance = myRS.getFloat("balance");
		}
		System.out.println("��" + balance);
		myRS.close();
		myConn.close();
		return balance;

	}

	@Override
	public List<User> listUsers() throws Exception {

		this.printUi("�û�", "����");
		List<User> luser = new ArrayList<>();
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, "select * from User");
		while (myRS.next()) {

			String userName = myRS.getString("name");
			int userID = myRS.getInt("id");
			User user = new User();
			user.setId(userID);
			user.setName(userName);
			luser.add(user);
			System.out.println(userID + "\t" + userName);

		}
		myRS.close();
		myConn.close();
		return luser;

	}

	@Override
	public Boolean addUser(String userName) throws Exception {
		return this.executeIfAbsent("SELECT count(id) as cnt FROM User WHERE name = +'" + userName + "'",
				"INSERT INTO User (name) VALUES ('" + userName + "')");
	}

	@Override
	public Boolean removeUser(String userName) throws Exception {
		return this.executeIfDelete("select count(name) as cnt from User where name = '" + userName + "'",
				"delete from User where name ='" + userName + "'");
	}

	@Override
	public List<Account> listAccounts(String userName) throws Exception {
		// �г�ָ���û��������˺š�
		Connection myConn = this.createConnection();
		this.printUi("�˻�", "�˺�");
		List<Account> laccount = new ArrayList<>();
		ResultSet myRS = this.getResultSet(myConn, "select id  From User WHERE name = '" + userName + "'");
		if (!myRS.next()) {
			System.out.println("�û�: " + userName + "������");
		}
		int userId = myRS.getInt("id");
		myRS = this.getResultSet(myConn, "select id,number From Account WHERE user_id = '" + userId + "'");
		while (myRS.next()) {
			String accountNumber = myRS.getString("number");
			int accountID = myRS.getInt("id");
			Account account = new Account();
			account.setId(accountID);
			account.setNumber(accountNumber);
			account.setUserId(userId);
			laccount.add(account);
			System.out.println(accountID + "\t" + accountNumber);

		}
		myRS.close();
		myConn.close();
		return laccount;

	}

	@Override
	public Boolean addAccount(String userName, String accountNumber) throws Exception {
		// ��Ҫ�ж����ݿ�����û�����User,���û�о��ȴ���һ��User,Ȼ���ٴ����˻���
		this.addUser(userName);

		Connection myConn = this.createConnection();
		int userId = -1;
		ResultSet userIdRS = this.getResultSet(myConn, "select id from User where name ='" + userName + "'");
		while (userIdRS.next()) {
			userId = userIdRS.getInt("id");
		}
		System.out.println("�˻�:" + accountNumber + "�Ѵ���");
		userIdRS.close();
		myConn.close();

		if (userId != -1) {
			return this.executeIfAbsent("SELECT count(*) as cnt FROM Account WHERE number = '" + accountNumber + "'",
					"INSERT INTO Account (number, user_Id) VALUES ('" + accountNumber + "', " + userId + ")");
		}

		return false;
	}

	@Override
	public Boolean removeAccount(String accountNumber) throws Exception {
		return this.executeIfDelete("select count(number) as cnt from Account where number = '" + accountNumber + "'",
				"delete from Account where number ='" + accountNumber + "'");
	}

	@Override
	public List<Item> listItems(String accountNumber) throws Exception {
		// �ж���û������˺ţ��о���ʾItem,û�оʹ�ӡһ����Ϣ��
		Connection myConn = this.createConnection();
		ResultSet rs = this.getResultSet(myConn,
				"select count(id) as cnt from Account where number ='" + accountNumber + "'");
		rs.next();
		int sameAccount = rs.getInt("cnt");
		if (sameAccount == 0) {
			System.out.println("���˺Ų�����");
		}
		System.out.println("-----------------");
		System.out.println("������ϸ");
		System.out.println("-----------------");
		System.out.println("�˺�" + "\t" + "��ȡ" + "\t" + "ʱ��");
		System.out.println("-----------------");
		ResultSet accountIdRS = this.getResultSet(myConn,
				"select * from Account where number ='" + accountNumber + "'");
		int accountId = -1;
		while (accountIdRS.next()) {
			accountId = accountIdRS.getInt("id");
		}
		List<Item> litem = new ArrayList<>();
		ResultSet myRS = this.getResultSet(myConn,
				"select id,createdAt,amount from Item where account_id = " + accountId);
		while (myRS.next()) {
			Item item = new Item();
			int itemId = myRS.getInt("id");
			item.setId(itemId);
			item.setAccountId(accountId);
			Float amount = myRS.getFloat("amount");
			item.setAmount(amount);
			Date createdAt = myRS.getDate("createdAt");
			item.setCreatedAt(createdAt);
			litem.add(item);
			System.out.println(accountNumber + "\t" + amount + "\t" + createdAt);
		}
		myRS.close();
		myConn.close();
		return litem;

	}

	@Override
	public Boolean addItem(String accountNumber, float amount) throws Exception {
		// ���жϸ��˺��Ƿ���ڣ���������ڣ��ͱ������ڣ��ʹ�ȡ�ض��Ľ�
		Connection myConn = this.createConnection();
		ResultSet rs = this.getResultSet(myConn,
				"select count(id) as cnt from Account where number ='" + accountNumber + "'");
		rs.next();
		int sameAccount = rs.getInt("cnt");
		if (sameAccount == 0) {
			System.out.println("���˺Ų�����");
			rs.close();
			myConn.close();
			return false;
		}
		ResultSet accountIdRS = this.getResultSet(myConn,
				"select * from Account where number ='" + accountNumber + "'");
		int accountId = -1;
		accountIdRS.next();
		accountId = accountIdRS.getInt("id");
		Item item = new Item();
		Date d = new Date();
		Timestamp ts = new Timestamp(d.getTime());
		String addItem = "INSERT INTO Item (account_id,amount,createdAt) VALUES (" + accountId + "," + amount + ",'"
				+ ts + "')";
		myConn.createStatement().executeUpdate(addItem);
		System.out.println("�˺ţ�" + accountNumber + "��ȡ�ˣ�" + amount);
		accountIdRS.close();
		myConn.close();
		return true;
	}

	@Override
	public Boolean removeItem(int itemID) throws Exception {
		return this.executeIfDelete("select count(id) as cnt from Item where id = " + itemID,
				"delete from Item where id =" + itemID);
	}

	@Override
	public Float accountBalance(int accountID) throws Exception {

		return this.getBalance("select count(id) as cnt from Account where id =" + accountID,
				"SELECT sum(amount) as balance FROM Item WHERE account_id =" + accountID);
	}

	@Override
	public Float userBalance(int userID) throws Exception {
		Float userBalance = 0f;
		Float accountBalance = 0f;
		Float f;
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, " select id from Account where user_id = " + userID);
		int accountId = -1;
		while (myRS.next()) {
			accountId = myRS.getInt("id");
			accountBalance = this.accountBalance(accountId);
			f = accountBalance;
			userBalance += accountBalance;
		}
		myRS.close();
		myConn.close();

	}

	private Connection createConnection() {
		try {
			return DriverManager.getConnection("jdbc:mysql://192.168.0.115:3306/MyCard", "root", "root");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
