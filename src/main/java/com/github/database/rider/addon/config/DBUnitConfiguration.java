package com.github.database.rider.addon.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by pestano on 21/09/16.
 */
public class DBUnitConfiguration {

    private String url;

    private String user;

    private String password = "";

    private String driverClass;

    public DBUnitConfiguration set(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        if(password == null){
            password = "";
        }
        driverClass = resolveDriverClass();
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
            }catch (Exception ex){
            }
        }
        return name;
    }

    @Override
    public String toString() {
        return "Url: " + url + ", User: " + user + ", Driver class: " + driverClass;
    }

    public void testConnection() {
        Connection connection = null;
        try {
            if(driverClass != null && !"".equals(driverClass)){
                Class.forName(driverClass);
            }
            connection = DriverManager.getConnection(url,user,password);
            connection.isValid(6);
        } catch (Exception e) {
           throw new RuntimeException("Could not acquire jdbc connection for current configuration: "+toString() +". "+e.getMessage() +" - "+e.getCause(),e);
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
