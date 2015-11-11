/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queue;

import structures.Posting;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author JAY
 */
public class MyPriorityQueue extends PriorityQueue<Posting> {

    private final Set<Integer> list;

    public MyPriorityQueue(int initialCapacity, PriorityQueueComparator comp) {
        super(initialCapacity, comp);
        list = new HashSet<>(initialCapacity);
    }

    @Override
    public boolean offer(Posting e) {
        int docId_e = e.getDocID();
        if (list.contains(docId_e)) {        // increase Ad of existing element in queue
            Iterator<Posting> it = this.iterator();
            while (it.hasNext()) {
                Posting list_e = it.next();
                if (list_e.getDocID() == docId_e) {     // element with same docId exists in queue
                    int newtf = list_e.getTf() + e.getTf();
                    double newAd = list_e.getAd() + e.getAd();
                    it.remove();
                    e.setTf(newtf);
                    e.setAd(newAd);      // Increase Ad's value by adding both element's value
                    super.offer(e);
                    return true;
                }
            }
        }

        // no element from queue matched 'e' -> add e
        super.offer(e);
        list.add(docId_e);
        return true;
    }

    @Override
    public void clear() {
        super.clear();
    }

}
