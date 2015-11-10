/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import static java.lang.Math.log;

public final class TraditionalScheme extends WeightScheme {

    private final double docWeights;

    public TraditionalScheme(double _mDocWeight, double _tf) {
        docWeights = _mDocWeight;
        this.calcWdt(_tf);
        this.calcLd();
    }

    @Override
    public void calcWqt(double N, double dft) {
        wqt = (N == 0.0 || dft == 0.0) ? 0.0 : log(N / dft);
    }

    @Override
    public void calcWdt(double tftd) {
        wdt = tftd;
    }

    @Override
    public void calcLd() {
        Ld = docWeights;
    }
}
