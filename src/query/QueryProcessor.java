/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query;

import cache.CacheMemory;
import cache.Term;
import com.sun.istack.internal.NotNull;
import constants.Constants;
import index.DiskPositionalIndex;
import stemmer.PorterStemmer;
import structures.Posting;
import streamer.QueryStreamer;
import queue.PriorityQueueComparator;
import queue.MyPriorityQueue;
import util.ArrayUtility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeSet;

/**
 *
 * @author JAY
 */
public class QueryProcessor {

    private final DiskPositionalIndex index;
    private final PorterStemmer porterstemmer;
    private final QueryStreamer queryStreamer;
    private final PriorityQueue<Posting> pq;
    private final CacheMemory memory;
    private final boolean mode;
    private int numOfDiskAccess;    // number of times disk read happened

    /**
     * Query processor for given disk positional index
     *
     * @param _index disk positional index instance
     */
    public QueryProcessor(DiskPositionalIndex _index) {
        index = _index;
        porterstemmer = new PorterStemmer();
        queryStreamer = new QueryStreamer();
        mode = Constants.mode;
        if (!mode) {    // init priorityqueue only in ranked query processing
            pq = new MyPriorityQueue(Constants.maxNumOfDocsToReturn, new PriorityQueueComparator(false));
        } else {
            pq = null;
        }
        memory = new CacheMemory(Constants.cacheSize);
        numOfDiskAccess = 0;
    }

    /**
     * process the query and returns the result from the disk index. For ranked
     * mode, default return value is 10
     *
     * @param query query to be processed
     * @return query result containing the Postings
     */
    public Posting[] processQuery(String query) {
        return this.processQuery(query, Constants.maxNumOfDocsToReturn);
    }

    /**
     * process the query and returns the result from the disk index
     *
     * @param query query to be processed
     * @param initialCapacity number of elements to be returned
     * @return query result containing the Postings
     */
    public Posting[] processQuery(String query, int initialCapacity) {
        query = query.replaceAll("[^A-Za-z0-9-+/ \"]", "")
                .replaceAll("(( )( )+)", " ")
                .trim();
        String[] orSplit = query.split("\\+");
        Posting[] result = null;
        for (String subQuery : orSplit) {     // all subquery are without +
            Posting[] subResult = null;

            // generate subresult for subQuery
            queryStreamer.setQuery(subQuery);
            while (queryStreamer.hasNextToken()) {
                String token = queryStreamer.nextToken();
                if (token != null) {
                    Posting[] processToken = null;
                    if (token.contains("\"")) {   // token is positive phrase
                        if (mode) {
                            processToken = processPhrase(token);
                        } else {
                            return null;
                        }
                    } else {
                        processToken = processToken(token);
                    }
                    if (mode) {
                        subResult = ArrayUtility.and(subResult, processToken);
                    } else if (processToken != null && processToken.length > 0) {
                        int N = index.getNumberOfDocuments();
                        int dft = processToken.length;
                        for (Posting p : processToken) {
                            if (p != null) {
                                p.calculateWqt(N, dft);
                                p.calculateAd();
                                pq.offer(p);
                            }
                        }
                    }
                }
            }

            // process not-tokens in subQuery
            // (tokens can be single terms or phrase, both are negative)
            if (mode) {         // perform not operator only for boolean queries
                String[] notTokens = queryStreamer.getNotTokens();
                for (String token : notTokens) {
                    Posting[] processToken;
                    token = token.substring(1, token.length()); // remove '-'
                    if (token.contains("\"")) {   // token is positive phrase
                        processToken = processPhrase(token);
                    } else {
                        processToken = processToken(token);
                    }
                    subResult = ArrayUtility.remove(subResult, processToken);
                }

                // add to final result
                result = ArrayUtility.or(result, subResult);
            }
        }

        // make result return top K elements from queue for ranked query mode
        if (!mode) {
            int size = Math.min(initialCapacity, pq.size());
            result = new Posting[size];
            for (int i = 0; i < size; i++) {
                Posting posting = pq.poll();
                result[i] = posting;
            }
            pq.clear();
        }
        return result;
    }

    /**
     * Search index for token. Token can be positive or negative single term
     *
     * @param token Query literal
     * @return Postings for given token
     */
    private Posting[] processToken(String token) {
        token = porterstemmer.processToken(token.toLowerCase());
        Posting[] postings = memory.search(token);
        return postings != null ? postings : getPostingsFromDisk(token);
    }

