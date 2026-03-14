package com.yourname.project.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        // This MUST match the 'Key' name you typed in Render
        String dbUrl = System.getenv("DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }
}