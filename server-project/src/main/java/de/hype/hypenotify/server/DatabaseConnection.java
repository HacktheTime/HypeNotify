package de.hype.hypenotify.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/";
    private static final String NOTIFYURL = URL + "notify";
    private static final String BBSENTIALSURL = URL + "bbsentials";
    private static final String USER = "bot";
    private static final String PASSWORD = "Gdft46uihjcxsEfU";

    public static Connection getNotifyConnection() throws SQLException {
        return DriverManager.getConnection(NOTIFYURL, USER, PASSWORD);
    }

    public static Connection getBingoNetConnection() throws SQLException {
        return DriverManager.getConnection(BBSENTIALSURL, USER, PASSWORD);
    }
}