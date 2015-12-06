/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cache;

import com.sun.istack.internal.NotNull;
import queue.CacheQueueComparator;
import queue.MyCacheQueue;
import structures.Posting;

/**
 *
 * @author JAY
 */
public class CacheMemory {

    private final MyCacheQueue queue;

    /**
     * creates a cache memory
     *
     * @param _size maximum number of terms to be stored in cache memory
     */
    public CacheMemory(int _size) {
        queue = new MyCacheQueue(_size, new CacheQueueComparator(true));
    }

    /**
     * search the cache memory for the given term.
     *
     * @param term term to be searched
     * @return null if term is not present in memory, else postings for the term
     */
    public Posting[] search(@NotNull String term) {
        return queue.getTerm(term);
    }

    /**
     * insert new term to memory with new frequency
     *
     * @param term term to be inserted in cache memory
     */
    public void insert(@NotNull Term term) {
        term.increaseFreqBy(1);
        queue.offer(term);
    }

    public void print() {
        int size = queue.getCapacity();
        for (int i = 0; i < size; i++) {
            Term term = queue.poll();
            System.out.println(term.getTerm() + ": " + term.getFrequency());
        }
    }
}
