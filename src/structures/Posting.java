package structures;

import constants.Constants;
import rankschemes.WackyScheme;
import rankschemes.OkapiScheme;
import rankschemes.DefaultScheme;
import rankschemes.TraditionalScheme;
import rankschemes.WeightScheme;

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

    private int docID;
    private int tf; // term frequency
    private double Ad;
    private long[] positions;
    private WeightScheme scheme;

    /**
     * Posting containing document id, term frequency, accumulator, positions of
     * each term, ranking scheme to be used
     */
    public Posting() {
    }

    /**
     * Posting containing document id, term frequency, accumulator, positions of
     * each term, ranking scheme to be used
     *
     * @param _docID initialize with given document id
     */
    public Posting(int _docID) {
        docID = _docID;
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
     * @return the term frequency
     */
    public int getTf() {
        return tf;
    }

    /**
     * @param tf the term frequency to set
     */
    public void setTf(int tf) {
        this.tf = tf;
    }

    /**
     *
     * @return weight of document for given term
     */
    public double getWdt() {
        return scheme.getWdt();
    }

    /**
     * calculate weight of term t for query q
     *
     * @param N document collection
     * @param dft document frequency for given term
     */
    public void calculateWqt(int N, int dft) {
        scheme.calcWqt(N, dft);
    }

    /**
     *
     * @return weight of query for given term
     */
    public double getWqt() {
        return scheme.getWqt();
    }

    /**
     * initialize the position array
     */
    public void initPositions() {
        this.positions = new long[tf];
    }

    /**
     * set position pos at given index in position array
     *
     * @param pos position to term in document
     * @param index index where position is to be set
     */
    public void setPosition(long pos, int index) {
        this.positions[index] = pos;
    }

    /**
     * @return the positions of all terms
     */
    public long[] getPositions() {
        return positions;
    }

    /**
     *
     * @param index index of position array
     * @return position of given term mapped the index in position array
     */
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
     *
     * @return accumulator
     */
    public double getAd() {
        return Ad;
    }

    /**
     * set accumulator _Ad
     *
     * @param _Ad
     */
    public void setAd(double _Ad) {
        Ad = _Ad;
    }

    /**
     * calculate accumulator based on weights of document and query for given
     * term
     */
    public void calculateAd() {
        Ad = (scheme.getWqt() * scheme.getWdt()) / scheme.getLd();
    }

    /**
     * set the ranking scheme to be used
     *
     * @param docWeight document weight stored in docWeight binary file
     * @param byteSize size to this document in bytes
     * @param avgTf average term frequency in this document
     * @param avgDocWeight average of all document weights stored in docWeights
     * binary file
     */
    public void setScheme(double docWeight, double byteSize, double avgTf, double avgDocWeight) {
        switch (Constants.scheme) {
            case 0:
                scheme = new DefaultScheme(docWeight, tf);
                break;
            case 1:
                scheme = new TraditionalScheme(docWeight, tf);
                break;
            case 2:
                scheme = new OkapiScheme(docWeight, avgDocWeight, tf);
                break;
            case 3:
                scheme = new WackyScheme(avgTf, byteSize, tf);
                break;
            default:
                scheme = null;
                break;
        }
    }
}
