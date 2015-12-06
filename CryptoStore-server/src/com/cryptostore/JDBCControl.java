package com.cryptostore;

import java.lang.*;
import java.sql.*;
import java.util.Base64;

public class JDBCControl {
    private static String DBHost;
    private static String serverUsername;
    private static String serverPassword;

    public JDBCControl(String DBHost, String serverUsername, String serverPassword){
        JDBCControl.DBHost = DBHost;
        JDBCControl.serverUsername = serverUsername;
        JDBCControl.serverPassword =  serverPassword;
    }

    public static boolean usernameExists(String username) {
        boolean exists = false;

        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT username " +
                            "FROM user_credentials " +
                            "WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);

            ResultSet queryResult = statement.executeQuery();

            exists = queryResult.next();
        } catch (SQLException ex) {
            Error.NO_USER.print();
        }

        return exists;
    }

    public static boolean checkUserPassword(String user, String password) {
        boolean verify = false;

        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT username, hash " +
                            "FROM user_credentials";

            Statement statement = connection.prepareStatement(query);
            ResultSet queryResult = statement.executeQuery(query);

            while (queryResult.next()) {
                if (queryResult.getString("username").equals(user)) {
                    verify = password.equals(queryResult.getString("hash"));
                }
            }

        } catch (SQLException ex) {
            Error.NO_USER.print();
        }

        return verify;
    }

    public static boolean createNewUser(String username, String passwdHash, byte[] salt) {
        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "INSERT INTO user_credentials (username, hash, salt) " +
                            "VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, passwdHash);
            statement.setString(3, Base64.getEncoder().encodeToString(salt));

            statement.execute();

        } catch (SQLException ex) {
            Error.NO_USER.print();
        }

        return checkUserPassword(username, passwdHash);
    }

    public static byte[] getSalt(String username) {
        String salt = null;

        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT salt " +
                            "FROM user_credentials " +
                            "WHERE username = ? ";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                salt = queryResult.getString("salt");
            }

        } catch (SQLException ex) {
            Error.NO_USER.print();
        }

        if (salt.length() > 0) {
            return Base64.getDecoder().decode(salt);
        } else {
            return null;
        }
    }
}
