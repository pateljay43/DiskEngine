/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author JAY
 */
public class QueryStreamer implements TokenStream {

    private Scanner mReader;
    private static ArrayList<String> notToken;

    public QueryStreamer() {
        notToken = new ArrayList<>();
    }

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
//        next = next.replaceAll("[^A-Za-z0-9-\"]", "");

        if (next.startsWith("-")) {
            if (next.contains("\"")) {
                next = getPhrase(next);
            }
            notToken.add(next);
            return nextToken();
        } else if (next.startsWith("\"")) {
            return getPhrase(next);
        }
        return next;
    }

    private String getPhrase(String next) {

        while (!next.endsWith("\"")) {
            String cont = mReader.next().trim();
            next = next + " " + cont;
        }
        return next;
    }

    public String[] getNotTokens() {
        return notToken.toArray(new String[notToken.size()]);
    }
}
