/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queue;

import cache.Term;
import java.util.Comparator;

/**
 *
 * @author JAY
 */
public class CacheQueueComparator implements Comparator<Term> {

    private static int above;
    private static int below;

    /**
     * arrange the collection in given order
     *
     * @param order true - ascending; false - descending
     */
    public CacheQueueComparator(boolean order) {
        above = order ? -1 : 1;
        below = order ? 1 : -1;
    }

    @Override
    public int compare(Term o1, Term o2) {
        return (o2.getFrequency() - o1.getFrequency()) > 0 ? above : below;
    }

}
