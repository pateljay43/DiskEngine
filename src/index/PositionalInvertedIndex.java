/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JAY
 */
public class PositionalInvertedIndex {

    // <term,<docID,<p1,p2,...,pn>>>
    private final HashMap<String, TreeMap<Integer, ArrayList<Integer>>> mIndex;
    //private final HashMap<String, List<PositionalPosting>>

    /**
     * creates new mIndex which stores terms with document in which it occurs
     * and positions where it occurs in that document
     */
    public PositionalInvertedIndex() {
        mIndex = new HashMap<>();
    }

    /**
     * adds new term to positional inverted index with its position in given
     * documentId
     *
     * @param term term to be referred in dictionary
     * @param documentID documentId to be referred in dictionary for given term
     * @param position position to be added in dictionary for given term and
     * documentId
     */
    public void addTerm(String term, int documentID, int position) {
        TreeMap<Integer, ArrayList<Integer>> postings = mIndex.getOrDefault(term, new TreeMap<>());
        ArrayList<Integer> positionalList = postings.getOrDefault(documentID, new ArrayList());
        if (positionalList.isEmpty() || positionalList.get(positionalList.size() - 1) < position) {
            positionalList.add(position);
            postings.put(documentID, positionalList);
            mIndex.put(term, postings);
        }
    }

    /**
     * gets postings for given term
     *
     * @param term term whose postings to be returned
     * @return postings containing list of documents paired with list of
     * positions (<document,[position1,..]>)
     */
    public TreeMap<Integer, ArrayList<Integer>> getPostings(String term) {
        TreeMap<Integer, ArrayList<Integer>> postings = mIndex.getOrDefault(term, new TreeMap<>());
        return postings;
    }

    /**
     * gets positional list for given term and documentId
     *
     * @param term term whose postings to be searched for documentId
     * @param documentId Id whose positional list will be returned
     * @return list of positions
     */
    public List<Integer> getPositionalList(String term, int documentId) {
        return mIndex.get(term).get(documentId);
    }

    /**
     *
     * @return number of terms in dictionary
     */
    public int getTermCount() {
        return mIndex.size();
    }

    /**
     *
     * @return return sorted list of terms in dictionary
     */
    public String[] getDictionary() {
        String[] terms = mIndex.keySet().toArray(new String[mIndex.size()]);
        Arrays.sort(terms);
        return terms;
    }

    /**
     * find k most frequent terms from the index
     *
     * @param k must be greater than 1
     * @return list of top k most occurring terms in index
     */
    public String[] getMostFrequentTerms(int k) {
        List<String> temp = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            int maxSize = 0;
            String maxSize_Key = null;
            Iterator<String> keys = mIndex.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                int numOfDocs = mIndex.get(key).size();
                if (numOfDocs > maxSize && !temp.contains(key)) {
                    maxSize = numOfDocs;
                    maxSize_Key = key;
                }
            }
            if (maxSize_Key != null) {
                temp.add(i, maxSize_Key);
            }
        }
        return temp.toArray(new String[k]);
    }
}
