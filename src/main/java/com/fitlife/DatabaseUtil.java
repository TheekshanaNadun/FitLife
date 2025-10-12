package com.fitlife;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/GymDB";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // <-- MAMP root password
    private static Connection connection;

    public static Connection getConnection() throws Exception {
        if(connection == null || connection.isClosed()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
