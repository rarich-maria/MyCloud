package client.auth;

public interface AuthService extends AutoCloseable{

    void close() throws Exception;

    boolean authUser(String username, String password) throws ClassNotFoundException;
    boolean renameUser(String username, String newName, String password)throws ClassNotFoundException;
}
