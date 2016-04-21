package com.w.card.store;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBConnection {

	private static ComboPooledDataSource cpds;

	static {
		try {
			// Class.forName("com.mysql.jdbc.Driver");
			cpds = new ComboPooledDataSource();
			cpds.setDriverClass("com.mysql.jdbc.Driver");
			cpds.setJdbcUrl("jdbc:mysql://192.168.0.115:3306/MyCard");
			cpds.setUser("root");
			cpds.setPassword("root");
			cpds.setMinPoolSize(5);
			cpds.setAcquireIncrement(5);
			cpds.setMaxPoolSize(20);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Connection createConnection() {
		try {
			return cpds.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
