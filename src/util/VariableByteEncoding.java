package util;

import java.io.IOException;
import java.io.RandomAccessFile;
import static java.lang.StrictMath.log;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Hamot
 */
public class VariableByteEncoding {

    /**
     * encodes the integer n using variable byte encoding
     *
     * @param n integer to be encoded
     * @return array of encoded bytes
     */
    public static byte[] encodeNumber(int n) {
        if (n == 0) {
            return new byte[]{(byte) 128};
        }
        int i = (int) (log(n) / log(128)) + 1;
        byte[] rv = new byte[i];
        int j = i - 1;
        do {
            rv[j--] = (byte) (n % 128);
            n /= 128;
        } while (j >= 0);
        rv[i - 1] += 128;
        return rv;
    }

    public static int decodeNumber(final RandomAccessFile postings) {
        int num = 0;
        try {
            int b = postings.readByte();
            int n = 0;
            while (true) {
                if ((b & 0xff) < 128) {     // not the last byte - leading bit '0'
                    n = 128 * n + b;
                    b = postings.readByte();
                } else {        // last byte - leading bit '1'
                    num = (128 * n + ((b - 128) & 0xff));
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(VariableByteEncoding.class.getName()).log(Level.SEVERE, null, ex);
        }
        return num;
    }
}
