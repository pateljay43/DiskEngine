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
    public static String docWeightFile = "docWeights.bin.bin";
    public static String vocabTableFile = "vocabTable.bin";
    public static String indexStatFile = "indexStatistics.bin";

    /**
     * scheme to be used for ranked retrieval 0 - default, 1 - traditional, 2 -
     * okapi, 3 - wacky
     */
    public static int scheme = 1;
    
    // top 'mostFreqTermCount' terms encountered while indexing
    public static int mostFreqTermCount = 10;
    
    // number of documents to be returned in rank retrieval mode 
    public static int maxNumOfDocsToReturn = 10;
}
