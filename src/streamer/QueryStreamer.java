/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package streamer;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author JAY
 */
public class QueryStreamer implements TokenStream {

    private Scanner mReader;
    private static ArrayList<String> notToken;

    /**
     * streams any query one token at a time, and returns negative tokens at the
     * end in a list
     */
    public QueryStreamer() {
        notToken = new ArrayList<>();
    }

    /**
     * set the query to be streamed
     *
     * @param query query to be streamed
     */
    public void setQuery(String query) {
        mReader = new Scanner(query);
        notToken.clear();
    }

    @Override
    public boolean hasNextToken() {
        return mReader.hasNext();
    }

    @Override
    public String nextToken() {
        if (!hasNextToken()) {
            return null;
        }

        String next = mReader.next().trim();

        if (next.startsWith("-")) {
            if (next.contains("\"")) {
                next = getPhrase(next); // generate the token as a single phrase query
            }
            notToken.add(next); // add the phrase query to negative token list
            return nextToken();
        } else if (next.startsWith("\"")) {
            return getPhrase(next);     // generate the token as a single phrase query
        }
        return next;
    }

    /**
     * returns the phrase query starting with next
     *
     * @param next starting token of single phrase query
     * @return single phrase query
     */
    private String getPhrase(String next) {

        while (!next.endsWith("\"")) {
            String cont = mReader.next().trim();
            next = next + " " + cont;
        }
        return next;
    }

    /**
     *
     * @return all the negative tokens encountered during the parsing of tokens
     */
    public String[] getNotTokens() {
        return notToken.toArray(new String[notToken.size()]);
    }
}
