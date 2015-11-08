package diskengine;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Hamot
 */
public class Posting {

    private long[] positions;
    private int docID;
    private int tf; // term frequency

    public Posting() {
    }

    public Posting(int _docID) {
        docID = _docID;
    }

    public void initPositions() {
        this.positions = new long[tf];
    }

    public void setPosition(long pos, int index) {
        this.positions[index] = pos;
    }

    /**
     * @return the positions
     */
    public long[] getPositions() {
        return positions;
    }

    public long getPosition(int index) {
        long position = this.positions[index];
        return position;
    }

    /**
     * @param positions the positions to set
     */
    public void setPositions(long[] positions) {
        this.positions = positions;
    }

    /**
     * @return the docID
     */
    public int getDocID() {
        return docID;
    }

    /**
     * @param docID the docID to set
     */
    public void setDocID(int docID) {
        this.docID = docID;
    }

    /**
     * @return the tf
     */
    public int getTf() {
        return tf;
    }

    /**
     * @param tf the tf to set
     */
    public void setTf(int tf) {
        this.tf = tf;
    }

}
