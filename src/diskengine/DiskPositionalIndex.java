/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAY
 */
public class DiskPositionalIndex {

    private RandomAccessFile mVocabList;
    private RandomAccessFile mPostings;
    private RandomAccessFile mDocWeights;
    private long[] mVocabTable;
    private List<String> mFileNames;

    public DiskPositionalIndex(String path) {
        try {
            mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
            mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
            mDocWeights = new RandomAccessFile(new File(path, "docWeights.bin"), "r");
            mVocabTable = readVocabTable(path);
            mFileNames = readFileNames(path);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    private Posting[] readPostingsFromFile(RandomAccessFile postings,
            long postingsPosition, boolean phrase) {
        try {
            // seek to the position in the file where the postings start.
            postings.seek(postingsPosition);

            // read the 4 bytes for the document frequency
            byte[] buffer = new byte[4];
            postings.read(buffer, 0, buffer.length);

            // use ByteBuffer to convert the 4 bytes into an int.
            int documentFrequency = ByteBuffer.wrap(buffer).getInt();

            // initialize the array that will hold the postings. 
            Posting[] postingsArray = new Posting[documentFrequency];

            // write the following code:
            // read 4 bytes at a time from the file, until you have read as many
            //    postings as the document frequency promised.
            // 
            // skip the positions of term by term frequency
            //    
            //
            // after each read, convert the bytes to an int posting. this value
            //    is the GAP since the last posting. decode the document ID from
            //    the gap and put it in the array.
            //
            // repeat until all postings are read.
            buffer = new byte[8];   // for wdt (double)
            int lastDocId = 0;
            for (int i = 0; i < documentFrequency; i++) {
                postingsArray[i] = new Posting();

                // read docID
                byte b = postings.readByte();
                int n = 0, gap;
                while (true) {
                    if ((b & 0xff) < 128) {     // not the last byte - leading bit '0'
                        n = 128 * n + b;
                        b = postings.readByte();
                    } else {        // last byte - leading bit '1'
                        gap = (128 * n + ((b - 128) & 0xff));
                        break;
                    }
                }
                int docId = lastDocId + gap;
                postingsArray[i].setDocID(docId);
                lastDocId = docId;

                // read wdt
                postings.read(buffer, 0, buffer.length);
                double wdt = ByteBuffer.wrap(buffer).getDouble();
                postingsArray[i].setWdt(wdt);

                // read term frequency of document
                b = postings.readByte();
                n = 0;
                int tf = 0;
                while (true) {
                    if ((b & 0xff) < 128) {     // not the last byte - leading bit '0'
                        n = 128 * n + b;
                        b = postings.readByte();
                    } else {        // last byte - leading bit '1'
                        tf = (128 * n + ((b - 128) & 0xff));
                        break;
                    }
                }
                postingsArray[i].setTf(tf);

                // initialize positions array
                postingsArray[i].initPositions();

                // read positions
                long lastPosition = 0;
                for (int j = 0; j < tf; j++) {
                    b = postings.readByte();
                    n = 0;
                    gap = 0;
                    while (true) {
                        if ((b & 0xff) < 128) {     // not the last byte - leading bit '0'
                            n = 128 * n + b;
                            b = postings.readByte();
                        } else {        // last byte - leading bit '1'
                            gap = (128 * n + ((b - 128) & 0xff));
                            break;
                        }
                    }
                    if (phrase) {       // store positions only for phrase query
                        long pos = lastPosition + gap;
                        postingsArray[i].setPosition(pos, j);
                        lastPosition = pos;
                    }
                }
            }
            return postingsArray;
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    public Posting[] getPostings(String term, boolean phrase) {
        long postingsPosition = binarySearchVocabulary(term);
        if (postingsPosition >= 0) {
            return readPostingsFromFile(mPostings, postingsPosition, phrase);
        }
        return null;
    }

    private long binarySearchVocabulary(String term) {
        // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
        int i = 0, j = mVocabTable.length / 2 - 1;
        while (i <= j) {
            try {
                int m = (i + j) / 2;
                long vListPosition = mVocabTable[m * 2];
                int termLength;
                if (m == mVocabTable.length / 2 - 1) {
                    termLength = (int) (mVocabList.length() - mVocabTable[m * 2]);
                } else {
                    termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
                }

                mVocabList.seek(vListPosition);

                byte[] buffer = new byte[termLength];
                mVocabList.read(buffer, 0, termLength);
                String fileTerm = new String(buffer, "ASCII");

                int compareValue = term.compareTo(fileTerm);
                if (compareValue == 0) {
                    // found it!
                    return mVocabTable[m * 2 + 1];
                } else if (compareValue < 0) {
                    j = m - 1;
                } else {
                    i = m + 1;
                }
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return -1;
    }

    private List<String> readFileNames(String path) {
        try {
            final List<String> names = new ArrayList<String>();
            final Path currentWorkingPath = Paths.get(path).toAbsolutePath();

            Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
                int mDocumentID = 0;

                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) {
                    // make sure we only process the current working directory
                    if (currentWorkingPath.equals(dir)) {
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                }

                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) {
                    // only process .txt files
                    if (file.toString().endsWith(".txt")) {
                        // just save file names not absoulte path to file
                        names.add(file.toFile().getName());
                    }
                    return FileVisitResult.CONTINUE;
                }

                // don't throw exceptions if files are locked/other errors occur
                public FileVisitResult visitFileFailed(Path file,
                        IOException e) {

                    return FileVisitResult.CONTINUE;
                }
            });
            return names;
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    private long[] readVocabTable(String indexName) {
        try {
            long[] vocabTable;

            RandomAccessFile tableFile = new RandomAccessFile(
                    new File(indexName, "vocabTable.bin"),
                    "r");

            byte[] byteBuffer = new byte[4];
            // read number of terms in tableFile
            tableFile.read(byteBuffer, 0, byteBuffer.length);

            int tableIndex = 0;
            // each element in vocabTable is 8 byte
            // length will be number of terms * 2 
            // (one element for term location in vocab.bin, another for location in postings.bin)
            vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];

            byteBuffer = new byte[8];

            while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 8 bytes
                vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
                tableIndex++;
            }
            tableFile.close();
            return vocabTable;
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    public double getWeight(int docId) {
        byte[] buffer = new byte[8];
        try {
            mDocWeights.seek(0);
            for (int i = 0; i < docId; i++) {
                mDocWeights.read(buffer, 0, buffer.length);
            }
            mDocWeights.read(buffer, 0, buffer.length);
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ByteBuffer.wrap(buffer).getDouble();
    }

    public List<String> getFileNames() {
        return mFileNames;
    }

    public int getNumberOfDocuments() {
        return mFileNames.size();
    }

    public int getTermCount() {
        return mVocabTable.length / 2;
    }
}
