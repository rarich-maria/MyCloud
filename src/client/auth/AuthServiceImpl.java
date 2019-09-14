package client.auth;

import java.sql.*;

public class AuthServiceImpl implements AuthService {
    private boolean result;
    private Connection con;
    private PreparedStatement ps;

    private final String URL ="jdbc:sqlite:authorizationUsers.db";
    private final static String DB_DRIVER = "org.sqlite.JDBC";

    static {
        try {
            Class.forName(DB_DRIVER);
            } catch (ClassNotFoundException e) {
            e.printStackTrace();
            }
    }
    public AuthServiceImpl()  {
        try {
            con = DriverManager.getConnection(URL);
            ps = con.prepareStatement("SELECT * FROM authorizationUsers WHERE Name = ? AND Password= ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        con.close();
    }

    @Override
    public boolean authUser(String username, String password) throws ClassNotFoundException {
        try {
            System.out.println("подключилось!");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rst = ps.executeQuery();
            result=rst.next();
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }
}
