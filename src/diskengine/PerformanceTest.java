/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import index.DiskPositionalIndex;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import query.QueryProcessor;
import query.QuerySyntaxCheck;

/**
 *
 * @author JAY
 */
public class PerformanceTest {

    private final QueryProcessor queryProcessor;
    private final QuerySyntaxCheck syntaxChecker;
    private final DiskPositionalIndex index;
    private final DecimalFormat df2;

    public PerformanceTest(String folder) {
        index = new DiskPositionalIndex(folder);
        queryProcessor = new QueryProcessor(index);
        syntaxChecker = new QuerySyntaxCheck();
        df2 = new DecimalFormat("#.##");
    }

    public void start(String[] queries, int[] randomNums) {
        System.out.println("#_Queries: " + randomNums.length);
        long sTime = System.nanoTime();
        for (int r : randomNums) {
            String query = queries[r];
            if (syntaxChecker.isValidQuery(query)) {       // invalid query
                queryProcessor.processQuery(query);
            }
        }
        System.out.println(BigDecimal.valueOf(((double) System.nanoTime() - sTime) / 1000000000)
                + " seconds");
        System.out.println("Number of disk access: " + queryProcessor.getNumberOfDiskAccess());
        queryProcessor.printCache();
        JOptionPane.showMessageDialog(null,
                "#_Queries: " + randomNums.length + "\n"
                + BigDecimal.valueOf(((double) System.nanoTime() - sTime) / 1000000000)
                + " seconds" + "\n"
                + "Number of disk access: " + queryProcessor.getNumberOfDiskAccess(),
                "Test Result", JOptionPane.INFORMATION_MESSAGE
        );
    }
}
