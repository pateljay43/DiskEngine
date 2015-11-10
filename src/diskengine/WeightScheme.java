/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

/**
 *
 * @author Jay
 */
abstract class WeightScheme {

    protected double wqt;
    protected double wdt;
    protected double Ld;

    public abstract void calcWqt(double N, double dft);

    public abstract void calcWdt(double tftd);

    public abstract void calcLd();
    
}