package RegistrationServer;

/*
 * WorkerThread.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is responsible for processing all the requests.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerThread extends Thread {

    private RegistrationServer registrationServer;
    private Socket socket;

    /**
     * Constructor which initializes the class variables.
     * @param registrationServer Object of the registrationServer
     * @param socket socket connection to process the request.
     */
    public WorkerThread(RegistrationServer registrationServer, Socket socket) {
        this.registrationServer = registrationServer;
        this.socket = socket;
    }

    @Override
    public void run() {
        //System.out.println("\t\tStarted WorkerThread");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = br.readLine();

            String[] type = message.trim().split("\\|");

            switch (type[0]) {
                case "BOOT_UP":
                    this.registrationServer.addNode(Integer.parseInt(type[1]), this.socket.getInetAddress().getHostName(), type[2]);

                    this.registrationServer.updateNodes();
                    break;

                case "SHUT_DOWN":
                    registrationServer.removeNode(Integer.parseInt(type[1]));
                    this.registrationServer.updateNodes();
                    break;
            }

            this.socket.close();
            br.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("\t\tExited WorkerThread");

    }

}
