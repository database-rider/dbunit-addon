package com.github.database.rider.addon.config;

import javax.inject.Singleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 21/09/16.
 */
@Singleton
public class DBUnitConfiguration {

    private String url;

    private String user;

    private String password = "";

    private String driverClass;

    private List<String> tableNames;

    private Connection connection;

    public DBUnitConfiguration set(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        if (password == null) {
            this.password = "";
        }
        driverClass = resolveDriverClass();
        try {
            if(connection != null && !connection.isClosed()){
                connection.close();
            }
            connection = createConnection();
        } catch (Exception e) {
            throw new RuntimeException("Could get connection using current configuration, use 'DBUnit Setup' to configure JDBC connection. Error: "+e.getMessage());
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
                name = DriverManager.getDriver(url).getClass().getName();
            } catch (Exception ex) {
            }
        }
        return name;
    }

    @Override
    public String toString() {
        return "Url: " + url + ", User: " + user + ", Driver class: " + driverClass;
    }


    public List<String> getTableNames(Connection connection) {
        if (tableNames == null) {
            tableNames = new ArrayList<String>();

            ResultSet result = null;
            try {
                DatabaseMetaData metaData = connection.getMetaData();

                result = metaData.getTables(null, null, "%", new String[]{"TABLE"});

                while (result.next()) {
                    String schema = resolveSchema(result);
                    String name = result.getString("TABLE_NAME");
                    tableNames.add(schema != null ? schema + "." + name : name);
                }
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "An exception occured while trying get table names.", ex);
            }
        }
        return tableNames;
    }

    private String resolveSchema(ResultSet result) {
        try {
            return result.getString("TABLE_SCHEMA");
        } catch (Exception e) {

        }
        return null;
    }

    public Connection createConnection() throws SQLException, ClassNotFoundException {
        if (driverClass != null && !"".equals(driverClass)) {
            Class.forName(driverClass);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() {
        if(connection == null){
            try {
                connection = createConnection();
            } catch (Exception e) {
                throw new RuntimeException("Could get connection using current configuration, use 'DBUnit Setup' to configure JDBC connection. Error: "+e.getMessage());
            }
        }
        return connection;
    }
}
