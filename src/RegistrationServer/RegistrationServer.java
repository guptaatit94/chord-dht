package RegistrationServer;

/*
 * RegistrationServer.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is the main class for Registration Server which handles all the alive/dead nodes.
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import FingerTable.*;

public class RegistrationServer {

    // class variables
    private Map<Integer, String[]> aliveNodes;
    private boolean[] successorNodes;
    private final int port = 5000;      //assuming the RegistrationServer is always available at port 5000
    private boolean isAlive;
    private final int N = 4;            // running simulation with 2^4 nodes.

    /**
     * Constructor for this class which initializes the Hashmap and other class variables.
     */
    public RegistrationServer() {
        this.aliveNodes = new ConcurrentHashMap<>();
        this.successorNodes = new boolean[(int) Math.pow(2, N)];
        this.isAlive = true;
    }

    /**
     * This method starts a server thread which will listen for all node requests.
     */
    public void startService() {
        new ServerThread(this, port).start();
    }

    /**
     * Returns the status of the server.
     *
     * @return boolean
     */
    public boolean isAlive() {
        return this.isAlive;
    }

    /**
     * Adds the node to alive nodes.
     *
     * @param nodeId      id of the new node
     * @param address     address of the new node
     * @param contactPort contact port of the new node.
     */
    public void addNode(Integer nodeId, String address, String contactPort) {
        System.out.println("Node " + nodeId +" joined!");
        String[] contactAddress = {address, contactPort};
        this.aliveNodes.put(nodeId, contactAddress);
        this.successorNodes[nodeId] = true;
    }

    /**
     * Removes a node from alive nodes when it dies.
     *
     * @param nodeId Id of the dead node
     */
    public void removeNode(Integer nodeId) {
        System.out.println("Node " + nodeId +" left!");
        this.aliveNodes.remove(nodeId);
        this.successorNodes[nodeId] = false;
    }

    /**
     * Spawns threads to handle the changes to be made whenever a node joins or leave the circle.
     */
    public void updateNodes() {
        for (Integer i : aliveNodes.keySet()) {
            new WorkerSendThread(calculateSuccessors(i), this.aliveNodes.get(i)[0], this.aliveNodes.get(i)[1]).start();
        }
    }

    /**
     * Calculates the Successor nodes for the finger tables.
     *
     * @param nodeId id of the node whose fingertable is being calculated.
     * @return FingerTable
     */
    public FingerTable calculateSuccessors(int nodeId) {
        int noOfNodes = (int) Math.pow(2, N);
        FingerTable fingerTable = new FingerTable();
        for (int i = 0; i < N; i++) {
            int value = (nodeId + (int) Math.pow(2, i)) % (noOfNodes);
            int temp = value;

            int successor = -1;
            while (successor == -1) {
                if (successorNodes[value]) {
                    successor = value;
                } else {
                    value++;
                    if (value == noOfNodes) {
                        value = 0;
                    }
                }
            }
            fingerTable.addRow(i, temp, successor, aliveNodes.get(successor));
        }
        return fingerTable;
    }

    /**
     * This is the main method which starts the RegistrationServer.
     *
     * @param args None
     */
    public static void main(String[] args) {
        System.out.println("Started Registration Server!");
        new RegistrationServer().startService();
    }
}
