/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query;

import constants.Constants;

/**
 *
 * @author metehan
 */
public class QuerySyntaxCheck {

    private String errorMessage = "valid";

    /**
     * returns a success or error message for the parenthesization of the query
     *
     * @param query query to be checked for proper use of parenthesis
     * @return error message if parenthesis are not properly used
     */
    public String checkParenthesis(String query) {
        boolean isParenthesis = true;
        errorMessage = "valid";
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (c == '(') {
                if (isParenthesis) {
                    isParenthesis = false;
                } else {
                    errorMessage = "There shouldn't be nested parenthesis";
                    return errorMessage;
                }
            } else if (c == ')') {
                if (isParenthesis) {
                    errorMessage = "Invalid parenthesization";
                    return errorMessage;
                } else {
                    isParenthesis = true;
                }
            }
        }
        if (!isParenthesis) {
            errorMessage = "Invalid parenthesization";
        }
        return errorMessage;
    }

    /**
     * checks if query has quotes properly closed
     *
     * @param query query to be checked for proper use of quotes
     * @return error message if quotes are not properly used
     */
    public String checkQuotes(String query) {
        errorMessage = "valid";
        int i, count = 0;
        for (i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (count == 2 || (count == 1 && ("" + c).matches("[+)(-]"))) {
                break;
            } else if (c == '"') {
                count++;
            }
        }
        if (count % 2 != 0 || i != query.length()) {
            errorMessage = "Double Quotes not used correctly";
        } else if (query.contains("\"") && query.length() == 2) {
            errorMessage = "Empty Quotes";
        }
        return errorMessage;
    }

    /**
     * returns a success or error message that reports if query contains no
     * empty sequence of literals
     *
     * @param query query to be checked for empty query literals
     * @return error message if query contains empty literals
     */
    public String checkNoEmptyQ(String query) {
        query = query.replaceAll("[^A-Za-z0-9-+ \"]", "");
        errorMessage = "valid";
        if (query.endsWith("+") || query.endsWith("-")) {
            errorMessage = "Query contains an empty sequence of literals";
            return errorMessage;
        }
        if (query.contains("+")) {
            String[] split = query.split("\\+");
            for (String queryLiteral : split) {
                queryLiteral = queryLiteral.trim();
                if (!(queryLiteral.length() > 0)) {
                    errorMessage = "Query contains an empty sequence of literals";
                    return errorMessage;
                }
            }
        }
        return errorMessage;
    }

    /**
     * returns a success or error message that reports if each Q_i has at least
     * one positive literal
     *
     * @param query query to be checked for at least one positive query literal
     * @return error message if query does not contains at least one positive
     * query literal
     */
    public String checkOnePosLit(String query) {
        boolean isPos = true;
        errorMessage = "valid";
        query = query.replaceAll("[^A-Za-z0-9-+)( \"]", "");
        // get Q_i: sequence of literals
        String[] split = query.split("\\+");
        for (String queryLiteralSeq : split) {
            if (!isPos) {
                errorMessage = "Each sequence of literals must contain at least one positive literal";
                return errorMessage;
            }
            isPos = false;
            if (queryLiteralSeq.contains("-")) {
                String[] splitLiteralSeq = queryLiteralSeq.split("\\s+");
                for (String queryLiteral : splitLiteralSeq) {
                    // check if there is a positive literal
                    if (!queryLiteral.contains("-") && queryLiteral.trim().length() > 0) {
                        isPos = true;
                        break;
                    }
                }
            } else if (queryLiteralSeq.trim().length() > 0) {
                isPos = true;
            }
        }
        if (!isPos) {
            errorMessage = "Each sequence of literals must contain at least one positive literal";
            return errorMessage;
        }
        return errorMessage;
    }

    /**
     * check if near operator used properly or not used at all
     *
     * @param query query to be checked for proper use of operator
     * @return error message if operator are not properly used
     */
    public String checkNearOperator(String query) {
        errorMessage = "valid";
        String s = "NEAR/";
        if (query == null) {
            return errorMessage;
        } else {
            if (query.contains(s)) {
                int index = query.indexOf(s);
                if (index <= 1 || query.length() < index + 8) {
                    errorMessage = "Invalid usage of NEAR operator!";
                    return errorMessage;
                }
                char c = query.charAt(index + 5);
                if ((c < '0' || c > '9')) {
                    errorMessage = "k should be a positive integer in NEAR operator!";
                    return errorMessage;
                }
            }
        }
        return errorMessage;
    }

    /**
     * true if query passed all five syntax checking conditions
     *
     * @param query query to be checked.
     * @return true if the query is valid; else false.
     */
    public boolean isValidQuery(String query) {
        if (query == null || query.length() == 0 || !query.matches("[A-Za-z0-9-+ \"]*")) {
            errorMessage = "unidentified symbols used";
            return false;
        }
        if (!Constants.mode) {
            if (query.contains("\"") || query.contains("+") || query.contains("-")) {
                errorMessage = "You cannot use quotes/operators in ranked mode!";
                return false;
            }
            String message0 = checkNearOperator(query);
            if (!message0.equals("valid")) {
                return false;
            }
        }
        String message1 = checkParenthesis(query);
        if (!message1.equals("valid")) {
            return false;
        }
        String message2 = checkNoEmptyQ(query);
        if (!message2.equals("valid")) {
            return false;
        }
        String message3 = checkOnePosLit(query);
        if (!message3.equals("valid")) {
            return false;
        }
        String message4 = checkQuotes(query);
        if (!message4.equals("valid")) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return error message generated while validation of query
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
