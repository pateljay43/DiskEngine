package index;

import constants.Constants;
import stemmer.PorterStemmer;
import streamer.SimpleTokenStream;
import util.VariableByteEncoding;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes an inverted indexing of a directory to disk.
 */
public class IndexWriter {

    private final String mFolderPath;
    private final PorterStemmer porterStemmer;
    private final Set<String> termList;
    private long totalDocFreq;

    /**
     * Constructs an IndexWriter object which is prepared to index the given
     * folder.
     *
     * @param folderPath path to folder containing files
     */
    public IndexWriter(String folderPath) {
        mFolderPath = folderPath;
        porterStemmer = new PorterStemmer();
        termList = new HashSet<>();
        totalDocFreq = 0;
    }

    /**
     * Builds and writes an inverted index to disk. Creates three files:
     * vocab.bin, containing the vocabulary of the corpus; postings.bin,
     * containing the postings list of document IDs; vocabTable.bin, containing
     * a table that maps vocab terms to postings locations
     */
    public void buildIndex() {
        buildIndexForDirectory(mFolderPath);
    }

    /**
     * Builds the normal NaiveInvertedIndex for the folder.
     */
    private void buildIndexForDirectory(String folder) {
//        NaiveInvertedIndex index = new NaiveInvertedIndex();
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        // delete old "docWeights.bin" if already exists
        createDocWeightsFile(folder);

        // Index the directory using a naive index
        indexFiles(folder, index);

        

        // at this point, "index" contains the in-memory inverted index 
        // now we save the index to disk, building three files: the postings index,
        // the vocabulary list, and the vocabulary table.
        // the array of terms
        String[] dictionary = index.getDictionary();
        System.out.println("Number of terms: " + dictionary.length);
        // an array of positions in the vocabulary file
        // each element will hold position (long) where term starts in vocab.bin
        long[] vocabPositions = new long[dictionary.length];

        buildVocabFile(folder, dictionary, vocabPositions);
        buildPostingsFile(folder, index, dictionary, vocabPositions);
        
        // write index statistics to "indexStatistics.bin" file
        writeIndexStatistics(index);
    }

