class Server {

    public static void main(String args[]) throws Exception {
        //new ServerManager(5556);
        JDBCControl jdbcControl = new JDBCControl("jdbc:mysql://mysql.student.sussex.ac.uk:3306/cs391", "cs391", "r127xxhar1");

        System.out.println(jdbcControl.checkUserPassword("HarryAdmin", "passwordHash01passwordHash01passwordHash01passwordHash0111111111"));
        System.out.println(jdbcControl.createNewUser("NonAdmin", "passwordHash01passwordHash01passwordHash01passwordHash0122222222"));
        //System.out.println(new HashGenerator().getHashString(new HashGenerator().getHash("paSSw@", "sexySalt".getBytes(), 1000, 32)));
    }
} 