/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
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
    private String mostFreqTerms;
    private double avgDocPerTerm;
    private final DecimalFormat df2;

    private int largestWord;

    // Variables for statistics
    private int totalDocumentsFrequency;
    private int numOfDocuments;
    private int totalMemory;

    /**
     * creates new mIndex which stores terms with document in which it occurs
     * and positions where it occurs in that document
     */
    public PositionalInvertedIndex() {
        mIndex = new HashMap<>();
        largestWord = 0;
        df2 = new DecimalFormat("#.##");
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
        largestWord = Math.max(largestWord, term.length());
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
//        ArrayList<Integer> list = new ArrayList<>();
//        list.addAll(keySet);
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
     *
     * @return length of largest term in dictionary
     */
    public int getLargestWordLength() {
        return largestWord;
    }

    /**
     * @return the totalMemory
     */
    public int getTotalMemory() {
        return totalMemory;
    }

    /**
     * @return the totalDocumentsFrequency
     */
    public int getTotalDocumentsFrequency() {
        return totalDocumentsFrequency;
    }

    /**
     * calculate statistics of index and find 'mostFreqLimit' terms in that
     * index
     *
     * @param mostFreqLimit
     */
    public void indexFinalize(int mostFreqLimit) {
        int totalStrMem = 0;
        int totalPostListMem = 0;
        int totalPostMem = 0;
        totalDocumentsFrequency = 0;

        // set of all terms
        Set keys = mIndex.keySet();

        // calculate total hash memory
        int hashMem = 24 + 36 * mIndex.size();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            // calculate string memory
            int strMem = 40;
            strMem = strMem + 2 * key.length();
            totalStrMem = totalStrMem + strMem;

            //calculate total postings list memory
            totalPostListMem = totalPostListMem + 24 + 8 * mIndex.get(key).size();

            //calculate total posting memory
            // set of all docID for 'key' term
            Set docKeys = mIndex.get(key).keySet();
            Iterator docItr = docKeys.iterator();
            while (docItr.hasNext()) {
                int docKey = (Integer) docItr.next();
                totalPostMem = totalPostMem + 48 + 4 * mIndex.get(key).get(docKey).size();
                totalDocumentsFrequency++;
            }
        }
        totalMemory = hashMem + totalStrMem + totalPostListMem + totalPostMem;
        avgDocPerTerm = ((double) totalDocumentsFrequency) / mIndex.size();
        mostFrequentTerms(mostFreqLimit);
    }

    /**
     * find k most frequent terms from the index
     *
     * @param k must be greater than 1
     */
    public void mostFrequentTerms(int k) {
        mostFreqTerms = "";
        ArrayList<String> temp = new ArrayList<>();
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
        temp.stream().forEach((key) -> {
            mostFreqTerms = mostFreqTerms + "\t<" + key + ", "
                    + df2.format((double) mIndex.get(key).size() / numOfDocuments) + ">\n";
        });
    }

    public String getMostFreqTerms() {
        return mostFreqTerms;
    }

    public double getAvgDocPerTerm() {
        return avgDocPerTerm;
    }

    public void setNumOfDocuments(int numOfDocuments) {
        this.numOfDocuments = numOfDocuments;
    }

}
