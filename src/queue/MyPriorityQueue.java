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

    // list of docIds present in queue at any time
    private final Set<Integer> list;

    /**
     * Priority queue pre sorted based on the accumulator value of each element
     *
     * @param initialCapacity initial capacity of the queue
     * @param comp comparator to be use for this queue
     */
    public MyPriorityQueue(int initialCapacity, PriorityQueueComparator comp) {
        super(initialCapacity, comp);
        list = new HashSet<>(initialCapacity);
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param e element to be inserted in the queue
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws ClassCastException if the specified element cannot be compared
     * with elements currently in this priority queue according to the priority
     * queue's ordering
     * @throws NullPointerException if the specified element is null
     */
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

    /**
     * Removes all of the elements from this priority queue. The queue will be
     * empty after this call returns.
     */
    @Override
    public void clear() {
        super.clear();
        list.clear();
    }

}
