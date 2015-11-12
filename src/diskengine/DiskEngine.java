package diskengine;

import static constants.Constants.scheme;
import ui.GUI;
import index.IndexWriter;
import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DiskEngine {

    public static void main(String[] args) {
        while (true) {
            // select to build or read index from disk
            String[] options = {"Build Index", "Read Index"};
            int menuChoice = JOptionPane.showOptionDialog(null,
                    "Select what to do in selected directory",
                    "Menu",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (menuChoice == JOptionPane.CLOSED_OPTION) {
                System.exit(0);
            }
            // select directory
            String folder = selectDirectory();

            switch (menuChoice) {
                case 0: {   // build index
                    IndexWriter writer = new IndexWriter(folder);
                    writer.buildIndex();
                    break;
                }
                case 1: {   // read index
                    // select processing mode
                    options = new String[]{"Boolean Mode", "Ranked Mode"};
                    int modeChoice = JOptionPane.showOptionDialog(null,
                            "Select query processing mode",
                            "Menu",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]);
                    if (modeChoice == JOptionPane.CLOSED_OPTION) {
                        System.exit(0);
                    }
                    boolean mode = (modeChoice == 0);

                    if (!mode) {
                        // set default scheme for rank retrieval
                        scheme = 0;

                        // select weight scheme to be used in ranked reterival mode
                        options = new String[]{"Default", "Traditional",
                            "Okapi", "Wacky"};
                        scheme = JOptionPane.showOptionDialog(null,
                                "Select query processing mode",
                                "Menu",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);;
                        if (!(scheme >= 0 && scheme < 4)) {  // check if dialog was closed
                            System.exit(0);
                        }
                    }

                    // start GUI
                    GUI gui = new GUI(folder, mode);
                    while (!gui.isChangeIndex()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DiskEngine.class.getName()).log(Level.SEVERE, null, ex);
                            System.exit(0);
                        }
                    }
                    if (gui.isQuit()) {
                        System.exit(0);
                    }
                    gui.dispose();
                    break;
                }
                default: {
                    System.out.println("Invalid choice");
                    break;
                }
            }
        }
    }

    /**
     * pick the directory for building/reading index
     * @return Location to the selected folder
     */
    private static String selectDirectory() {
        JFileChooser directoryPicker = new JFileChooser();
        directoryPicker.setCurrentDirectory(new java.io.File(""));
        directoryPicker.setDialogTitle("Select any directory with text files");
        directoryPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryPicker.setAcceptAllFileFilterUsed(false);
        directoryPicker.setVisible(true);
        File selectedDirectory = null;
        if (directoryPicker.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = directoryPicker.getSelectedFile();
        } else {
            System.exit(0);
        }
        return Paths.get(selectedDirectory.getPath()).toAbsolutePath().toString();
    }
}
