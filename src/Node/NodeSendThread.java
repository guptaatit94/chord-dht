package Node;

/*
 * NodeSendThread.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is responsible of sending updates to other nodes.
 */

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class NodeSendThread extends Thread implements Serializable {
    private String address;
    private int port;
    private String message;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;

    /**
     * Constructor which initializes the class variables.
     *
     * @param address address on which to send the data
     * @param port    port on which other node is listening
     * @param message message to be delivered
     */
    public NodeSendThread(String address, int port, String message) {
        this.address = address;
        this.port = port;
        this.message = message;
    }

    /**
     * This will send the data to the node.
     */
    @Override
    public void run() {
        //System.out.println("\tStarting NodeSendThread");
        try {
            this.socket = new Socket(this.address, this.port);
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.writeObject(message);

            this.objectOutputStream.close();
            this.socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("\tExiting NodeSendThread");
    }
}
