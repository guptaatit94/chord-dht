package FingerTable;

/*
 * FingerRow.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is a helper class tp FingerTable and is used for storing a single row in the FingerTable.
 */

import java.io.Serializable;

public class FingerRow implements Serializable {
    private int i;
    private int value;
    private int successor;
    private String address;
    private int port;

    /**
     * Constructor which initializes the class variables.
     *
     * @param i         ith row
     * @param value     id of node
     * @param successor id of successor
     * @param address   address of successor
     */
    public FingerRow(int i, int value, int successor, String[] address) {
        this.i = i;
        this.value = value;
        this.successor = successor;
        this.address = address[0];
        this.port = Integer.parseInt(address[1]);
    }

    /**
     * Returns the address of the successor.
     *
     * @return address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Returns the id of the successor.
     *
     * @return id
     */
    public int getSuccessor() {
        return this.successor;
    }

    /**
     * Returns the contact port of the successor.
     *
     * @return port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the id at ith row.
     *
     * @return value
     */
    public int getValue() {
        return this.value;
    }

    /**
     * String representation of object.
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.i + "\t\t" + this.value + "\t\t" + this.successor + "\n";
    }
}