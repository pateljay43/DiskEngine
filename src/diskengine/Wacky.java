/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/**
 *
 * @author Hamot
 */
public class Wacky implements WeightScheme {

    double aveTfd;
    long byteSize;

    public Wacky(double mAveTfd, long mByteSize) {
        this.aveTfd = mAveTfd;
        this.byteSize = mByteSize;
    }

    @Override
    public double calcWqt(long N, long dft) {
        long n = (N - dft);
        long d = dft;
        return max(0, (n == 0 || d == 0) ? 0 : log(n / d));
    }

    @Override
    public double calcWdt(long tfd) {
        double n = (1 + log(tfd));
        double d = (1 + log(aveTfd));
        return (n == 0 || d == 0) ? 0 : n / d;
    }

    @Override
    public double calcLd() {
        return (byteSize <= 0) ? 0 : sqrt(byteSize);
    }

}
