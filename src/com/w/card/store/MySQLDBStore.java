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

	// 定义一个方法来连接数据库，接收命令。
	private ResultSet getResultSet(Connection conn, String sql) throws Exception {
		Connection myConn = conn;
		Statement statement = myConn.createStatement();
		String mySQL = sql;
		ResultSet rs = statement.executeQuery(mySQL);
		return rs;
	}

	// 定义一个list方法。

	// 定义一个方法。

	private boolean executeIfAbsent(final String countSql, final String updateSql) throws Exception {
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count > 0) {
			System.out.println("已存在");
			return false;
		}
		myConn.createStatement().executeUpdate(updateSql);
		myRS.close();
		myConn.close();
		return true;
	}

	@Override
	public List<User> listUsers() throws Exception {
		// 列出所有用户和姓名。
		Connection myConn = this.createConnection();

		System.out.println("-----------------");
		System.out.println("所有用户");
		System.out.println("-----------------");
		System.out.println(" 用户" + "\t" + " 姓名");
		System.out.println("-----------------");
		List<User> luser = new ArrayList<>();

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
		return this.executeIfAbsent("SELECT count(*) as cnt FROM User WHERE name = +'" + userName + ",",
				"INSERT INTO User (name) VALUES ('" + userName + "')");
	}

	@Override
	public Boolean removeUser(String userName) throws Exception {
		// 遍历数据库查找有没有这个user,没有就结束方法，有就删除。
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn,
				"select count(name) as cnt from User where name = '" + userName + "'");
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count > 0) {
			String mySQL = "delete from User where name ='" + userName + "'";
			myConn.createStatement().executeUpdate(mySQL);
			System.out.println("用户：" + userName + "已删除");
			myRS.close();
			myConn.close();
			return true;
		}
		System.out.println("用户：" + userName + "不存在");
		myRS.close();
		myConn.close();
		return false;
	}

	@Override
	public List<Account> listAccounts(String userName) throws Exception {
		// 列出指定用户的所有账号。
		Connection myConn = this.createConnection();

		System.out.println("-----------------");
		System.out.println("所有账户");
		System.out.println("-----------------");
		System.out.println(" 账户" + "\t" + " 账号");
		System.out.println("-----------------");
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
		// 先要判断数据库中有没有这个User,如果没有就先创建一个User,然后再创建账户。
		this.addUser(userName);

		Connection myConn = this.createConnection();
		int userId = -1;
		ResultSet userIdRS = this.getResultSet(myConn, "select id from User where name ='" + userName + "'");
		while (userIdRS.next()) {
			userId = userIdRS.getInt("id");
		}
		System.out.println("账户:" + accountNumber + "已创建");
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
		// 在数据库查找有没有这个账号，没有就结束方法，有就删除。

		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn,
				"select count(number) as cnt from Account where name = '" + accountNumber + "'");
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count > 0) {
			String mySQL = "delete from Account where number ='" + accountNumber + "'";
			myConn.createStatement().executeUpdate(mySQL);
			System.out.println("账户：" + accountNumber + "已删除");
			myRS.close();
			myConn.close();
			return true;
		}
		System.out.println("账户：" + accountNumber + "不存在");
		myRS.close();
		myConn.close();
		return false;
	}

	@Override
	public List<Item> listItems(String accountNumber) throws Exception {
		// 判断有没有这个账号，有就显示Item,没有就打印一条信息。
		Connection myConn = this.createConnection();
		ResultSet accountIdRS = this.getResultSet(myConn, "select id from User where name ='" + accountNumber + "'");
		int accountId = -1;
		while (accountIdRS.next()) {
			accountId = accountIdRS.getInt("id");
		}

		System.out.println("-----------------");
		System.out.println("所有明细");
		System.out.println("-----------------");
		System.out.println("账号" + "\t" + "存取" + "\t" + "时间");
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
		// 先判断该账号是否存在，如果不存在，就报错。存在，就存取特定的金额。
		Connection myConn = this.createConnection();
		ResultSet accountRS = this.getResultSet(myConn, "select * from Account where number ='" + accountNumber + "'");
		if (!accountRS.next()) {
			System.out.println("该账号不存在！");
			return false;
		}
		int accountId = -1;
		ResultSet accountIdRS = this.getResultSet(myConn,
				"select id from Account where number ='" + accountNumber + "'");
		while (accountIdRS.next()) {
			accountId = accountIdRS.getInt("id");
		}
		String sql = "insert into Item (account_id,amount) values (" + accountId + "," + amount + ")";
		myConn.createStatement().executeUpdate(sql);
		System.out.println("账号：" + accountNumber + "存入了：" + amount);
		return true;
	}

	@Override
	public Boolean removeItem(int itemID) throws Exception {
		// 判断这个Item是否存在，不存在就报错，存在就删除。

		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn,
				"select count(id) as cnt from Item where item_id = '" + itemID + "'");
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count <= 0) {
			System.out.println("不存在");
			myRS.close();
			myConn.close();
			return false;
		}
		String mySQL = "delete from Item where item_id ='" + itemID + "'";
		myConn.createStatement().executeUpdate(mySQL);
		System.out.println("已删除");
		myRS.close();
		myConn.close();
		return true;
	}

	@Override
	public Float accountBalance(int accountID) throws Exception {

		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn,
				"SELECT sum(amount) as balance FROM Item WHERE account_id =" + accountID);
		myRS.next();
		Float balance = myRS.getFloat("balance");
		System.out.println("账号：" + accountID + "-" + "余额：" + balance);
		return balance;
	}

	@Override
	public Float userBalance(int userID) throws Exception {
		List<Account> accounts = new ArrayList();

		Connection myConn = this.createConnection();
		int accountId = -1;
		ResultSet accountIdRS = this.getResultSet(myConn, "select * from Account where user_id ='" + userID + "'");
		while (accountIdRS.next()) {
			accountId = accountIdRS.getInt("user_id");
		}
		ResultSet myRS = this.getResultSet(myConn,
				"SELECT sum(amount) as balance FROM Item WHERE account_id =" + accountId);
		myRS.next();
		Float balance = myRS.getFloat("balance");
		System.out.println("账户：" + userID + "-" + "余额：" + balance);
		return balance;

	}

	private Connection createConnection() {
		try {
			return DriverManager.getConnection("jdbc:mysql://192.168.0.115:3306/MyCard", "root", "root");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
