package com.w.card.store;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.w.card.domain.Account;
import com.w.card.domain.Item;
import com.w.card.domain.User;

public class MySQLDBStore implements Store {

	// 定义一个打印方法。
	private void printUi(String label1, String label2) {
		System.out.println("-----------------");
		System.out.println("所有" + label1);
		System.out.println("-----------------");
		System.out.println(label1 + "\t" + label2);
		System.out.println("-----------------");
	}

	// 定义一个方法来连接数据库，接收命令。
	private ResultSet getResultSet(Connection conn, String sql) throws Exception {

		Connection myConn = conn;
		Statement statement = myConn.createStatement();
		String mySQL = sql;
		ResultSet rs = statement.executeQuery(mySQL);
		return rs;
	}

	// 定义一个remove方法。先判断条目是否存在，如果不存在就返回，存在的话看余额是否为0，如果是零，就删除，非零就返回。
	private boolean remove(final String countSql, final String deleteSql, String userName) throws Exception {
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count <= 0) {
			System.out.println("不存在");
			return false;
		}

		Float userBalance = this.userBalance(userName);
		if (userBalance == 0) {
			int ifDelete = myConn.createStatement().executeUpdate(deleteSql);
			System.out.println("已删除");
			myRS.close();
			myConn.close();
			return ifDelete == 1;
		}
		System.out.println("余额不为0，不能删除");
		return false;
	}

	// 定义一个add方法。用来判断条目是否存在，如果存在就返回，不存在就创建一个。
	private boolean executeIfAbsent(final String countSql, final String updateSql) throws Exception {
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count > 0) {
			System.out.println("已存在");
			return false;
		}
		int ifInsert = myConn.createStatement().executeUpdate(updateSql);
		myRS.close();
		myConn.close();
		return ifInsert == 1;
	}

	// 定义一个getBalance方法。用来计算余额。
	private Float getBalance(final String countSql, final String amountSql) throws Exception {
		Float balance = 0f;
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count == 0) {
			System.out.println("账号不存在！");
			myRS.close();
			myConn.close();
			return balance;
		}
		myRS = this.getResultSet(myConn, amountSql);
		while (myRS.next()) {
			balance = myRS.getFloat("balance");
		}
		System.out.println("余额：" + balance);
		myRS.close();
		myConn.close();
		return balance;

	}

	@Override
	public List<User> listUsers() throws Exception {

		this.printUi("用户", "姓名");
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
		return this.remove("select count(name) as cnt from User where name = '" + userName + "'",
				"delete from User where name ='" + userName + "'", userName);
	}

	@Override
	public List<Account> listAccounts(String userName) throws Exception {
		// 列出指定用户的所有账号。
		Connection myConn = this.createConnection();
		this.printUi("账户", "账号");
		List<Account> laccount = new ArrayList<>();
		ResultSet rs = this.getResultSet(myConn, "select count(id) as cnt  From User WHERE name = '" + userName + "'");
		rs.next();
		if (rs.getInt("cnt") == 0) {
			System.out.println("用户: " + userName + "不存在");
		}
		ResultSet userIdRs = this.getResultSet(myConn, "select id From User WHERE name = '" + userName + "'");
		userIdRs.next();
		int userId = userIdRs.getInt("id");
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
			return this.executeIfAbsent("SELECT count(*) as cnt FROM Account WHERE number = '" + accountNumber + "'",
					"INSERT INTO Account (number, user_Id) VALUES ('" + accountNumber + "', " + userId + ")");
		}

		return false;
	}

	@Override
	public Boolean removeAccount(String accountNumber, String userName) throws Exception {
		return this.remove("select count(number) as cnt from Account where number = '" + accountNumber + "'",
				"delete from Account where number ='" + accountNumber + "'", userName);
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
			System.out.println("转账成功！");
			rs.close();
			myConn.close();
			return true;
		}
		System.out.println("您的账户余额不足，请选择别的账户");
		rs.close();
		myConn.close();
		return false;
	}

	@Override
	public List<Item> listItems(String accountNumber) throws Exception {
		// 判断有没有这个账号，有就显示Item,没有就打印一条信息。
		Connection myConn = this.createConnection();
		ResultSet rs = this.getResultSet(myConn,
				"select count(id) as cnt from Account where number ='" + accountNumber + "'");
		rs.next();
		int sameAccount = rs.getInt("cnt");
		if (sameAccount == 0) {
			System.out.println("该账号不存在");
		}
		System.out.println("-----------------");
		System.out.println("所有明细");
		System.out.println("-----------------");
		System.out.println("账号" + "\t" + "存取" + "\t" + "时间");
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
		// 先判断该账号是否存在，如果不存在，就报错。存在，就存取特定的金额。
		Connection myConn = this.createConnection();
		ResultSet rs = this.getResultSet(myConn,
				"select count(id) as cnt from Account where number ='" + accountNumber + "'");
		rs.next();
		int sameAccount = rs.getInt("cnt");
		if (sameAccount == 0) {
			System.out.println("该账号不存在");
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
		System.out.println("账号：" + accountNumber + "存取了：" + amount);
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
						+ "' and User.id = Account.user_id and Item.account_id = Account.id");

	}

	private Connection createConnection() {
		return DBConnection.createConnection();
	}

}
