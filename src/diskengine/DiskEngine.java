package diskengine;

import java.util.Scanner;

public class DiskEngine {

    /**
     * 1 - default, 2 - traditional, 3 - okapi, 4 - wacky
     */
    public static int scheme = 1;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.println("Menu:");
        System.out.println("1) Build index");
        System.out.println("2) Read and query index");
        System.out.println("Choose a selection:");
        int menuChoice = scan.nextInt();
        scan.nextLine();
        System.out.println("");
        String folder = "/Users/JAY/Desktop/529 - SET/all corpus/angels";
//        String folder = "/Users/JAY/Desktop/529 - SET/all corpus/MobyDick10Chapters";
//        String folder = "/Users/JAY/Desktop/529 - SET/all corpus/MobyDickChapters";
//        String folder = "/Users/JAY/Desktop/529 - SET/all corpus/mycorpus";
        switch (menuChoice) {
            case 1: {
                System.out.println("Enter the name of a directory to index: ");
                System.out.println("" + folder);

                IndexWriter writer = new IndexWriter(folder);
                writer.buildIndex();
                break;
            }
            case 2: {
                System.out.println("Enter the name of an index to read:");
                System.out.println("" + folder + "\n");

                System.out.println("Select mode of operation:");
                System.out.println("1) Boolean mode");
                System.out.println("2) Ranked mode");
                boolean mode = (scan.nextInt() == 1);
                scan.nextLine();
                System.out.println("");

                if (!mode) {
                    System.out.println("Select weighting scheme:");
                    System.out.println("1) Default");
                    System.out.println("2) Traditional");
                    System.out.println("3) Okapi");
                    System.out.println("4) Wacky");
                    int choice = scan.nextInt();
                    scan.nextLine();
                    if (choice > 0 && choice < 5) {
                        scheme = choice;
                    } else {
                        System.out.println("Incorrect choice");
                        System.exit(0);
                    }
                    System.out.println("");
                }

                DiskPositionalIndex index = new DiskPositionalIndex(folder);
                QueryProcessor queryProcessor = new QueryProcessor(index);

                while (true) {
                    System.out.println("Enter one or more search terms, separated "
                            + "by spaces:");
                    String input = scan.nextLine();

                    if (input.equals("EXIT")) {
                        break;
                    }

                    // process query 'input' with mode, (optional) K element if ranked mode is selected
                    // default K value is 10 for ranked query mode
                    Posting[] postingsList = queryProcessor.processQuery(input, mode);

                    if (postingsList == null || postingsList.length == 0) {
                        System.out.print("Term not found");
                    } else {
                        System.out.println("Docs: ");
                        if (mode) {
                            for (Posting posting : postingsList) {
                                System.out.print(index.getFileNames().get(posting.getDocID()) + " ");
                            }
                            System.out.println("");
                        } else {
                            for (Posting posting : postingsList) {
                                System.out.println(index.getFileNames().get(posting.getDocID()) + ": "
                                        + posting.getAd());
                            }
                        }

                    }
                    System.out.println();
                    System.out.println();
                }

                break;
            }
            default: {
                System.out.println("Invalid choice");
                break;
            }
        }
    }
}
