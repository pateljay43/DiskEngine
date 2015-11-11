package structures;

import diskengine.DiskEngine;
import constants.Constants;
import rankscheme.WackyScheme;
import rankscheme.OkapiScheme;
import rankscheme.DefaultScheme;
import rankscheme.TraditionalScheme;
import rankscheme.WeightScheme;

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
    private WeightScheme scheme;
    private double Ad;

    public Posting() {
    }

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

    public double getWdt() {
        return scheme.getWdt();
    }

    public void calculateWqt(int N, int dft) {
        scheme.calcWqt(N, dft);
    }

    public double getWqt() {
        return scheme.getWqt();
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

    public double getAd() {
        return Ad;
    }

    public void setAd(double _Ad) {
        Ad = _Ad;
    }

    public void calculateAd() {
//        Ad = (scheme.wqt * scheme.wdt) / scheme.Ld;
        Ad = (scheme.getWqt() * scheme.getWdt());
    }

    public void finalizeAd() {
        Ad = Ad / scheme.getLd();
    }

    public void setScheme(double docWeight, double byteSize, double avgTf, double avgDocWeight) {
        switch (Constants.scheme) {
            case 1:
                scheme = new DefaultScheme(docWeight, tf);
                break;
            case 2:
                scheme = new TraditionalScheme(docWeight, tf);
                break;
            case 3:
                scheme = new OkapiScheme(docWeight, avgDocWeight, tf);
                break;
            case 4:
                scheme = new WackyScheme(avgTf, byteSize, tf);
                break;
            default:
                scheme = null;
                break;
        }
    }
}
