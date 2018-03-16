package RegistrationServer;

/*
 * WorkerSendThread.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is responsible for sending the updated FingerTables to all alive nodes.
 */

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import FingerTable.*;

public class WorkerSendThread extends Thread implements Serializable {

    private FingerTable fingerTable;
    private String address;
    private int sendingPort;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;

    /**
     * Constructor which is used to initialize class variables.
     *
     * @param fingerTable calculated fingertable for a particular node.
     * @param address     address of that node
     * @param port        port on which that node is listening
     */
    public WorkerSendThread(FingerTable fingerTable, String address, String port) {
        this.fingerTable = fingerTable;
        this.address = address;
        this.sendingPort = Integer.parseInt(port);
    }

    /**
     * The run method is executed when the thread is started and will write the finger table object onto the stream and
     * will send it.
     */
    @Override
    public void run() {
        try {
            //System.out.println("\t\t\tIn WorkerSendThread");
            this.socket = new Socket(this.address, this.sendingPort);
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(this.fingerTable);
            //System.out.println("\t\t\tExiting WorkerSendThread");

            this.objectOutputStream.close();
            this.socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
