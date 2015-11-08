/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import java.util.Comparator;

/**
 *
 * @author JAY
 */
public class PriorityQueueComparator implements Comparator<Posting> {

    PriorityQueueComparator() {
    }

    @Override
    public int compare(Posting o1, Posting o2) {
        return (o2.getAd() - o1.getAd()) > 0 ? 1 : -1;  // decreasing order
    }

}
