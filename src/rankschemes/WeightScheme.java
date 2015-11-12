/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rankschemes;

/**
 *
 * @author Jay
 */
public abstract class WeightScheme {

    protected double wqt;
    protected double wdt;
    protected double Ld;

    /**
     * calculate weight of term t for query q term t
     *
     * @param N document collection
     * @param dft document frequency for given term
     */
    public abstract void calcWqt(double N, double dft);

    /**
     * calculate weight of term frequency
     *
     * @param tftd term frequency for term t and document d
     */
    public abstract void calcWdt(double tftd);

    /**
     * calculate weight of given document
     */
    public abstract void calcLd();

    /**
     *
     * @return weight of query for given term
     */
    public final double getWqt() {
        return wqt;
    }

    /**
     *
     * @return weight of document for given term
     */
    public final double getWdt() {
        return wdt;
    }

    /**
     *
     * @return weight of the given document
     */
    public final double getLd() {
        return Ld;
    }
}
