/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import static java.lang.Math.log;

/**
 *
 * @author Hamot
 */
public class DefaultScheme implements WeightScheme {

    private double docWeights;

    public DefaultScheme(double mDocWeight) {
        this.docWeights = mDocWeight;
    }

    @Override
    public double calcWqt(long N, long dft) {
        return (dft == 0) ? 0 : log(1 + N / dft);
    }

    @Override
    public double calcWdt(long tfd) {
        return (tfd == 0) ? 1 : 1 + log(tfd);
    }

    @Override
    public double calcLd() {
        return docWeights;
    }

}
