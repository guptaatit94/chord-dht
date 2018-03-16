package Node;

/*
 * Node.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is the main class for Nodes. It has implementation of all necessary functionality of node class.
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import FingerTable.*;

public class Node {
    //class variables
    private static String address;
    private final int N = 4;            // running simulation with 2^4 nodes.
    private final int NO_OF_NODES = (int) Math.pow(2, N);
    private int nodeId;
    private Socket sendingSocket;
    private Scanner sc = new Scanner(System.in);
    private int serverPort = 5000;      // assuming server is listening at this port
    private int listeningPort;
    private BufferedWriter bw;
    private FingerTable fingerTable;
    private boolean isAlive;
    private Map<Integer, ArrayList<String>> data;
    private List<FingerRow> sortedContents;

    /**
     * Constructor for initializing the class variables.
     *
     * @param nodeId        id of this node
     * @param listeningPort port at which this node will be listening.
     */
    public Node(int nodeId, int listeningPort) {
        this.nodeId = nodeId;
        this.listeningPort = listeningPort;
        this.data = new ConcurrentHashMap<>();
    }

    /**
     * Start the service at this node. This will create one thread for listening.
     */
    public void startService() {
        try {
            String input;

            new NodeRecThread(this, this.listeningPort).start();
            this.isAlive = true;

            // notify server that you are online.
            this.sendingSocket = new Socket(address, serverPort);
            this.bw = new BufferedWriter(new OutputStreamWriter(this.sendingSocket.getOutputStream()));

            contactServer();

            while (this.isAlive()) {
                System.out.println("Menu: ");
                System.out.println("1. Add data ");
                System.out.println("2. Query the data");
                System.out.println("3. Display the data on this node");
                System.out.println("4. Print the FingerTable at this node");
                System.out.println("5. Turn off the node");
                System.out.println(">>>");
                input = sc.nextLine();

                switch (Integer.parseInt(input)) {
                    case 1:
                        System.out.println("Enter the NodeId on which you want to enter the data: ");
                        String destNode = sc.nextLine();
                        System.out.println("Enter the data you want to enter at " + destNode);
                        String inputData = sc.nextLine();
                        if (Integer.parseInt(destNode) == this.nodeId) {
                            addData(Integer.parseInt(destNode), inputData);
                        } else {
                            routeData(Integer.parseInt(destNode), inputData);
                        }
                        break;

                    case 2:
                        System.out.println("Enter the NodeID of data you want to query: ");
                        String queryId = sc.nextLine();
                        queryNode(Integer.parseInt(queryId), InetAddress.getLocalHost().getHostAddress(), this.listeningPort);
                        break;

                    case 3:
                        System.out.println(this.data);
                        break;

                    case 4:
                        printFingerTables();
                        break;

                    case 5:
                        this.sendingSocket = new Socket(address, serverPort);
                        this.bw = new BufferedWriter(new OutputStreamWriter(this.sendingSocket.getOutputStream()));
                        //handle files here before exit
                        distributeData(false, "ADD");
                        disconnectServer();
                        System.out.println("Disconnected from server");
                        this.isAlive = false;
                        System.exit(0);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method distributes data when a node goes down or when a new node joins
     *
     * @param flag    false if node dies, data gets added to next alive node
     *                true if new node arrives, data routes from the next node.
     * @param command ADD/ ROUTE based on above description.
     */

    public void distributeData(boolean flag, String command) {
        String toSendMessage = null;
        String destAddress = this.fingerTable.getRow(0).getAddress();
        int destPort = this.fingerTable.getRow(0).getPort();
        ArrayList<NodeSendThread> activeThreads = new ArrayList<>();

        for (Integer id : this.data.keySet()) {
            if (flag) {
                if (id == this.nodeId) {
                    continue;
                }
            }
            ArrayList<String> temp = this.data.get(id);
            for (String message : temp) {
                toSendMessage = command + "|" + id + "|" + message;
                NodeSendThread thread = new NodeSendThread(destAddress, destPort, toSendMessage);
                thread.start();
                activeThreads.add(thread);
            }
            this.data.remove(id);
        }

        for (NodeSendThread nst : activeThreads) {
            try {
                nst.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is used to query the Chord for data from particular node.
     *
     * @param queryId node id from which data is to be queried.
     * @param address address of this node, to get the data.
     * @param port    listening port of this node to get the data.
     */
    public void queryNode(int queryId, String address, int port) {
        if (queryId == this.nodeId) {
            queryThisNode(queryId, address, port);
            return;
        }

        String toSendMessage = null;
        this.sortedContents = createCopy(this.fingerTable.getContents());

        int i;
        for (i = 0; i < this.fingerTable.getSize(); i++) {
            if (this.sortedContents.get(i).getValue() == queryId) {
                toSendMessage = "QUERY_THIS_NODE|" + queryId + "|" + address + "|" + port;
                new NodeSendThread(this.sortedContents.get(i).getAddress(), this.sortedContents.get(i).getPort(), toSendMessage).start();
                return;
            } else if (this.sortedContents.get(i).getValue() > queryId) {
                break;
            }
        }
        int indexToUse = i - 1;
        if (i == 0) {
            indexToUse = 0;
        }

        int relativeDestNode = getRelativeNode(this.nodeId, queryId);
        int relativeSuccessor = getRelativeNode(this.nodeId, this.sortedContents.get(indexToUse).getSuccessor());

        if (relativeSuccessor >= relativeDestNode) {
            toSendMessage = "QUERY_THIS_NODE|" + queryId + "|" + address + "|" + port;
            new NodeSendThread(this.sortedContents.get(indexToUse).getAddress(), this.sortedContents.get(indexToUse).getPort(), toSendMessage).start();
        } else {
            toSendMessage = "QUERY|" + queryId + "|" + address + "|" + port;
            new NodeSendThread(this.sortedContents.get(indexToUse).getAddress(), this.sortedContents.get(indexToUse).getPort(), toSendMessage).start();
        }
    }

    /**
     * This method is called when we are sure data is at this node.
     *
     * @param queryId node id from which data is to be queried.
     * @param address address of this node, to get the data.
     * @param port    listening port of this node to get the data.
     */
    public void queryThisNode(int queryId, String address, int port) {
        if (this.data.containsKey(queryId)) {
            new NodeSendThread(address, port, "DATA|" + this.data.get(queryId).toString()).start();
        } else {
            new NodeSendThread(address, port, "DATA|" + "NO SUCH KEY FOUND").start();
        }
    }

    /**
     * This method is used to route data to the appropriate destination node.
     *
     * @param destNodeId expected destination node id
     * @param message    message to be saved at that node.
     */
    public void routeData(int destNodeId, String message) {
        if (destNodeId == this.nodeId) {
            addData(destNodeId, message);
            return;
        }
        String toSendMessage = null;
        this.sortedContents = createCopy(this.fingerTable.getContents());

        this.sortedContents.sort((fr1, fr2) -> fr1.getValue() - fr2.getValue());
        int i;
        for (i = 0; i < this.fingerTable.getSize(); i++) {
            if (this.sortedContents.get(i).getValue() == destNodeId) {
                toSendMessage = "ADD|" + destNodeId + "|" + message;
                new NodeSendThread(this.sortedContents.get(i).getAddress(), this.sortedContents.get(i).getPort(), toSendMessage).start();
                return;
            } else if (this.sortedContents.get(i).getValue() > destNodeId) {
                break;
            }
        }

        int indexToUse = i - 1;
        if (i == 0) {
            indexToUse = 0;
        }

        int relativeDestNode = getRelativeNode(this.nodeId, destNodeId);
        int relativeSuccessor = getRelativeNode(this.nodeId, this.sortedContents.get(indexToUse).getSuccessor());

        if (relativeSuccessor >= relativeDestNode) {
            toSendMessage = "ADD|" + destNodeId + "|" + message;
            new NodeSendThread(this.sortedContents.get(indexToUse).getAddress(), this.sortedContents.get(indexToUse).getPort(), toSendMessage).start();
        } else {
            toSendMessage = "ROUTE|" + destNodeId + "|" + message;
            new NodeSendThread(this.sortedContents.get(indexToUse).getAddress(), this.sortedContents.get(indexToUse).getPort(), toSendMessage).start();
        }
    }

    /**
     * Helper function to copy an ArrayList
     *
     * @param contents list to be copied
     * @return deep copy of ArrayList
     */
    public ArrayList<FingerRow> createCopy(ArrayList<FingerRow> contents) {
        ArrayList<FingerRow> temp = new ArrayList<>();
        for (FingerRow s : contents) {
            temp.add(s);
        }
        return temp;
    }

    /**
     * This is a helper method for the route method used to find the relative node to calculate the next hop.
     *
     * @param insertionNode node from which user wants to insert data.
     * @param otherNode     node from which you want to calculate relative distance.
     * @return relative distance
     */
    public int getRelativeNode(int insertionNode, int otherNode) {
        if (otherNode > insertionNode) {
            return otherNode - insertionNode;
        } else {
            return NO_OF_NODES - insertionNode + otherNode;
        }
    }

    /**
     * Adds data to this node
     *
     * @param destNode  destination node used as key
     * @param inputData message
     */
    public void addData(int destNode, String inputData) {
        ArrayList<String> temp;

        if (!this.data.containsKey(destNode)) {
            temp = new ArrayList<>();
        } else {
            temp = this.data.get(destNode);
        }
        temp.add(inputData);
        this.data.put(destNode, temp);
    }

    /**
     * This method notifies the server that it is online.
     */
    public void contactServer() {
        try {
            bw.write("BOOT_UP|" + this.nodeId + "|" + this.listeningPort + "\n");
            bw.flush();
            bw.close();
            sendingSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method notifies the server that it is offline.
     */
    public void disconnectServer() {
        try {
            bw.write("SHUT_DOWN|" + this.nodeId + "|" + this.listeningPort + "\n");
            bw.flush();
            bw.close();
            sendingSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method prints the Finger Tables.
     */
    public void printFingerTables() {
        System.out.println(this.fingerTable);
    }

    /**
     * This method finds if the node is active or not.
     *
     * @return true iff node is active.
     */
    public boolean isAlive() {
        return this.isAlive;
    }

    /**
     * Updates the fingerTable based on update received from server.
     *
     * @param fingerTable updated fingertable
     */
    public void updateFingerTable(FingerTable fingerTable) {
        this.fingerTable = fingerTable;
    }

    /**
     * The main method
     *
     * @param args args[0] address of the server.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        if (args.length < 1) {
            System.out.println("Enter the address of Registration Server: ");
            address = sc.nextLine();
        } else {
            address = args[0];
        }

        System.out.println("Enter the nodeId for this node: ");
        int nodeId = sc.nextInt();
        System.out.println("Enter the listening port for this node: ");
        int listeningPort = sc.nextInt();

        Node node = new Node(nodeId, listeningPort);
        node.startService();
    }

}

