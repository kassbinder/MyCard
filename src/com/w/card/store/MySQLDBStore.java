package com.w.card.store;

import java.sql.Connection;
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

	// ����һ��list������
	private ResultSet list(final String countSql, final String executeSql, Connection conn) throws Exception {
		if (countSql == null) {
			ResultSet rs = this.getResultSet(conn, executeSql);
			return rs;
		}
		ResultSet ifExistRs = this.getResultSet(conn, countSql);
		ifExistRs.next();
		if (ifExistRs.getInt("cnt") == 0) {
			System.out.println("������");
			return null;
		}
		ResultSet rs = this.getResultSet(conn, executeSql);
		return rs;

	}

	// ����һ����ӡ����������list�����С�
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

	// ����һ��remove����������3��sql��䡣��һ���ж���Ŀ�Ƿ���ڣ���������ھͷ��أ�
	// ���ڵĻ���ִ�еڶ���������ж�����Ƿ�Ϊ0������ͷ��أ����ɾ����
	private boolean remove(final String countSql, final String countSql2, final String... deleteSql) throws Exception {

		Float balance = this.getBalance(countSql, countSql2);
		if (balance != 0) {
			System.out.println("��Ϊ�㣬����ɾ��");
			return false;
		}
		Connection myConn = this.createConnection();
		int delete = 0;
		for (int i = 0; i < deleteSql.length; i++) {
			delete += myConn.createStatement().executeUpdate(deleteSql[i]);
		}
		myConn.close();
		return delete >= 0;

	}

	// ����һ��add��������������sql��䡣��һ�������ж���Ŀ�Ƿ���ڣ�������ھͷ��أ������ھ�ִ�еڶ�����䡣
	private boolean executeIfAbsent(final String countSql, final String... updateSql) throws Exception {
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count > 0) {
			System.out.println("�Ѵ���");
			return false;
		}
		int excuted = 0;
		for (int i = 0; i < updateSql.length; i++) {
			excuted += myConn.createStatement().executeUpdate(updateSql[i]);
		}
		myRS.close();
		myConn.close();
		return excuted >= 0;
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
		Connection myConn = this.createConnection();
		this.printUi("�û�", "����");
		List<User> luser = new ArrayList<>();
		ResultSet myRS = this.list(null, "select * from User", myConn);
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
		Connection myConn = this.createConnection();
		ResultSet rs = this.getResultSet(myConn, "SELECT id FROM User WHERE name = '" + userName + "'");
		rs.next();
		int userId = rs.getInt("id");
		return this.remove(" select count(id) as cnt from User where name = '" + userName + "'",
				"SELECT sum(amount) as balance FROM Item,Account,User WHERE name = '" + userName
						+ "' and User.id = Account.user_id and Item.account_id = Account.id",
				"DELETE FROM User WHERE name = '" + userName + "'",
				"DELETE FROM Account WHERE Account.user_id =" + userId,
				"DELETE FROM Item WHERE account_id = (SELECT Account.id FROM Account, User WHERE Account.user_id = User.id AND User.name = '"
						+ userName + "')");

	}

	@Override
	public List<Account> listAccounts(String userName) throws Exception {
		// �г�ָ���û��������˺š�
		Connection myConn = this.createConnection();
		this.printUi("�˻�", "�˺�");
		List<Account> laccount = new ArrayList<>();
		ResultSet myRs = this.list("select count(id) as cnt  From User WHERE name = '" + userName + "'",
				"select id From User WHERE name = '" + userName + "'", myConn);
		myRs.next();
		int userId = myRs.getInt("id");
		ResultSet accountRs = this.getResultSet(myConn, "SELECT id,number FROM Account WHERE user_id =" + userId);
		while (accountRs.next()) {
			String accountNumber = accountRs.getString("number");
			int accountID = accountRs.getInt("id");
			Account account = new Account();
			account.setId(accountID);
			account.setNumber(accountNumber);
			account.setUserId(userId);
			laccount.add(account);
			System.out.println(accountID + "\t" + accountNumber);

		}
		accountRs.close();
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
	public Boolean removeAccount(int accountID) throws Exception {
		return this.remove("select count(id) as cnt from Account where id =" + accountID,
				"SELECT sum(amount) as balance FROM Item WHERE account_id =" + accountID,
				"DELETE FROM Account WHERE Account.id =" + accountID,
				"DELETE FROM Item WHERE Item.account_id =" + accountID);
	}

	@Override
	public Boolean transfer(String accountNumber1, String accountNumber2, Float amount) throws Exception {
		// TODO Auto-generated method stub
		Connection myConn = this.createConnection();
		ResultSet rs = this.getResultSet(myConn, "select id from Account where number = '" + accountNumber1 + "'");
		rs.next();
		int account1Id = rs.getInt("id");
		Float account1Balance = this.accountBalance(account1Id);
		if (amount > 0 && amount <= account1Balance) {
			this.addItem(accountNumber1, -(amount));
			this.addItem(accountNumber2, amount);
			System.out.println("ת�˳ɹ���");
			rs.close();
			myConn.close();
			return true;
		}
		System.out.println("�����˻����㣬��ѡ�����˻�");
		rs.close();
		myConn.close();
		return false;
	}

	@Override
	public List<Item> listItems(String accountNumber) throws Exception {
		// �ж���û������˺ţ��о���ʾItem,û�оʹ�ӡһ����Ϣ��
		Connection myConn = this.createConnection();
		this.printUi("��ȡ", "ʱ��");
		ResultSet myRs = this.list("select count(id) as cnt from Account where number ='" + accountNumber + "'",
				"select * from Account where number ='" + accountNumber + "'", myConn);
		int accountId = -1;
		while (myRs.next()) {
			accountId = myRs.getInt("id");
		}
		List<Item> litem = new ArrayList<>();
		ResultSet itemRS = this.getResultSet(myConn,
				"select id,createdAt,amount from Item where account_id = " + accountId);
		while (itemRS.next()) {
			Item item = new Item();
			int itemId = itemRS.getInt("id");
			item.setId(itemId);
			item.setAccountId(accountId);
			Float amount = itemRS.getFloat("amount");
			item.setAmount(amount);
			Date createdAt = itemRS.getDate("createdAt");
			item.setCreatedAt(createdAt);
			litem.add(item);
			System.out.println(amount + "\t" + createdAt);
		}
		itemRS.close();
		myRs.close();
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
				"select id from Account where number ='" + accountNumber + "'");
		int accountId = -1;
		accountIdRS.next();
		accountId = accountIdRS.getInt("id");
		Date d = new Date();
		Timestamp ts = new Timestamp(d.getTime());
		String addItem = "INSERT INTO Item (account_id,amount,createdAt) VALUES (" + accountId + "," + amount + ",'"
				+ ts + "')";
		int ifAdd = myConn.createStatement().executeUpdate(addItem);
		System.out.println("�˺ţ�" + accountNumber + "��ȡ�ˣ�" + amount);
		accountIdRS.close();
		myConn.close();
		return ifAdd == 1;
	}

	@Override
	public Float accountBalance(int accountID) throws Exception {

		return this.getBalance("select count(id) as cnt from Account where id =" + accountID,
				"SELECT sum(amount) as balance FROM Item WHERE account_id =" + accountID);
	}

	@Override
	public Float userBalance(String userName) throws Exception {
		return this.getBalance(" select count(id) as cnt from User where name = '" + userName + "'",
				"SELECT sum(amount) as balance FROM Item,Account,User WHERE name = '" + userName
						+ "' and Account.user_id = User.id and Item.account_id = Account.id");

	}

	private Connection createConnection() {
		return DBConnection.createConnection();
	}

}
