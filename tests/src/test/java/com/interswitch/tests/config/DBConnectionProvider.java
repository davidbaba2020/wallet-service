package com.interswitch.tests.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public record DBConnectionProvider(String jdbcUrl, String username, String password) {

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

}