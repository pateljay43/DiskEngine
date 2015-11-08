/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

/**
 *
 * @author Hamot
 */
public interface WeightScheme {
    
    /**
     *
     * @param N
     * @param dft
     * @return
     */
    public double calcWqt(long N, long dft);
    
    public double calcWdt(long tfd);
   
    public double calcLd();
}
