import java.sql.*;

public class JDBCControl {
    private static String DBHost;
    private static String serverUsername;
    private static String serverPassword;

    public JDBCControl(String DBHost, String serverUsername, String serverPassword){
        JDBCControl.DBHost = DBHost;
        JDBCControl.serverUsername = serverUsername;
        JDBCControl.serverPassword =  serverPassword;
    }

    public static boolean checkUserPassword(String user, String password) {
        boolean verify = false;

        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "SELECT id, username, hash " +
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

    public static boolean createNewUser(String username, String passwdHash) {
        try {
            Connection connection = DriverManager.getConnection(DBHost, serverUsername, serverPassword);

            String query =  "INSERT INTO user_credentials (username, hash) " +
                            "VALUES (\'" + username + "\', \'" + passwdHash + "\')";

            connection.prepareStatement(query).execute();

        } catch (SQLException ex) {
            Error.NO_USER.print();
        }

        return checkUserPassword(username, passwdHash);
    }
}
