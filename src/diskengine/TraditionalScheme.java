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
public class TraditionalScheme implements WeightScheme {

    private double docWeights;

    public TraditionalScheme(double mDocWeight) {
        this.docWeights = mDocWeight;
    }

    @Override
    public double calcWqt(long N, long dft) {
        return (N == 0 || dft == 0) ? 0 : log(N / dft);
    }

    @Override
    public double calcWdt(long tfd) {
        return tfd;
    }

    @Override
    public double calcLd() {
        return docWeights;
    }

}
