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
public class OkapiScheme implements WeightScheme {

    double Kd;

    public OkapiScheme(double docWD, double docWA) {
        this.Kd = 1.2 * (0.25 + 0.75 * (docWD / docWA));
    }

    @Override
    public double calcWqt(long N, long dft) {
        double n = (N - dft + 0.5);
        double d = (dft + 0.5);
        return (d == 0 || n == 0) ? 0 : log(n / d);
    }

    @Override
    public double calcWdt(long tfd) {
        return (2.2 * tfd) / (Kd + tfd);
    }

    @Override
    public double calcLd() {
        return 1;
    }

    public double getKd() {
        return Kd;
    }

}
