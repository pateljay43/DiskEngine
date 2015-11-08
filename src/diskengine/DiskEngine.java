package diskengine;

import java.util.Scanner;

public class DiskEngine {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.println("Menu:");
        System.out.println("1) Build index");
        System.out.println("2) Read and query index");
        System.out.println("Choose a selection:");
        int menuChoice = scan.nextInt();
        scan.nextLine();

        switch (menuChoice) {
            case 1: {
                System.out.println("Enter the name of a directory to index: ");
                System.out.println("/Users/JAY/Desktop/529 - SET/Home works contents/MobyDickChapters");
//                String folder = scan.nextLine();
                String folder = "/Users/JAY/Desktop/529 - SET/Home works contents/MobyDickChapters";

                IndexWriter writer = new IndexWriter(folder);
                writer.buildIndex();
                break;
            }
            case 2: {
                System.out.println("Enter the name of an index to read:");
                System.out.println("/Users/JAY/Desktop/529 - SET/Home works contents/MobyDickChapters");
//                String indexName = scan.nextLine();
                String indexPath = "/Users/JAY/Desktop/529 - SET/Home works contents/MobyDickChapters";

                System.out.println("Select mode of operation:");
                System.out.println("1) Boolean mode");
                System.out.println("2) Ranked mode\n");
//                boolean mode = (scan.nextInt() == 1);

//                DiskInvertedIndex index = new DiskInvertedIndex(indexName);
                DiskPositionalIndex index = new DiskPositionalIndex(indexPath);
                QueryProcessor queryProcessor = new QueryProcessor(index);

                while (true) {
                    System.out.println("Enter one or more search terms, separated "
                            + "by spaces:");
                    String input = scan.nextLine();

                    if (input.equals("EXIT")) {
                        break;
                    }

//                    Posting[] postingsList = queryProcessor.processQuery(input, mode);
                    Posting[] postingsList = queryProcessor.processQuery(input, false, 10);

                    if (postingsList == null || postingsList.length == 0) {
                        System.out.print("Term not found");
                    } else {
                        System.out.println("Docs: ");
                        for (Posting posting : postingsList) {
//                            System.out.print(index.getFileNames().get(post.getDocID())
//                                    + ":"
//                                    + index.getWeight(post.getDocID())
//                                    + " ");
                            System.out.println(index.getFileNames().get(posting.getDocID()) + ": "
                                    + posting.getAd());
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
