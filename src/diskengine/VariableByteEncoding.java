package diskengine;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import static java.lang.StrictMath.log;
import java.util.ArrayList;
import java.util.List;
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
}
