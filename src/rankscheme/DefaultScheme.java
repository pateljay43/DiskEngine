/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rankscheme;

import static java.lang.Math.log;

public final class DefaultScheme extends WeightScheme {

    private final double docWeight;

    public DefaultScheme(double _docWeight, double _tf) {
        docWeight = _docWeight;
        this.calcWdt(_tf);
        this.calcLd();
    }

    @Override
    public final void calcWqt(double N, double dft) {
        wqt = log(1.0 + N / dft);
    }

    @Override
    public final void calcWdt(double tftd) {
        wdt = 1.0 + log(tftd);
    }

    @Override
    public final void calcLd() {
        Ld = docWeight;
    }
}
