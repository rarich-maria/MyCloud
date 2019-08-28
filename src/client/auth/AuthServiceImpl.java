package client.auth;

import java.sql.*;

public class AuthServiceImpl implements AuthService {
    private boolean result;
    private Connection con;
    private PreparedStatement ps;
    private PreparedStatement checkNameExist;
    private PreparedStatement updateUserName;

    private String url="jdbc:sqlite:authorizationUsers.db";


    static {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
    }



    public AuthServiceImpl()  {
        try {
            con = DriverManager.getConnection(url);
            ps = con.prepareStatement("SELECT * FROM authorizationUsers WHERE Name = ? AND Password= ?");
            checkNameExist = con.prepareStatement("SELECT * FROM authorizationUsers WHERE Name = ? ");
            updateUserName = con.prepareStatement("UPDATE authorizationUsers SET Name = ? WHERE Name = ? AND Password= ?");
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

    @Override
    public boolean renameUser(String username, String newName, String password) throws ClassNotFoundException{
        if (authUser(username, password)) {

            try {
                checkNameExist.setString(1, newName);
                ResultSet rst = checkNameExist.executeQuery();

                if (!rst.next()) {
                    updateUserName.setString(1, newName);
                    updateUserName.setString(2, username);
                    updateUserName.setString(3, password);

                    if (updateUserName.executeUpdate()==1) {
                        result=true;
                        System.out.println("Updated name user ");
                    }else {
                        System.out.println("Can not updated name user ");
                        result=false;
                    }
                }else {
                    System.out.println("Name exist!!! change Name ");
                    result=false;
                }


            }catch (SQLException e) {
                e.printStackTrace();
            }

        }else {
            System.out.println("Введите правильный пароль для изменения имени");
        }
        return result;
    }




}
