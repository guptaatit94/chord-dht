package Node;

/*
 * NodeRecThread.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is responsible of receiving and handling all the updates on the node.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import FingerTable.*;

public class NodeRecThread extends Thread {

    private Node node;
    private ServerSocket serverSocket;
    private int serverPort;
    private Socket receivingSocket;
    private ObjectInputStream objectInputStream;
    private String inputData;

    /**
     * Constructor which initializes the class variables.
     *
     * @param node       object of this node
     * @param serverPort port at which this node is listening.
     */
    public NodeRecThread(Node node, int serverPort) {
        this.node = node;
        this.serverPort = serverPort;
    }

    /**
     * This will spawn a thread which will listen at the given port until the node is active.
     */
    @Override
    public void run() {
        //System.out.println("Starting Node Receiving thread");
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
            while (this.node.isAlive()) {
                this.receivingSocket = serverSocket.accept();

                this.objectInputStream = new ObjectInputStream(receivingSocket.getInputStream());

                Object o = objectInputStream.readObject();
                if (o instanceof FingerTable) {
                    this.node.updateFingerTable((FingerTable) o);
                    this.node.distributeData(true, "ROUTE");
                } else if (o instanceof String) {
                    this.inputData = (String) o;

                    String[] type = this.inputData.trim().split("\\|");
                    if (type[0].equals("ADD")) {
                        this.node.addData(Integer.parseInt(type[1]), type[2]);
                    } else if (type[0].equals("ROUTE")) {
                        this.node.routeData(Integer.parseInt(type[1]), type[2]);
                    } else if (type[0].equals("QUERY")) {
                        this.node.queryNode(Integer.parseInt(type[1]), type[2], Integer.parseInt(type[3]));
                    } else if (type[0].equals("QUERY_THIS_NODE")) {
                        this.node.queryThisNode(Integer.parseInt(type[1]), type[2], Integer.parseInt(type[3]));
                    } else if (type[0].equals("DATA")) {
                        System.out.println("Data: " + type[1]);
                    }
                }
                this.objectInputStream.close();
                this.receivingSocket.close();
            }
            this.serverSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
