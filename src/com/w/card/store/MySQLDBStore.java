package com.w.card.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		myConn.createStatement().executeUpdate(deleteSql);
		System.out.println("��ɾ��");
		myRS.close();
		myConn.close();
		return true;
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
		myConn.createStatement().executeUpdate(updateSql);
		myRS.close();
		myConn.close();
		return true;
	}

	// ����һ��getBalance����������������
	private Float getBalance(final String countSql, final String amountSql) throws Exception {
		Float balance = 0f;
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		int count = myRS.getInt("cnt");
		if (count < 1) {
			System.out.println("��" + balance);
			myRS.close();
			myConn.close();
			return balance;
		}
		myRS = this.getResultSet(myConn, amountSql);
		while (myRS.next()) {
			balance += myRS.getFloat("balance");
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
		ResultSet myRS = this.getResultSet(myConn, "select * from User");
		while (myRS.next()) {
			String accountNumber = myRS.getString("number");
			int accountID = myRS.getInt("id");
			Account account = new Account(accountNumber);
			account.setId(accountID);
			account.setNumber(accountNumber);
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
			return this.executeIfAbsent("SELECT count(*) as cnt FROM Account WHERE number = +'" + accountNumber + "'",
					"INSERT INTO ACCOUNT (number, user_Id) VALUES ('" + accountNumber + "', " + userId + ")");
		}

		return false;
	}

	@Override
	public Boolean removeAccount(String accountNumber) throws Exception {
		return this.executeIfDelete("select count(number) as cnt from Account where name = '" + accountNumber + "'",
				"delete from Account where number ='" + accountNumber + "'");
	}

	@Override
	public List<Item> listItems(String accountNumber) throws Exception {
		// �ж���û������˺ţ��о���ʾItem,û�оʹ�ӡһ����Ϣ��
		Connection myConn = this.createConnection();
		ResultSet accountIdRS = this.getResultSet(myConn, "select id from User where name ='" + accountNumber + "'");
		int accountId = -1;
		while (accountIdRS.next()) {
			accountId = accountIdRS.getInt("id");
		}

		System.out.println("-----------------");
		System.out.println("������ϸ");
		System.out.println("-----------------");
		System.out.println("�˺�" + "\t" + "��ȡ" + "\t" + "ʱ��");
		System.out.println("-----------------");
		List<Item> litem = new ArrayList<>();

		ResultSet myRS = this.getResultSet(myConn, "select * from Account where account_id = '" + accountId + "'");
		while (myRS.next()) {

			int accountNm = accountId;
			Item item = new Item();
			Float itemAmount = item.getAmount();
			Date itemCreatedAt = item.getCreatedAt();
			litem.add(item);
			System.out.println(accountNm + "\t" + itemAmount + "\t" + itemCreatedAt);

		}
		myRS.close();
		myConn.close();
		return litem;

	}

	@Override
	public Boolean addItem(String accountNumber, float amount) throws Exception {
		// ���жϸ��˺��Ƿ���ڣ���������ڣ��ͱ������ڣ��ʹ�ȡ�ض��Ľ�
		Connection myConn = this.createConnection();
		ResultSet accountRS = this.getResultSet(myConn, "select * from Account where number ='" + accountNumber + "'");
		if (!accountRS.next()) {
			System.out.println("��ȡʧ�ܣ����˺Ų����ڡ�");
			return false;
		}
		int accountId = -1;
		while (accountRS.next()) {
			accountId = accountRS.getInt("id");
		}
		String sql = "insert into Item (account_id,amount) values (" + accountId + "," + amount + ")";
		myConn.createStatement().executeUpdate(sql);
		System.out.println("�˺ţ�" + accountNumber + "�����ˣ�" + amount);
		return true;
	}

	@Override
	public Boolean removeItem(int itemID) throws Exception {
		return this.executeIfDelete("select count(id) as cnt from Item where item_id = '" + itemID + "'",
				"delete from Item where item_id ='" + itemID + "'");
	}

	@Override
	public Float accountBalance(int accountID) throws Exception {
		// Float balance = 0f;
		// Connection myConn = this.createConnection();
		// ResultSet myRS = this.getResultSet(myConn,
		// "select count(id) as cnt from Account where id ='" + accountID +
		// "'");
		// int count = myRS.getInt("cnt");
		// if (count > 0) {
		// myRS = this.getResultSet(myConn, "SELECT sum(amount) as balance FROM
		// Item WHERE account_id =" + accountID);
		// while (myRS.next()) {
		//
		// balance += myRS.getFloat("balance");
		// }
		// System.out.println("��" + balance);
		// return balance;
		// }
		// System.out.println("��" + balance);
		// return balance;
		return this.getBalance("select count(id) as cnt from Account where id ='" + accountID + "'",
				"SELECT sum(amount) as balance FROM Item WHERE account_id =" + accountID);
	}

	@Override
	public Float userBalance(int userID) throws Exception {
		// Float balance = 0f;
		// Connection myConn = this.createConnection();
		// ResultSet myRS = this.getResultSet(myConn,
		// "select count(id) as cnt from Account where user_id ='" + userID +
		// "'");
		// int count = myRS.getInt("cnt");
		// if (count > 0) {
		// myRS = this.getResultSet(myConn, " select id from Account where
		// user_id = " + userID);
		// while (myRS.next()) {
		// int accountId = myRS.getInt("id");
		// myRS = this.getResultSet(myConn,
		// "SELECT sum(amount) as balance FROM Item WHERE account_id =" +
		// accountId);
		// balance += myRS.getFloat("balance");
		// }
		// System.out.println("��" + balance);
		// return balance;
		// }
		// System.out.println("���: " + balance);
		// myRS.close();
		// myConn.close();
		// return balance;
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, " select id from Account where user_id = " + userID);
		int accountId = -1;
		while (myRS.next()) {
			accountId = myRS.getInt("id");
		}
		myRS.close();
		myConn.close();
		return this.getBalance("select count(id) as cnt from Account where user_id ='" + userID + "'",
				"SELECT sum(amount) as balance FROM Item WHERE account_id =" + accountId);

	}

	private Connection createConnection() {
		try {
			return DriverManager.getConnection("jdbc:mysql://192.168.0.115:3306/MyCard", "root", "root");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
