/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diskengine;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JAY
 */
public class ArrayUtility {

    /**
     * AND intersection of list1 and list2. If one of the list is null, then
     * other list is returned
     *
     * @param list1
     * @param list2
     * @return list generated after intersecting list1 and list2.
     */
    public static Posting[] and(Posting[] list1, Posting[] list2) {
        if (list1 == null && list2 == null) {
            return new Posting[0];
        } else if (list1 == null) {
            return list2;
        } else if (list2 == null) {
            return list1;
        }
        List<Posting> result = new ArrayList<>(Math.max(list1.length, list2.length));
        for (int i = 0, j = 0; i < list1.length && j < list2.length;) {
            int compare_value = compare_Id(list1[i], list2[j]);
            if (compare_value == 0) {   // add Posting to result
                result.add(list1[i]);
                i++;
                j++;
            } else if (compare_value == 1) {   // list1.id > list2.id
                j++;
            } else if (compare_value == -1) {     // list1.id < list2.id
                i++;
            }
        }
        return result.toArray(new Posting[result.size()]);
    }

    /**
     * OR union of list1 and list2. If one of the list is null, then other list
     * is returned
     *
     * @param list1
     * @param list2
     * @return list generated after union of list1 and list2.
     */
    public static Posting[] or(Posting[] list1, Posting[] list2) {
        if (list1 == null && list2 == null) {
            return new Posting[0];
        } else if (list1 == null) {
            return list2;
        } else if (list2 == null) {
            return list1;
        }
        List<Posting> result = new ArrayList<>(list1.length + list2.length);
        int i = 0, j = 0;
        for (; i < list1.length && j < list2.length;) {
            int compare_value = compare_Id(list1[i], list2[j]);
            if (compare_value == 0) {   // add Posting to result
                result.add(list1[i]);
                i++;
                j++;
            } else if (compare_value == 1) {   // list1.id > list2.id
                result.add(list2[j]);
                j++;
            } else if (compare_value == -1) {     // list1.id < list2.id
                result.add(list1[i]);
                i++;
            }
        }
        if (i != list1.length) {      // add all remaining posting from list1 to result
            while (i < list1.length) {
                result.add(list1[i]);
                i++;
            }
        }
        if (j != list2.length) {   // add all remaining posting from list2 to result
            while (j < list2.length) {
                result.add(list2[j]);
                j++;
            }
        }
        return result.toArray(new Posting[result.size()]);
    }

    /**
     * Remove all elements of notList from list1. If list1 is null, returns
     * null. is returned
     *
     * @param list1 list to be filtered with notList
     * @param notList list to be removed from list1
     * @return list generated after NOT operation.
     */
    public static Posting[] remove(Posting[] list1, Posting[] notList) {
        if (notList == null && list1 != null) {
            return list1;
        } else if (list1 == null) {
            return new Posting[0];
        }
        List<Posting> result = new ArrayList<>(list1.length);
        int i = 0, j = 0;
        for (; i < list1.length && j < notList.length;) {
            int compare_value = compare_Id(list1[i], notList[j]);
            if (compare_value == 0) {   // add Posting to result
                i++;
                j++;
            } else if (compare_value == 1) {   // list1.id > notList.id
                j++;
            } else if (compare_value == -1) {     // list1.id < notList.id
                result.add(list1[i]);
                i++;
            }
        }
        if (i != list1.length) {      // add all remaining posting from list1 to result
            while (i < list1.length) {
                result.add(list1[i]);
                i++;
            }
        }
        return result.toArray(new Posting[result.size()]);
    }

    /**
     * compare postings instance based on their docIdslist1
     *
     * @param a
     * @param b
     * @return 0 if same; 1 if (x.id > y.id), else (x.id < y.id) -1
     */
    public static int compare_Id(Posting a, Posting b) {
        int docID1 = a.getDocID();
        int docID2 = b.getDocID();
        if (docID1 > docID2) {
            return 1;
        } else if (docID1 < docID2) {
            return -1;
        }
        return 0;
    }
}
