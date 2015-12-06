/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;

/**
 *
 * @author JAY
 */
public class Constants {

    // File names
    public static String vocabFile = "vocab.bin";
    public static String postingFile = "postings.bin";
    public static String docWeightFile = "docWeights.bin";
    public static String vocabTableFile = "vocabTable.bin";
    public static String indexStatFile = "indexStatistics.bin";

    /**
     * scheme to be used for ranked retrieval 0 - default, 1 - traditional, 2 -
     * okapi, 3 - wacky
     */
    public static int scheme = 0;

    // top 'mostFreqTermCount' terms encountered while indexing
    public static int mostFreqTermCount = 10;

    // number of documents to be returned in rank retrieval mode 
    public static int maxNumOfDocsToReturn = 10;

    // read mode; true - boolean, false - rank
    public static boolean mode = true;

    // max size of cache memory to store terms and its result
    public static int cacheSize = 20;

    // test search engine for performance
    public static boolean test = true;
}
