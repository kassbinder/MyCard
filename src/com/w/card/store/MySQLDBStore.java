package com.w.card.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

	@Override
	public List<User> listUsers() throws Exception {

		Connection conn = this.createConnection();
		Statement statement = conn.createStatement();
		String mySQL = "select * from User";
		ResultSet rs = statement.executeQuery(mySQL);
		System.out.println("-----------------");
		System.out.println("�����û�");
		System.out.println("-----------------");
		System.out.println(" �û�" + "\t" + " ����");
		System.out.println("-----------------");
		List<User> luser = new ArrayList<>();

		while (rs.next()) {

			String userName = rs.getString("name");
			int userID = rs.getInt("id");
			User user = new User();
			user.setId(userID);
			user.setName(userName);
			luser.add(user);
			System.out.println(userID + "\t" + userName);

		}
		rs.close();
		conn.close();

		return luser;
	}

	@Override
	public Boolean addUser(String userName) throws Exception {
		// �ж����ݿ�����û�����user,���û�о������ݿⴴ��һ����
		Connection conn = this.createConnection();
		Statement statement = conn.createStatement();
		String mySQL = "select count(name) as cnt from User where name = '" + userName + "'";
		ResultSet rs = statement.executeQuery(mySQL);
		rs.next();
		int count = rs.getInt("cnt");
		if (count > 0) {
			System.out.println("�û��Ѵ���");
			return false;
		}
		mySQL = "insert into User (name) values ('" + userName + "')";
		statement.executeUpdate(mySQL);
		System.out.println("�û�" + userName + "�Ѵ���");
		return true;
	}

	@Override
	public Boolean removeUser(String userName) throws Exception {
		// �������ݿ������û�����user,û�оͽ����������о�ɾ����
		Connection conn = this.createConnection();
		Statement statement = conn.createStatement();
		String mySQL = "select count(name) as cnt from User where name = '" + userName + "'";
		ResultSet rs = statement.executeQuery("'" + mySQL + "'");
		rs.next();
		int count = rs.getInt("cnt");
		if (count == 0) {
			System.out.println(userName + "���û�������");
			return false;
		}
		return null;
	}

	@Override
	public List<Account> listAccounts(String userName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean addAccount(String userName, String accountNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean removeAccount(String accountNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Item> listItems(String accountNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean addItem(String accountNumber, float amount) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean removeItem(int itemID) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float accountBalance(int accountID) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float userBalance(int userID) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private Connection createConnection() {
		try {
			return DriverManager.getConnection("jdbc:mysql://192.168.0.115:3306/MyCard", "root", "root");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