    private Posting[] getPostingsFromDisk(@NotNull String token) {
        Posting[] postings = index.getPostings(token, false);
        memory.insert(new Term(token, postings));
        numOfDiskAccess++;
        return postings;
    }

    public int getNumberOfDiskAccess() {
        return numOfDiskAccess;
    }

    public void printCache() {
        memory.print();
    }

    /**
     * Search index for phrase. Phrase can be positive or negative
     *
     * @param query Phrase Query
     * @return DocIds for given phrase query
     */
    private Posting[] processPhrase(String query) {
        int nearK = 1;
        if (query.contains("NEAR/")) {
            Scanner scanner = new Scanner(query);
            query = "";
            while (scanner.hasNext()) {
                String next = scanner.next();
                if (next.contains("NEAR/")) {
                    // skip
                    nearK = Integer.parseInt(next.substring(next.indexOf("/") + 1));
                } else {
                    query = query + " " + next;
                }
            }
            query = query.trim();
        }
        HashMap<Integer, TreeSet<Long>> result = new HashMap<>();
        query = query.substring(1, query.length() - 1);     // remove quotes
        String[] tokens = query.split(" ");
        if (tokens.length > 1) {        // more than one token in phrase
            int i = 0;
            // get Postings for token1
            Posting[] postings1 = index
                    .getPostings(porterstemmer.processToken(tokens[i].toLowerCase()), true);
            // get Postings for remaining tokens as token2, at the en make token1 = token2
            for (i = 1; i < tokens.length; i++) {
                // token2's postings
                Posting[] postings2 = index
                        .getPostings(porterstemmer.processToken(tokens[i].toLowerCase()), true);

                // go through all postings of token1 and token2
                if (postings1 != null && postings2 != null) {
                    for (int j = 0, k = 0; j < postings1.length && k < postings2.length;) {
                        // compare posting1.id to posting2.id
                        int compare_value = postings1[j].getDocID() - postings2[k].getDocID();
                        if (compare_value == 0) {       // same docId
                            TreeSet<Long> search = matchPostings(postings1[j],
                                    postings2[k],
                                    nearK);
                            TreeSet<Long> postings = result
                                    .getOrDefault(postings1[j].getDocID(), new TreeSet<>());
                            postings.addAll(search);

                            result.put(postings1[j].getDocID(), postings);
                            j++;
                            k++;
                        } else if (compare_value > 0) {    // postings1[j].id > postings2[k].id
                            k++;
                        } else if (compare_value < 0) {   // postings1[j].id < postings2[k].id
                            j++;
                        }
                    }
                } else {
                    return null;
                }
                postings1 = postings2;
            }
        } else if (tokens.length == 1) {  // only one token in phrase
            return index.getPostings(porterstemmer.processToken(tokens[0].toLowerCase()), true);
        } else {  // no token
            return null;
        }
        Iterator<Integer> iterator = result.keySet().iterator();
        ArrayList<Posting> ret = new ArrayList<>(result.size());
        while (iterator.hasNext()) {
            Integer docId = iterator.next();
            Iterator<Long> positions = result.get(docId).iterator();
            Long last = null;
            int count = 0;
            while (positions.hasNext()) {
                Long next = positions.next();
                if (last == null) {
                    last = next;
                    count++;
                } else {
                    long diff = next - last;
                    if (diff >= 0 && diff <= nearK) {
                        count++;
                    } else {
                        count = 1;
                    }
                    last = next;
                }
                if (count == tokens.length) {
                    ret.add(new Posting(docId));
                    break;
                }
            }
        }
        return ret.toArray(new Posting[ret.size()]);
    }

    /**
     * Search positions where token1 and token2 are atmost nearK distance away
     *
     * @param token1 Posting for token1
     * @param token2 Posting for token2
     * @return List of positions where token1 and token2 matched the requirement
     */
    private TreeSet<Long> matchPostings(Posting token1, Posting token2, int nearK) {
        TreeSet<Long> result = new TreeSet<>();
        long[] term1_posList = token1.getPositions();
        long[] term2_posList = token2.getPositions();
        if (term1_posList.length > 0 && term2_posList.length > 0) {
            int i = 0, j = 0;
            for (; i < term1_posList.length && j < term2_posList.length;) {
                Long p1 = term1_posList[i];
                Long p2 = term2_posList[j];
                if (p2 - p1 >= 0 && p2 - p1 <= nearK) {
                    result.add(p1);
                    result.add(p2);
                    i++;
                    j++;
                } else if (p1 < p2) {
                    i++;
                } else if (p1 > p2) {
                    j++;
                }
            }
        }
        return result;
    }
}