    /**
     * Builds the postings.bin file for the indexed directory, using the given
     * NaiveInvertedIndex of that directory.
     */
    private static void buildPostingsFile(String folder, PositionalInvertedIndex index,
            String[] dictionary, long[] vocabPositions) {
        FileOutputStream postingsFile = null;
        try {
            postingsFile = new FileOutputStream(
                    new File(folder, Constants.postingFile)
            );

            // simultaneously build the vocabulary table on disk, mapping a term index to a
            // file location in the postings file.
            FileOutputStream vocabTable = new FileOutputStream(
                    new File(folder, Constants.vocabTableFile)
            );

            // the first thing we must write to the vocabTable file is the number of vocab terms.
            byte[] tSize = ByteBuffer.allocate(4)
                    .putInt(dictionary.length).array();
            vocabTable.write(tSize, 0, tSize.length);
            int vocabI = 0;
            for (String s : dictionary) {
                // for each String in dictionary, retrieve its postings.
                TreeMap<Integer, ArrayList<Integer>> postings = index.getPostings(s);

                /*  write the vocab table entry for this term: the 8 byte location 
                 of the term in the vocab list file,
                 and the 8 byte location of the postings for the term in the postings file.*/
                byte[] vPositionBytes = ByteBuffer.allocate(8)
                        .putLong(vocabPositions[vocabI]).array();
                vocabTable.write(vPositionBytes, 0, vPositionBytes.length);

                byte[] pPositionBytes = ByteBuffer.allocate(8)
                        .putLong(postingsFile.getChannel().position()).array();
                vocabTable.write(pPositionBytes, 0, pPositionBytes.length);

                /* write the postings file for this term. 
                 first, the document frequency for the term, then
                 the document IDs, encoded as gaps, then term frequency
                 in this document, then positions where term occurs in this doc
                 as gaps*/
                byte[] docFreqBytes = ByteBuffer.allocate(4)
                        .putInt(postings.size()).array();
//                byte[] docFreqBytes = VariableByteEncoding.encodeNumber(postings.size());
                postingsFile.write(docFreqBytes, 0, docFreqBytes.length);

                int lastDocId = 0;
                Set<Integer> docIds = postings.keySet();
                for (int docId : docIds) {
                    // write encoded docId as gap
                    byte[] docIdBytes = VariableByteEncoding.encodeNumber(docId - lastDocId);
                    postingsFile.write(docIdBytes, 0, docIdBytes.length);
                    lastDocId = docId;

                    // list of positions
                    List<Integer> positionalList = postings.get(docId);
                    // write encoded term-frequency
                    int tf = positionalList.size();
                    byte[] termFreqBytes = VariableByteEncoding.encodeNumber(tf);
                    postingsFile.write(termFreqBytes, 0, termFreqBytes.length);

                    int lastPos = 0;
                    for (int pos : positionalList) {
//                        byte[] position = ByteBuffer.allocate(4).putInt(pos - lastPos).array();
                        byte[] position = VariableByteEncoding.encodeNumber(pos - lastPos);

                        postingsFile.write(position, 0, position.length);
                        lastPos = pos;
                    }
                }
                vocabI++;
            }
            vocabTable.close();
            postingsFile.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                postingsFile.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void buildVocabFile(String folder, String[] dictionary,
            long[] vocabPositions) {
        OutputStreamWriter vocabList = null;
        try {
            // first build the vocabulary list: a file of each vocab word concatenated together.
            // also build an array associating each term with its byte location in this file.
            int vocabI = 0;
            vocabList = new OutputStreamWriter(
                    new FileOutputStream(new File(folder, Constants.vocabFile)), "ASCII"
            );

            int vocabPos = 0;
            for (String vocabWord : dictionary) {
                // for each String in dictionary, save the byte position where that 
                // term will start in the vocab file.
                vocabPositions[vocabI] = vocabPos;
                vocabList.write(vocabWord); // then write the String
                vocabI++;
                vocabPos += vocabWord.length();
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } finally {
            try {
                vocabList.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    private static int mDocumentID = 0;

    private void indexFiles(String folder, final PositionalInvertedIndex index) {
        final Path currentWorkingPath = Paths.get(folder).toAbsolutePath();

        try {
            Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {

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
                        // we have found a .txt file; add its name to the fileName list,
                        // then index the file and increase the document ID counter.
                        // System.out.println("Indexing file " + file.getFileName());

                        indexFile(file.toFile(), index, mDocumentID);
                        mDocumentID++;
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

            // write average Ld;
            double Ld_sum = 0.0;
            try (RandomAccessFile mDocWeights = new RandomAccessFile(
                    new File(folder, Constants.docWeightFile), "r")) {
                byte[] buffer = new byte[24];
                for (int i = 0; i < mDocumentID; i++) {
                    mDocWeights.read(buffer, 0, buffer.length);
                    Ld_sum = Ld_sum + ByteBuffer.wrap(buffer, 0, 8).getDouble();
                }
            }
            try (FileOutputStream docWeightFile = new FileOutputStream(
                    new File(mFolderPath, Constants.docWeightFile),
                    true
            )) {
                double avgLd = Ld_sum / mDocumentID;
                byte[] LdBytes = ByteBuffer.allocate(8)
                        .putDouble(avgLd).array();
                docWeightFile.write(LdBytes, 0, LdBytes.length);
            }
        } catch (IOException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void indexFile(File fileName, PositionalInvertedIndex index,
            int documentID) {
        try (FileOutputStream docWeightFile = new FileOutputStream(
                new File(mFolderPath, Constants.docWeightFile), true);) {
            SimpleTokenStream stream = new SimpleTokenStream(fileName);
            int position = 0;
            Set<String> terms = new HashSet<>();
            while (stream.hasNextToken()) {
                String term = stream.nextToken();
                termList.add(term);
                String t = "";
                if (term.contains("-")) { // process term with '-'
                    // for ab-xy -> store (abxy, ab, xy) all three
                    // all with same position
                    t = porterStemmer.processToken(term.replaceAll("-", ""));
                    index.addTerm(t, documentID, position);
                    totalDocFreq++;
                    String[] subtokens = term.split("-");
                    for (String subtoken : subtokens) {
                        if (subtoken.length() > 0) {
                            index.addTerm(porterStemmer.processToken(subtoken),
                                    documentID, position);
                            totalDocFreq++;
                        }
                    }
                } else {
                    t = porterStemmer.processToken(term);
                    index.addTerm(t, documentID, position);
                    totalDocFreq++;
                }
                terms.add(t);
                position++;
            }

            // add all tf for terms in this document
            double sumOfWdt_2 = 0.0;
            long sum_tf = 0;
            for (String term : terms) {
                int tf = index.getPositionalList(term, documentID).size();
                sum_tf = sum_tf + tf;
                sumOfWdt_2 = sumOfWdt_2 + Math.pow((1 + ((tf == 0) ? 0 : Math.log(tf))), 2);
            }

            // write Ld
            double Ld = (sumOfWdt_2 == 0.0) ? 0.0 : Math.sqrt(sumOfWdt_2);
            byte[] LdBytes = ByteBuffer.allocate(8)
                    .putDouble(Ld).array();
            docWeightFile.write(LdBytes, 0, LdBytes.length);

            // write byteSize
            byte[] byteSize = ByteBuffer.allocate(8)
                    .putDouble(fileName.length()).array();
            docWeightFile.write(byteSize, 0, byteSize.length);

            // write avg(tf) for this document
            double numOfTerms = terms.size();
            byte[] avgTf = ByteBuffer.allocate(8)
                    .putDouble(sum_tf / numOfTerms).array();
            docWeightFile.write(avgTf, 0, avgTf.length);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private void writeIndexStatistics(PositionalInvertedIndex index) {
        try (FileOutputStream indexStatFile = new FileOutputStream(
                new File(mFolderPath, Constants.indexStatFile));) {

            // write
            // // term count
            long numOfTerms = index.getTermCount();
            byte[] buffer = ByteBuffer.allocate(8)
                    .putLong(numOfTerms).array();
            indexStatFile.write(buffer, 0, buffer.length);
            // // number of type of terms
            buffer = ByteBuffer.allocate(8)
                    .putLong(termList.size()).array();
            indexStatFile.write(buffer, 0, buffer.length);
            // // average number of documents per term
            buffer = ByteBuffer.allocate(8)
                    .putDouble(totalDocFreq / index.getTermCount()).array();
            indexStatFile.write(buffer, 0, buffer.length);
            // // total memory of all files used on secondary memory
            long totalSecondaryMemory = 0;
            RandomAccessFile file = new RandomAccessFile(new File(mFolderPath, Constants.vocabFile), "r");
            totalSecondaryMemory += file.length();
            file.close();
            file = new RandomAccessFile(new File(mFolderPath, Constants.postingFile), "r");
            totalSecondaryMemory += file.length();
            file.close();
            file = new RandomAccessFile(new File(mFolderPath, Constants.docWeightFile), "r");
            totalSecondaryMemory += file.length();
            file.close();
            file = new RandomAccessFile(new File(mFolderPath, Constants.vocabTableFile), "r");
            totalSecondaryMemory += file.length();
            file.close();
            buffer = ByteBuffer.allocate(8)
                    .putLong(totalSecondaryMemory).array();
            indexStatFile.write(buffer, 0, buffer.length);

            // // 10 most frequent terms
            String[] terms = index.getDictionary();
            ArrayList<String> temp = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                int maxSize = 0;
                String maxSize_term = null;
                String term;
                for (int j = i; j < numOfTerms; j++) {
                    term = terms[j];
                    int numOfDocs = index.getPostings(term).size();
                    if (numOfDocs > maxSize && !temp.contains(term)) {
                        maxSize = numOfDocs;
                        maxSize_term = term;
                    }
                }
                if (maxSize_term != null) {
                    temp.add(i, maxSize_term);
                }
            }
            String[] mostFreqTerms = temp.toArray(new String[temp.size()]);
            // // // write all the terms to file as [num of byte of term, term]
            for (String term : mostFreqTerms) {
                byte[] termByte = term.getBytes();
                byte[] termByteLength = ByteBuffer.allocate(termByte.length)
                        .putInt(termByte.length).array();
                indexStatFile.write(termByteLength, 0, termByteLength.length);
                indexStatFile.write(termByte, 0, termByte.length);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createDocWeightsFile(String folder) {
        try (FileOutputStream docWeightFile = new FileOutputStream(
                new File(folder, Constants.docWeightFile));) {
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
