package client.controller;

public interface ClientController {
    void start ();

    boolean stop () throws InterruptedException;
}
