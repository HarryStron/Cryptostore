class Server {
    public static void main(String args[]) throws Exception {
        new ServerManager(5556);

//        /**HashGenerator testing**/
//        byte[] salt = HashGenerator.getSalt();
//        String hashedpass = HashGenerator.getHash("P4$$w0rd", salt, 100000, 32);
//
//        /**JDBCControl testing**/
//        new JDBCControl("jdbc:mysql://mysql.student.sussex.ac.uk:3306/cs391", "cs391", "r127xxhar1");
//
//        System.out.println(JDBCControl.createNewUser("Admin1", hashedpass, salt));
//        System.out.println(JDBCControl.checkUserPassword("Admin2", HashGenerator.getHash("P4$$w0rd", JDBCControl.getSalt("Admin2"), 100000, 32)));
//        System.out.println(JDBCControl.usernameExists("Admin4"));
    }
}