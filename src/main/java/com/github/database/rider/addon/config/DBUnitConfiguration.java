package com.github.database.rider.addon.config;

import javax.enterprise.context.ApplicationScoped;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 21/09/16.
 */
@ApplicationScoped
public class DBUnitConfiguration {

	private String url;

	private String user;

	private String password = "";

	private String driverClass;

	private List<String> tableNames;

	public DBUnitConfiguration set(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
		if (password == null) {
			this.password = "";
		}
		driverClass = resolveDriverClass();
		Connection connection = null;
		try {
			connection = createConnection();
			tableNames = resolveTableNames(connection);
		} catch (Exception e) {
			throw new RuntimeException(
					"Could get connection using current configuration, use 'DBUnit Setup' to configure JDBC connection. Error: "
							+ e.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return this;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getDriverClass() {
		return driverClass;
	}

	private String resolveDriverClass() {
		String name = "";
		try {
			name = DriverManager.getDriver(url).getClass().getName();
			return name;
		} catch (Exception e) {
			try {
				//yea it needs to be called twice! 
				name = DriverManager.getDriver(url).getClass().getName();
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.getLogger(getClass().getName()).log(Level.WARNING,
						"An exception occured while trying get table names.", ex);
				
			}
		}
		return name;
	}

	@Override
	public String toString() {
		return "Url: " + url + ", User: " + user + ", Driver class: " + driverClass;
	}

	public List<String> resolveTableNames(Connection connection) {
		List<String> tableNames = new ArrayList<String>();

		ResultSet result = null;
		try {
			DatabaseMetaData metaData = connection.getMetaData();

			result = metaData.getTables(null, null, "%", new String[] { "TABLE" });

			while (result.next()) {
				String name = result.getString("TABLE_NAME");
				tableNames.add(name);
			}
		} catch (SQLException ex) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING,
					"An exception occured while trying get table names.", ex);
		}
		return tableNames;
	}

	public List<String> getTableNames() {
		return tableNames;
	}

	public Connection createConnection() throws SQLException, ClassNotFoundException {
		if (driverClass != null && !"".equals(driverClass)) {
			Class.forName(driverClass);
		}
		if(url == null || "".equals(url.trim())){
			throw new RuntimeException("Use the 'setup' command to provide a valid database URL in order to use this plugin.");
		}
		return DriverManager.getConnection(url, user, password);
	}

}
