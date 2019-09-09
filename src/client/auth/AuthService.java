package client.auth;

public interface AuthService extends AutoCloseable{

    void close() throws Exception;

    boolean authUser(String username, String password) throws ClassNotFoundException;
}
