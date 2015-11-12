/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package index;

import constants.Constants;
import structures.Posting;
import structures.Statistics;
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
    private Statistics statistics;
    private List<String> mFileNames;

    /**
     * Read the binary files containing index information for directory 'path'
     *
     * @param path path to the binary files as well as text documents
     */
    public DiskPositionalIndex(String path) {
        try {
            mVocabList = new RandomAccessFile(new File(path, Constants.vocabFile), "r");
            mPostings = new RandomAccessFile(new File(path, Constants.postingFile), "r");
            mDocWeights = new RandomAccessFile(new File(path, Constants.docWeightFile), "r");
            mVocabTable = readVocabTable(path);
            mFileNames = readFileNames(path);
            statistics = readStatistics(path);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * read the postings from the file
     *
     * @param postings file containing postings
     * @param postingsPosition position from which reading should be started
     * @param phrase true - keep the positions in the returned structure, else
     * ignore the positions
     * @return array of Posting structure (optionally positions and scheme)
     */
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

            int lastDocId = 0;
            double averageWeight = getAverageWeight();
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

                // set [Ld, byteSize, avg(tf)]
                // calculate Ld and wdt
                double[] weights = getWeight(docId);
                postingsArray[i].setScheme(weights[0], weights[1], weights[2], averageWeight);
            }
            return postingsArray;
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    /**
     * retrieve the postings for the given term
     *
     * @param term to be searched in index table
     * @param phrase is the term part of phrase query?
     * @return array of Posting structure (optionally positions and scheme)
     */
    public Posting[] getPostings(String term, boolean phrase) {
        long postingsPosition = binarySearchVocabulary(term);
        if (postingsPosition >= 0) {
            return readPostingsFromFile(mPostings, postingsPosition, phrase);
        }
        return null;
    }

    /**
     * search for the location mapped with the given term
     *
     * @param term terms whose location is to be searched
     * @return location mapping the postings with given term
     */
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

    /**
     * read file names with .txt extension in the given path
     *
     * @param path location to be searched for .txt files
     * @return list of filenames in path directory
     */
    private List<String> readFileNames(String path) {
        try {
            final List<String> names = new ArrayList<>();
            final Path currentWorkingPath = Paths.get(path).toAbsolutePath();

            Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
                int mDocumentID = 0;

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) {
                    // make sure we only process the current working directory
                    if (currentWorkingPath.equals(dir)) {
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
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
                @Override
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

    /**
     * read vocab table containing mapping of <term_location,postings_location>
     *
     * @param path path to the directory containing vocabTable file
     * @return array containing
     * [term1_location,postings1_location,term2_location,postings2_location,....]
     */
    private long[] readVocabTable(String path) {
        try (RandomAccessFile tableFile = new RandomAccessFile(
                new File(path, Constants.vocabTableFile), "r");) {
            long[] vocabTable;

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
            return vocabTable;
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    /**
     * returns Ld, byteSize, avg(tf) stored in docWeights.bin
     *
     * @param docId id of document
     * @return array containing [Ld, byteSize, avg(tf)]
     */
    private double[] getWeight(int docId) {
        byte[] buffer = new byte[24];
        try {   // skip all Ld, byteSize, avg(tf) for each document
            mDocWeights.seek(docId * 24);
            mDocWeights.read(buffer, 0, buffer.length);
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        double ld = ByteBuffer.wrap(buffer, 0, 8).getDouble();
        double byteSize = ByteBuffer.wrap(buffer, 8, 8).getDouble();
        double avg_tf = ByteBuffer.wrap(buffer, 16, 8).getDouble();
        return new double[]{ld, byteSize, avg_tf};
    }

    /**
     *
     * @return Average weight of all document
     */
    public double getAverageWeight() {
        byte[] buffer = new byte[8];
        try {   // average is stored after all the Ld, byteSize, avg(tf) for each document
            mDocWeights.seek(mFileNames.size() * 24);
            mDocWeights.read(buffer, 0, buffer.length);
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ByteBuffer.wrap(buffer).getDouble();
    }

    /**
     *
     * @return list of filenames which are indexed
     */
    public List<String> getFileNames() {
        return mFileNames;
    }

    public int getNumberOfDocuments() {
        return mFileNames.size();
    }

    public int getTermCount() {
        return mVocabTable.length / 2;
    }

    /**
     * read statistics of indexed files
     *
     * @param path path to the file containing pre computed index statistics
     * @return structure containing index statistics
     */
    private Statistics readStatistics(String path) {
        Statistics stat;
        try (RandomAccessFile indexStat = new RandomAccessFile(new File(path, Constants.indexStatFile), "r");) {
            stat = new Statistics();

            byte[] buffer = new byte[32];
            indexStat.read(buffer, 0, buffer.length);
            // read term count
            long termCount = ByteBuffer.wrap(buffer, 0, 8).getLong();
            stat.setTermCount(termCount);
            // read number of type of terms
            long numOfTypes = ByteBuffer.wrap(buffer, 8, 8).getLong();
            stat.setNumOfTypes(numOfTypes);
            // read average number of documents per term
            double avg = ByteBuffer.wrap(buffer, 16, 8).getDouble();
            stat.setAvgDocPerTerm(avg);
            // read total memory of all files used on secondary memory
            long mem = ByteBuffer.wrap(buffer, 24, 8).getLong();
            stat.setTotalMemory(mem);
            // read all the terms to file as [num of byte of term, term]

            for (int i = 0; i < Constants.mostFreqTermCount; i++) {
                // number of bytes of this term
                int bytelength = indexStat.readInt();
                buffer = new byte[bytelength];
                // read term
                indexStat.read(buffer, 0, buffer.length);
                // add term to statistics
                stat.addMostFreqTerm(new String(buffer));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return stat;
    }

    /**
     *
     * @return statistics of current index
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     *
     * @param docId document id
     * @return file name mapped to given docId
     */
    public String getFileName(int docId) {
        return mFileNames.get(docId);
    }
}
