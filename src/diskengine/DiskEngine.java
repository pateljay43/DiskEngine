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
                System.out.println("/Users/JAY/Desktop/529 - SET/project/MobyDick10Chapters");
//                String folder = scan.nextLine();
                String folder = "/Users/JAY/Desktop/529 - SET/project/MobyDick10Chapters";

                IndexWriter writer = new IndexWriter(folder);
                writer.buildIndex();
                break;
            }
            case 2: {
                System.out.println("Enter the name of an index to read:");
                System.out.println("/Users/JAY/Desktop/529 - SET/project/MobyDick10Chapters");
//                String indexName = scan.nextLine();
                String indexPath = "/Users/JAY/Desktop/529 - SET/project/MobyDick10Chapters";

//                DiskInvertedIndex index = new DiskInvertedIndex(indexName);
                DiskPositionalIndex index = new DiskPositionalIndex(indexPath);
                QueryProcessor queryProcessor2 = new QueryProcessor(index);

                while (true) {
                    System.out.println("Enter one or more search terms, separated "
                            + "by spaces:");
                    String input = scan.nextLine();

                    if (input.equals("EXIT")) {
                        break;
                    }

                    Posting[] postingsList = queryProcessor2.processQuery(input);
                            
                    if (postingsList == null || postingsList.length == 0) {
                        System.out.println("Term not found");
                    } else {
                        System.out.print("Docs: ");
                        for (Posting post : postingsList) {
//                            System.out.print(index.getFileNames().get(post.getDocID())
//                                    + ":"
//                                    + index.getWeight(post.getDocID())
//                                    + " ");
                            System.out.print(index.getFileNames().get(post.getDocID()) + " ");
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
