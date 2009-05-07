package org.cs533.asm;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author amit
 */
public class MatrixMultiplyTest {

    static int source1Rows = 2;
    static int source1Cols = 2;
    static int source2Rows = 2;
    static int source2Cols = 2;
    static int destRows = source1Rows;
    static int destCols = source2Cols;
    static int currRow = 0;
    static int currCol = 0;
    static int rCount = 0;
    static int NUM_THREADS = 10;
    final static Object lock = new Object();

    public static void main(String[] args) {
        int[] source1 = new int[source1Rows * source1Cols];
        int[] source2 = new int[source2Rows * source2Cols];
        int[] dest = new int[destRows * destCols];
        randomizeMatrix(source1);
        randomizeMatrix(source2);
        // multiplyMatrix(source1, source2, dest);
        spawnThreads(source1, source2, dest);
        printMatrix(source1);
        printMatrix(source2);
        printMatrix(dest);

    }

    public static void randomizeMatrix(int[] matrix) {
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < matrix.length; i++) {
            // matrix[i] = r.nextInt(8);
            matrix[i] = rCount++;
        }
    }

    public static void spawnThreads(final int[] source, final int[] source2, final int[] dest) {
        Thread[] tArr = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            tArr[i] = new Thread(new Runnable() {

                public void run() {
                    threadMultiply(source, source2, dest);
                }
            });
            tArr[i].start();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                tArr[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(MatrixMultiplyTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void threadMultiply(int[] source1, int[] source2, int[] dest) {
        while (true) {
            int[] nextTuple = getTuple();
            if (nextTuple == null) {
                return;
            } else {
                int destValue = 0;
                synchronized (lock) {
                    //         System.out.println("iterating with currRow = " + nextTuple[0] + " and currCol = " + nextTuple[1]);
                }
                int counter = 0;
                while (true) {
                    int rowValue = source1[nextTuple[0] * source1Cols + counter];
                    int colValue = source2[nextTuple[1] + (counter * source2Cols)];
                    destValue += rowValue * colValue;
                    counter++;
                    if (counter == source1Rows) {
                        break;
                    }
                }
                dest[(nextTuple[0] * destCols) + nextTuple[1]] = destValue;
            }
        }
    }

    private static int[] getTuple() {
        int[] toReturn = new int[2];
        synchronized (lock) {
            toReturn[0] = currRow;
            toReturn[1] = currCol;
            currCol++;
            if (currCol >= source2Cols) {
                currRow++;
                if (currRow >= source1Rows) {
                    //unlock then branch to halt
                    return null; // branch to halt
                } else {
                    currCol = 0;
                }

            }
            System.out.println("SEt currRow to " + currRow + " and col to " + currCol);
            System.out.flush();
        }
        return toReturn;
    }

    private static void multiplyMatrix(int[] source1, int[] source2, int[] dest) {
        while (true) {
            while (true) {
                int destValue = 0;
                System.out.println("iterating with currRow = " + currRow + " and currCol = " + currCol);
                int counter = 0;
                while (true) {
                    int rowValue = source1[currRow * source1Cols + counter];
                    int colValue = source2[currCol + (counter * source2Cols)];
                    destValue += rowValue * colValue;
                    counter++;
                    if (counter == source1Rows) {
                        break;
                    }
                }
                dest[(currRow * destCols) + currCol] = destValue;
                currRow++;
                if (currRow == source1Rows) {
                    break;
                }
            }
            currRow = 0;
            currCol++;
            if (currCol == source2Cols) {
                break;
            }
        }

    }

    private static void printMatrix(int[] matrix) {
        int counter = 0;
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(matrix[i] + ",");
        }
        System.out.println();
    }
}
