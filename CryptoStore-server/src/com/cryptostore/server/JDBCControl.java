package com.cryptostore.server;

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
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);
            String query =  "SELECT username " +
                            "FROM user_credentials " +
                            "WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet queryResult = statement.executeQuery();

            exists = queryResult.next();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            com.cryptostore.server.Error.DB_ERROR.print(); //TODO why did you use full path?
        }

        return exists;
    }

    public static boolean checkUserPassword(String user, String password) {
        boolean verify = false;

        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT hash " +
                    "FROM user_credentials " +
                    "WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user);

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                if (password.equals(queryResult.getString("hash"))) {
                    verify = true;
                }
            }

        } catch (SQLException ex) {
            com.cryptostore.server.Error.DB_ERROR.print();
        }

        return verify;
    }

    public static boolean isAdmin(String user) {
        boolean isAdmin = false;

        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT isAdmin " +
                            "FROM user_credentials " +
                            "WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user);

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                if (queryResult.getString("isAdmin").equals("1")) {
                    isAdmin = true;
                }
            }

        } catch (SQLException ex) {
            com.cryptostore.server.Error.DB_ERROR.print();
        }

        return isAdmin;
    }

    public static boolean checkEncPass(String user, String encPass) {
        boolean out = false;
        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT encPass " +
                            "FROM user_credentials " +
                            "WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user);

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                if (queryResult.getString("encPass").equals(encPass)) {
                    out = true;
                }
            }

        } catch (SQLException ex) {
            com.cryptostore.server.Error.DB_ERROR.print();
        }

        return out;
    }

    public static String getEncPassSalt(String username) {
        String encSalt = null;
        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT encSalt " +
                            "FROM user_credentials " +
                            "WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                encSalt = queryResult.getString("encSalt");
            }

        } catch (SQLException ex) {
            com.cryptostore.server.Error.DB_ERROR.print();
        }

        return encSalt;
    }

    public static boolean createNewUser(String username, String passwdHash, byte[] salt, String encPass, String encSalt, String isAdmin) {
        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "INSERT INTO user_credentials (username, hash, salt, encPass, encSalt, isAdmin) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, passwdHash);
            statement.setString(3, Base64.getEncoder().encodeToString(salt));
            statement.setString(4, encPass);
            statement.setString(5, encSalt);
            statement.setString(6, isAdmin);

            statement.execute();

        } catch (SQLException ex) {
            com.cryptostore.server.Error.DB_ERROR.print();
        }

        return checkUserPassword(username, passwdHash);
    }

    public static byte[] getSalt(String username) {
        String salt = null;

        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT salt " +
                            "FROM user_credentials " +
                            "WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                salt = queryResult.getString("salt");
            }

        } catch (SQLException ex) {
            com.cryptostore.server.Error.DB_ERROR.print();
        }

        if (salt.length() > 0) {
            return Base64.getDecoder().decode(salt);
        } else {
            return null;
        }
    }
}
