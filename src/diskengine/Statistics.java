/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

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

    public Statistics() {
        mostFreqTerms = new ArrayList<>(10);    // default 10 terms
    }

    public double getTermCount() {
        return termCount;
    }

    public void setTermCount(long termCount) {
        this.termCount = termCount;
    }

    public long getNumOfTypes() {
        return numOfTypes;
    }

    public void setNumOfTypes(long numOfTypes) {
        this.numOfTypes = numOfTypes;
    }

    public double getAvgDocPerTerm() {
        return avgDocPerTerm;
    }

    public void setAvgDocPerTerm(double avgDocPerTerm) {
        this.avgDocPerTerm = avgDocPerTerm;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public List<String> getMostFreqTerms() {
        return mostFreqTerms;
    }

    public String getMostFreqTermsAsString() {
        String ret = "";
        for (String term : mostFreqTerms) {
            ret = ret + term + ", ";
        }
        ret = ret + "\b\b";
        return ret;
    }

    public void addMostFreqTerm(String term) {
        mostFreqTerms.add(term);
    }

    public void setMostFreqTerms(List<String> mostFreqTerms) {
        this.mostFreqTerms = mostFreqTerms;
    }

}
