/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rankschemes;

import static java.lang.Math.log;

public final class OkapiScheme extends WeightScheme {

    private final double Kd;

    public OkapiScheme(double docWD, double docWA, double _tf) {
        this.Kd = 1.2 * (0.25 + 0.75 * ((docWA == 0.0) ? 0.0 : (docWD / docWA)));
        this.calcWdt(_tf);
        this.calcLd();
    }

    @Override
    public final void calcWqt(double N, double dft) {
        double n = N - dft + 0.5;
        double d = dft + 0.5;
        wqt = (d == 0.0 || n == 0.0) ? 0.0 : log(n / d);
    }

    @Override
    public final void calcWdt(double tftd) {
        double n = (2.2 * tftd);
        double d = (Kd + tftd);
        wdt = (d == 0.0) ? 0 : (n / d);
    }

    @Override
    public final void calcLd() {
        Ld = 1.0;
    }

}
