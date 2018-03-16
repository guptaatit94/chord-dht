package RegistrationServer;

/*
 * ServerThread.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is responsible for all connection requests received on the server.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private RegistrationServer registrationServer;
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;

    /**
     * Constructor which sets initializes the class variables.
     *
     * @param registrationServer object of registration server
     * @param port               port number on which the server is listening.
     */
    public ServerThread(RegistrationServer registrationServer, int port) {
        this.registrationServer = registrationServer;
        this.port = port;
    }


    /**
     * The run method. Executed when we start the thread and handles all the potential requests.
     */
    @Override
    public void run() {
        //System.out.println("\tStarted ServerThread!");
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            //System.out.println("Cannot open port " + this.port);
            e.printStackTrace();
        }

        while (registrationServer.isAlive()) {
            try {
                this.socket = this.serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            new WorkerThread(this.registrationServer, this.socket).start();
        }
        //System.out.println("\tExited ServerThread!");
    }
}
