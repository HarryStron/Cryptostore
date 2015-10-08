import java.sql.*;

public class JDBCControl {
    public JDBCControl(){
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:8888/users", "admin", "locH4al");

            Statement stmt = conn.createStatement();

            String strSelect = "select id, username, password from users";
            ResultSet rset = stmt.executeQuery(strSelect);

            int rowCount = 0;
            while(rset.next()) {
                int id = rset.getInt("id");
                String username = rset.getString("username");
                String password = rset.getString("password");
                System.out.println(id + " : " + username + " : " + password);
                ++rowCount;
            }
            System.out.println("\nTotal number of records: " + rowCount);

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }
}
