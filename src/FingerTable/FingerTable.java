package FingerTable;

/*
 * FingerTable.java
 *
 * Author: Atit Gupta       ag3654
 *
 * This class is used for maintaining the Finger Tables.
 */

import java.io.Serializable;
import java.util.ArrayList;

public class FingerTable implements Serializable {

    private ArrayList<FingerRow> contents;

    /**
     * The constructor initializes the ArrayList.
     */
    public FingerTable() {
        contents = new ArrayList<>();
    }

    /**
     * Adds a row in FingerTable
     *
     * @param i         value of n
     * @param value     node
     * @param successor successor node
     * @param address   address of the successor node
     */
    public void addRow(int i, int value, int successor, String[] address) {
        contents.add(new FingerRow(i, value, successor, address));
    }

    /**
     * Returns the FingerTable represented by the ArrayList.
     *
     * @return contents
     */
    public ArrayList<FingerRow> getContents() {
        return this.contents;
    }

    /**
     * Returns the FingerRow for a particular i.
     *
     * @param i ith row
     * @return Finger row at location i.
     */
    public FingerRow getRow(int i) {
        return this.contents.get(i);
    }

    /**
     * Returns the size of the ArrayList.
     *
     * @return size of ArrayList.
     */
    public int getSize() {
        return this.contents.size();
    }

    /**
     * Returns the String representation of this object.
     *
     * @return string
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("i\t\tk+2^i\tsuccessor\n");
        for (FingerRow row : contents) {
            stringBuilder.append(row);
        }
        return stringBuilder.toString();
    }
}


