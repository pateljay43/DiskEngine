package diskengine;

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

    /**
     * Constructs an IndexWriter object which is prepared to index the given
     * folder.
     *
     * @param folderPath path to folder containing files
     */
    public IndexWriter(String folderPath) {
        mFolderPath = folderPath;
        porterStemmer = new PorterStemmer();
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
        try {
            FileOutputStream docWeightFile = new FileOutputStream(
                    new File(folder, "docWeights.bin")
            );
            docWeightFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

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
                    new File(folder, "postings.bin")
            );

            // simultaneously build the vocabulary table on disk, mapping a term index to a
            // file location in the postings file.
            FileOutputStream vocabTable = new FileOutputStream(
                    new File(folder, "vocabTable.bin")
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
                    int tf = positionalList.size();

                    // write wdt = 1 + log(tf)
                    byte[] wdt = ByteBuffer.allocate(8).putDouble(1
                            + ((tf == 0) ? 0 : Math.log(tf))).array();
                    postingsFile.write(wdt, 0, wdt.length);

                    // write encoded term-frequency
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
                    new FileOutputStream(new File(folder, "vocab.bin")), "ASCII"
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

    private void indexFiles(String folder, final PositionalInvertedIndex index) {
        final Path currentWorkingPath = Paths.get(folder).toAbsolutePath();

        try {
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
        } catch (IOException ex) {
            Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void indexFile(File fileName, PositionalInvertedIndex index,
            int documentID) {
        FileOutputStream docWeightFile = null;
        try {
            SimpleTokenStream stream = new SimpleTokenStream(fileName);
            int position = 0;
            HashMap<String, Double> term_freq = new HashMap<>();
            while (stream.hasNextToken()) {
                String term = stream.nextToken();
                if (term.contains("-")) { // process term with '-'
                    // for ab-xy -> store (abxy, ab, xy) all three
                    // all with same position
                    index.addTerm(porterStemmer.processToken(term.replaceAll("-", "")),
                            documentID, position);
                    String[] subtokens = term.split("-");
                    for (String subtoken : subtokens) {
                        if (subtoken.length() > 0) {
                            index.addTerm(porterStemmer.processToken(subtoken),
                                    documentID, position);
                        }
                    }
                } else {
                    index.addTerm(porterStemmer.processToken(term), documentID, position);
                }
                term_freq.put(term, (term_freq.getOrDefault(term, 0.0) + 1));
                position++;
            }
            Collection<Double> tfs = term_freq.values();
            Double sum_wdt = 0.0;
            for (Double tf : tfs) {
                sum_wdt = sum_wdt + Math.pow((1 + ((tf == 0) ? 0 : Math.log(tf))), 2);
            }
            double Ld = Math.sqrt(sum_wdt);
            docWeightFile = new FileOutputStream(
                    new File(mFolderPath, "docWeights.bin"),
                    true
            );
            byte[] LdBytes = ByteBuffer.allocate(8)
                    .putDouble(Ld).array();
            docWeightFile.write(LdBytes, 0, LdBytes.length);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            try {
                docWeightFile.close();
            } catch (IOException ex) {
                Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
