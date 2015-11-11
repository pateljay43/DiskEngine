/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queue;

import structures.Posting;
import java.util.Comparator;

/**
 *
 * @author JAY
 */
public class PriorityQueueComparator implements Comparator<Posting> {

    private static int above;
    private static int below;

    /**
     * arrange the collection in given order
     *
     * @param order true - ascending; false - descending
     */
    public PriorityQueueComparator(boolean order) {
        above = order ? -1 : 1;
        below = order ? 1 : -1;
    }

    @Override
    public int compare(Posting o1, Posting o2) {
        return (o2.getAd() - o1.getAd()) >= 0 ? above : below;
    }

}
