/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import constants.Constants;
import structures.Posting;
import structures.Statistics;
import index.DiskPositionalIndex;
import query.QuerySyntaxCheck;
import query.QueryProcessor;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JAY
 */
public class GUI extends JFrame implements MouseListener, KeyListener {

    private DecimalFormat df2;
    private DiskPositionalIndex index;
    private QueryProcessor queryProcessor;
    private QuerySyntaxCheck syntaxChecker;
    private Posting[] queryResult;
    private JTextField queryTF;
    private JButton searchBtn;
    private JButton newDirectoryBtn;
    private JTable Jtable;
    private TableModel tableModel;
    private JScrollPane tableScrollPane;
    private JLabel processingTimeLBL;
    private JLabel processingTime;
    private JLabel numOfDocumentsLBL;
    private JLabel numOfDocuments;
    private JLabel indexStatisticsLBL;
    private String folder;
    private int guiHeightOffset;
    private boolean quit;
    private boolean changeIndex;
    private static int queryHistoryPointer;
    private static HashMap<String, Posting[]> queryHistory;
    private static ArrayList<String> queryHistoryArray;

    public GUI(String _currentWorkingPath) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            guiHeightOffset = 30;
        } else {
            guiHeightOffset = 20;
        }
        quit = false;
        folder = _currentWorkingPath;
        changeIndex = false;
        try {
            index = new DiskPositionalIndex(folder);
            queryProcessor = new QueryProcessor(index);
            syntaxChecker = new QuerySyntaxCheck();
            queryHistory = new HashMap<>();
            queryHistoryArray = new ArrayList<>();
            queryHistoryPointer = 0;
            df2 = new DecimalFormat("#.##");

            indexStatisticsLBL = new JLabel("Index Statistics:-  Press (Ctrl + i)");
            indexStatisticsLBL.setBounds(15, 5, 250, 30);
            add(indexStatisticsLBL);

            newDirectoryBtn = new JButton("Change Folder");
            newDirectoryBtn.setBounds(670, 7, 120, 25);
            newDirectoryBtn.addActionListener((ActionEvent e) -> {
                setChangeIndex();
            });
            newDirectoryBtn.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), "ctrl+i");
            newDirectoryBtn.getActionMap().put("ctrl+i", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showStatistics();
                }
            });
            add(newDirectoryBtn);

            queryTF = new JTextField();
            queryTF.setBounds(10, 40, 650, 25);
            queryTF.addKeyListener(this);
            queryTF.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), "ctrl+i");
            queryTF.getActionMap().put("ctrl+i", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showStatistics();
                }
            });
            add(queryTF);

            searchBtn = new JButton("Search");
            searchBtn.setBounds(670, 40, 120, 25);
            searchBtn.addActionListener((ActionEvent e) -> {
                if (startQueryProcessor(true, System.nanoTime())) {
                    queryHistoryPointer = queryHistory.size() - 1;
                }
            });
            searchBtn.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), "ctrl+i");
            searchBtn.getActionMap().put("ctrl+i", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showStatistics();
                }
            });
            add(searchBtn);

            tableModel = new TableModel();
            Jtable = new JTable(tableModel);
            Jtable.setGridColor(Color.gray);
            Jtable.setShowVerticalLines(false);
            Jtable.addMouseListener(this);
            Jtable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), "ctrl+i");
            Jtable.getActionMap().put("ctrl+i", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showStatistics();
                }
            });
            Jtable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
            Jtable.getActionMap().put("enter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int row = ((JTable) e.getSource()).getSelectedRow();
                    openFile(row);
                }
            });
            tableScrollPane = new JScrollPane(Jtable,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            tableScrollPane.setBounds(10, 75, 780, 183);
            add(tableScrollPane);

            processingTimeLBL = new JLabel("Processing Time: ");
            processingTimeLBL.setBounds(15, 250, 150, 30);
            add(processingTimeLBL);

            processingTime = new JLabel("0.0 milliseconds");
            processingTime.setBounds(140, 250, 150, 30);
            add(processingTime);

            numOfDocumentsLBL = new JLabel("Number Of Documents: ");
            numOfDocumentsLBL.setBounds(450, 250, 150, 30);
            add(numOfDocumentsLBL);

            numOfDocuments = new JLabel("0");
            numOfDocuments.setBounds(610, 250, 100, 30);
            add(numOfDocuments);

            setTitle("Enter your search query");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(800, 70 + guiHeightOffset);
            setResizable(false);
            setLayout(null);
            setLocationRelativeTo(null);
            setVisible(true);

            queryTF.requestFocus();
        } catch (Exception ex) {
            ex.printStackTrace();
            changeIndex = true;
        }
    }

    /**
     * starts processing the query
     *
     * @param showErrors show errors in the query syntax
     * @param sTime time when the processing was requested
     * @return true - query processing executed without errors, else false
     */
    private boolean startQueryProcessor(boolean showErrors, long sTime) {
        String query = queryTF.getText().trim();
        if (query == null || query.length() == 0) {
            return false;
        }
        if (showErrors && queryHistory.containsKey(query)) {
            queryResult = queryHistory.get(query);
            if (queryResult != null) {
                addToHistory(query);
                showResultPanel(showErrors);
                processingTime.setText(BigDecimal.valueOf(((double) System.nanoTime() - sTime) / 1000000)
                        + " milliseconds");
                numOfDocuments.setText(queryResult.length + "");
                System.out.println("From Cache mem");
                return true;
            }
        }
        boolean ret = false;
        // check if query syntax is not valid
        if (!syntaxChecker.isValidQuery(query)) {       // invalid query
            hideResultPanel();
            if (showErrors) {
                JOptionPane.showMessageDialog(this,
                        syntaxChecker.getErrorMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {        // valid query
            if (showErrors && query.equals("EXIT")) {
                // 0 -> yes, 1 -> no, -1 -> dialog closed
                int n = JOptionPane.showOptionDialog(this,
                        "Would you like quit search application?",
                        "Exit Application",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (n == 0) {
                    changeIndex = true;
                    quit = true;
                    this.dispose();
                }
                queryTF.setText("");
                return false;
            }
            // add result to queryResult;
            queryResult = queryProcessor.processQuery(query);
            if (queryResult != null && queryResult.length > 0) {
                tableModel.fireTableDataChanged();
                showResultPanel(showErrors);
                if (showErrors) {
                    processingTime.setText(BigDecimal.valueOf(((double) System.nanoTime() - sTime) / 1000000)
                            + " milliseconds");
                    numOfDocuments.setText(queryResult.length + "");
                    addToHistory(query);
                }
            } else {        // no result
                hideResultPanel();
                if (showErrors) {
                    JOptionPane.showMessageDialog(this,
                            "No documents satisfies that query..!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
            ret = true;
        }
        return ret;
    }

    /**
     * show result panel, optionally with processing time and number of
     * documents
     *
     * @param show show processing time and number of documents in the query
     * result
     */
    public final void showResultPanel(boolean show) {
        if (!show && queryHistory.containsKey(queryTF.getText())) {
            show = true;
        }
        processingTime.setVisible(show);
        processingTimeLBL.setVisible(show);
        numOfDocuments.setVisible(show);
        numOfDocumentsLBL.setVisible(show);
        setSize(800, 70 + 210 + guiHeightOffset);
        setLocationRelativeTo(null);
    }

    /**
     * hide the panel showing result documents
     */
    public final void hideResultPanel() {
        if (queryHistory.containsKey(queryTF.getText())) {
            return;
        }
        setSize(800, 70 + guiHeightOffset);
        setLocationRelativeTo(null);
    }

    /**
     * hide the panel showing result documents
     */
    private void setChangeIndex() {
        this.setVisible(false);
        changeIndex = true;
    }

    /**
     * opens the text file in OS default text editor
     *
     * @param row row pointer to the document to be opened
     */
    private void openFile(int row) {
        Desktop desktop = Desktop.getDesktop();
        String fileURI = folder + "/" + Jtable.getValueAt(row, 0);
        try {
            desktop.open(new File(fileURI));
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return true if user clicked change index button, else false.
     */
    public boolean isChangeIndex() {
        return changeIndex;
    }

    /**
     *
     * @return true if user typed "EXIT" as query, else false;
     */
    public boolean isQuit() {
        return quit;
    }

    /**
     * adds the query to history
     *
     * @param query query to be added to the history
     */
    private void addToHistory(String query) {
        if (queryHistory.containsKey(query)) {
            queryHistoryArray.add(queryHistoryArray.remove(queryHistoryArray.indexOf(query)));
        } else {
            queryHistory.put(query, queryResult);
            queryHistoryArray.add(query);
            queryHistoryPointer = queryHistory.size() - 1;
        }
    }

    /**
     * # of Terms, # of Types, Average number of documents per term, 10 most
     * frequent terms, Approximate total memory required by index
     */
    private void showStatistics() {
        Statistics stat = index.getStatistics();
        JOptionPane.showMessageDialog(this,
                "Number of Terms: " + stat.getTermCount() + "\n"
                + "Number of Types (distinct tokens): " + stat.getNumOfTypes() + "\n"
                + "Average number of documents per term: " + df2.format(stat.getAvgDocPerTerm()) + "\n"
                + "Approximate total secondary memory (MB): "
                + df2.format(stat.getTotalMemory() / Math.pow(1024.00, 2)) + "\n"
                + "10 most frequent words: \n\n"
                + stat.getMostFreqTermsAsString(),
                "Index Statistics", JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Invoked when a key has been pressed. See the class description for
     * {@link KeyEvent} for a definition of a key pressed event.
     *
     * @param e textfield which generated this event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {     // user pressed ENTER key
            if (startQueryProcessor(true, System.nanoTime())) {
                queryHistoryPointer = queryHistory.size() - 1;
            }
        } else if (key == KeyEvent.VK_ESCAPE) {     // pressed ESCAPE key
            ((JTextField) e.getSource()).setText("");
            queryHistoryPointer = queryHistory.size();
            hideResultPanel();
        } else if (key == KeyEvent.VK_UP) {     // pressed up arrow key
            if (queryHistoryPointer > 0) {
                long sTime = System.nanoTime();
                queryHistoryPointer--;
                String query = queryHistoryArray.get(queryHistoryPointer);
                ((JTextField) e.getSource()).setText(query);
                queryResult = queryHistory.getOrDefault(query, new Posting[0]);
                if (queryResult.length > 0) {
                    showResultPanel(true);
                    processingTime.setText(BigDecimal
                            .valueOf(((double) System.nanoTime() - sTime) / 1000000)
                            + " milliseconds");
                    numOfDocuments.setText(queryResult.length + "");
                }
            }
        } else if (key == KeyEvent.VK_DOWN) {       // pressed down arrow key
            if (queryHistoryPointer < queryHistory.size() - 1) {
                long sTime = System.nanoTime();
                queryHistoryPointer++;
                String query = queryHistoryArray.get(queryHistoryPointer);
                ((JTextField) e.getSource()).setText(query);
                queryResult = queryHistory.getOrDefault(query, new Posting[0]);
                if (queryResult.length > 0) {
                    showResultPanel(true);
                    processingTime.setText(BigDecimal
                            .valueOf(((double) System.nanoTime() - sTime) / 1000000)
                            + " milliseconds");
                    numOfDocuments.setText(queryResult.length + "");
                }
            }
        }
    }

    /**
     * Invoked when a key has been released. See the class description for
     * {@link KeyEvent} for a definition of a key released event.
     *
     * @param e textfield which generated this event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        String text = ((JTextField) e.getSource()).getText();
        if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE) { // pressed delete/back space key
            if (text.equals("")) {
                queryHistoryPointer = queryHistory.size();
                hideResultPanel();
//            } else if (text.length() >= 3) {
//                startQueryProcessor(false, System.nanoTime());
            }
        } else if (key != KeyEvent.VK_ENTER && key != KeyEvent.VK_ESCAPE) { // pressed any key but enter/escape
//            if (text.length() >= 3) {
//                startQueryProcessor(false, System.nanoTime());
//            } else if (text.length() < 3) {
//                hideResultPanel();
//            }
        }
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a component.
     *
     * @param e button which generated this event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = ((JTable) e.getSource()).getSelectedRow();
            openFile(row);
        }
    }

    class TableModel extends AbstractTableModel {

        private final String[] columnNames;
        private final Class[] columnClass;

        /**
         * table model to list documents and optionally rank
         */
        public TableModel() {
            if (Constants.mode) { // boolean
                columnNames = new String[]{"File Name"};
                columnClass = new Class[]{String.class};
            } else {    // rank
                columnNames = new String[]{"File Name", "Accumulator"};
                columnClass = new Class[]{String.class, String.class};
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column]; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnClass[columnIndex];
        }

        @Override
        public int getRowCount() {
            return (queryResult == null) ? 0 : queryResult.length;
        }

        @Override
        public int getColumnCount() {
            return columnClass.length;
        }

        @Override
        public String getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {     // file name
                return index.getFileName(queryResult[rowIndex].getDocID());
            } else {  // accumulator
                return "" + queryResult[rowIndex].getAd();
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
