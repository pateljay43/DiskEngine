/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rankscheme;

import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public final class WackyScheme extends WeightScheme {

    private final double avgTfd;
    private final double byteSize_d;

    public WackyScheme(double _avgTfd, double _byteSize_d, double _tf) {
        avgTfd = _avgTfd;
        this.byteSize_d = _byteSize_d;
        this.calcWdt(_tf);
        this.calcLd();
    }

    @Override
    public final void calcWqt(double N, double dft) {
        double n = (N - dft);
        double d = dft;
        wqt = max(0.0, ((n < d || n <= 0.0) ? 0.0 : log(n / d)));
    }

    @Override
    public final void calcWdt(double tftd) {
        double n = 1.0 + log(tftd);
        double d = 1.0 + log(avgTfd);
        wdt = (n == 0.0 || d == 0.0) ? 0.0 : n / d;
    }

    @Override
    public final void calcLd() {
        Ld = sqrt(byteSize_d);
    }
}
