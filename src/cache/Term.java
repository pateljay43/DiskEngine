/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cache;

import com.sun.istack.internal.NotNull;
import structures.Posting;

/**
 *
 * @author JAY
 */
public class Term {

    private final String term;
    int frequency;
    Posting[] postings;

    public Term(@NotNull String _term, @NotNull Posting[] _postings) {
        term = _term;
        postings = _postings;
    }

    public final String getTerm() {
        return term;
    }

    public final int getFrequency() {
        return frequency;
    }

    public final Posting[] getPostings() {
        return postings;
    }

    public final void setFrequency(int _frequency) {
        frequency = _frequency;
    }

    public final void increaseFreqBy(int increment) {
        frequency += increment;
    }

}
