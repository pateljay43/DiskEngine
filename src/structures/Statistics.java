/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package structures;

import constants.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JAY
 */
public class Statistics {

    private long termCount;
    private long numOfTypes;
    private double avgDocPerTerm;
    private long totalMemory;
    private List<String> mostFreqTerms;

    /**
     * Statistics of index containing number of terms, number of types, average
     * number of documents per term, total memory used by index files, most
     * frequent terms
     */
    public Statistics() {
        mostFreqTerms = new ArrayList<>(10);    // default 10 terms
    }

    /**
     *
     * @return number of terms in index
     */
    public double getTermCount() {
        return termCount;
    }

    /**
     * set number of terms in index
     *
     * @param termCount
     */
    public void setTermCount(long termCount) {
        this.termCount = termCount;
    }

    /**
     *
     * @return number of types
     */
    public long getNumOfTypes() {
        return numOfTypes;
    }

    /**
     * set number of types
     *
     * @param numOfTypes
     */
    public void setNumOfTypes(long numOfTypes) {
        this.numOfTypes = numOfTypes;
    }

    /**
     *
     * @return average number of documents per term
     */
    public double getAvgDocPerTerm() {
        return avgDocPerTerm;
    }

    /**
     * set average number of documents per term
     *
     * @param avgDocPerTerm
     */
    public void setAvgDocPerTerm(double avgDocPerTerm) {
        this.avgDocPerTerm = avgDocPerTerm;
    }

    /**
     *
     * @return total secondary memory used by all the index files
     */
    public long getTotalMemory() {
        return totalMemory;
    }

    /**
     * set total secondary memory used by all the index files
     *
     * @param totalMemory
     */
    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    /**
     *
     * @return list of most frequent terms in index
     */
    public List<String> getMostFreqTerms() {
        return mostFreqTerms;
    }

    /**
     *
     * @return formatted string of most frequent terms in index
     */
    public String getMostFreqTermsAsString() {
        String ret = "";
        int length = Constants.mostFreqTermCount;
        for (int i = 0; i < length; i++) {
            ret = ret + mostFreqTerms.get(i) + ((i != length - 1) ? ", " : "");
        }
        return ret;
    }

    /**
     * adds most frequent term in the list
     *
     * @param term term to be added to most frequent term's list
     */
    public void addMostFreqTerm(String term) {
        mostFreqTerms.add(term);
    }
}
