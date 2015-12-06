/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queue;

import cache.Term;
import com.sun.istack.internal.NotNull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import structures.Posting;

/**
 *
 * @author JAY
 */
public class MyCacheQueue extends PriorityQueue<Term> {

    // list of terms present in queue at any time
    private final HashMap<String, Term> list;
    private final int maxCapacity;

    /**
     * Priority queue pre sorted based on the accumulator value of each element
     *
     * @param initialCapacity initial capacity of the queue
     * @param comp comparator to be use for this queue
     */
    public MyCacheQueue(int initialCapacity, CacheQueueComparator comp) {
        super(initialCapacity, comp);
        maxCapacity = initialCapacity;
        list = new HashMap<>(initialCapacity);
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
    public boolean offer(Term e) {
        String term = e.getTerm();
        if (this.contains(term)) {      // add frequency to term already in the queue
            increaseFreqBy(term, e.getFrequency());
        } else {
            if (this.size() == maxCapacity) {   // remove least frequent term
                Term remove = this.poll();
                list.remove(remove.getTerm());
            }
            super.offer(e);
            list.put(term, e);
        }
        return true;
    }

    public Posting[] getTerm(@NotNull String term) {
        Term t = list.get(term);
        if (t != null) {
            increaseFreqBy(term, 1);
            Posting[] postings = t.getPostings();
            return postings;
        }
        return null;
    }

    public void increaseFreqBy(String term, int increment) {
        Iterator<Term> it = this.iterator();
        while (it.hasNext()) {
            Term t = it.next();
            if (t.getTerm().equals(term)) {
                it.remove();
                t.increaseFreqBy(increment);
                list.put(term, t);
                super.offer(t);
                break;
            }
        }
    }

    @Override
    public boolean contains(Object o) {
        return list.containsKey((String) o); //To change body of generated methods, choose Tools | Templates.
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

    public int getCapacity() {
        return Math.min(this.size(), maxCapacity);
    }

}
