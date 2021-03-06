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

	// 定义一个list方法。
	private ResultSet list(final String countSql, final String executeSql, Connection conn) throws Exception {
		if (countSql == null) {
			ResultSet rs = this.getResultSet(conn, executeSql);
			return rs;
		}
		ResultSet ifExistRs = this.getResultSet(conn, countSql);
		ifExistRs.next();
		if (ifExistRs.getInt("cnt") == 0) {
			System.out.println("不存在");
			return null;
		}
		ResultSet rs = this.getResultSet(conn, executeSql);
		return rs;

	}

	// 定义一个打印方法。用于list方法中。
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

	// 定义一个remove方法。传入3条sql语句。第一条判断条目是否存在，如果不存在就返回；
	// 存在的话再执行第二条语句来判断余额是否为0，非零就返回，零就删除。
	private boolean remove(final String countSql, final String countSql2, final String... deleteSql) throws Exception {

		Float balance = this.getBalance(countSql, countSql2);
		if (balance != 0) {
			System.out.println("余额不为零，不能删除");
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

	// 定义一个add方法。传入两条sql语句。第一条用来判断条目是否存在，如果存在就返回，不存在就执行第二条语句。
	private boolean executeIfAbsent(final String countSql, final String... updateSql) throws Exception {
		Connection myConn = this.createConnection();
		ResultSet myRS = this.getResultSet(myConn, countSql);
		myRS.next();
		int count = myRS.getInt("cnt");
		if (count > 0) {
			System.out.println("已存在");
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

	// 定义一个getBalance方法。用来计算余额。
	private Float getBalance(final String countSql, final String amountSql) throws Exception {
		Float balance = 0f;
		Connection myConn = this.createConnection();
		ResultSet rs = this.list(countSql, amountSql, myConn);
		while (rs.next()) {
			balance = rs.getFloat("balance");
		}
		System.out.println("余额：" + balance);
		rs.close();
		myConn.close();
		return balance;

	}

	@Override
	public List<User> listUsers() throws Exception {
		Connection myConn = this.createConnection();
		this.printUi("用户", "姓名");
		List<User> luser = new ArrayList<>();
		ResultSet myRS = this.list(null, "select * from User", myConn);
		if (myRS == null) {
			System.out.println("当前没有用户！");
			myRS.close();
			myConn.close();
			return null;
		}
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
		// 列出指定用户的所有账号。
		Connection myConn = this.createConnection();
		this.printUi("账户", "账号");
		List<Account> laccount = new ArrayList<>();
		ResultSet myRs = this.list("select count(id) as cnt  From User WHERE name = '" + userName + "'",
				"SELECT User.id ,Account.id,Account.number FROM Account,User WHERE User.name = '" + userName
						+ "' and Account.user_id = User.id",
				myConn);
		if (myRs == null) {
			System.out.println("该用户尚未有账户！");
			myRs.close();
			myConn.close();
			return null;
		}
		while (myRs.next()) {
			String accountNumber = myRs.getString("Account.number");
			int userId = myRs.getInt("User.id");
			int accountID = myRs.getInt("Account.id");
			Account account = new Account();
			account.setId(accountID);
			account.setNumber(accountNumber);
			account.setUserId(userId);
			laccount.add(account);
			System.out.println(accountID + "\t" + accountNumber);

		}
		myRs.close();
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
		this.printUi("存取", "时间");
		ResultSet myRs = this.list("select count(id) as cnt from Account where number ='" + accountNumber + "'",
				"SELECT Account.id,Item.id,Item.createdAt,Item.amount FROM Account,Item WHERE Account.number = '"
						+ accountNumber + "' AND Item.account_id = Account.id",
				myConn);
		if (myRs == null) {
			System.out.println("该账户不存在！");
			myRs.close();
			myConn.close();
			return null;
		}
		List<Item> litem = new ArrayList<>();
		while (myRs.next()) {
			int accountId = myRs.getInt("Account.id");
			Item item = new Item();
			int itemId = myRs.getInt("Item.id");
			item.setId(itemId);
			item.setAccountId(accountId);
			Float amount = myRs.getFloat("Item.amount");
			item.setAmount(amount);
			Date createdAt = myRs.getDate("Item.createdAt");
			item.setCreatedAt(createdAt);
			litem.add(item);
			System.out.println(amount + "\t" + createdAt);
		}
		myRs.close();
		myRs.close();
		myConn.close();
		return litem;

	}

	@Override
	public Boolean addItem(String accountNumber, float amount) throws Exception {
		// 先判断该账号是否存在，如果不存在，就报错。存在，就存取特定的金额。
		Connection myConn = this.createConnection();
		ResultSet rs = this.list("select count(id) as cnt from Account where number ='" + accountNumber + "'",
				"select id from Account where number ='" + accountNumber + "'", myConn);
		int accountId = -1;
		rs.next();
		accountId = rs.getInt("id");
		Date d = new Date();
		Timestamp ts = new Timestamp(d.getTime());
		String addItem = "INSERT INTO Item (account_id,amount,createdAt) VALUES (" + accountId + "," + amount + ",'"
				+ ts + "')";
		int ifAdd = myConn.createStatement().executeUpdate(addItem);
		System.out.println("账号：" + accountNumber + "存取了：" + amount);
		rs.close();
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
