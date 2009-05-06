package org.cs533.asm;


import java.util.Random;

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

    public static void main(String[] args) {
        int[] source1 = new int[source1Rows * source1Cols];
        int[] source2 = new int[source2Rows * source2Cols];
        int[] dest = new int[destRows * destCols];
        randomizeMatrix(source1);
        randomizeMatrix(source2);
        multiplyMatrix(source1, source2, dest);
        printMatrix(source1, source1Cols);
        printMatrix(source2, source2Cols);
        printMatrix(dest, destCols);

    }

    public static void randomizeMatrix(int[] matrix) {
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < matrix.length; i++) {
            // matrix[i] = r.nextInt(8);
            matrix[i] = rCount++;
        }
    }

    private static void multiplyMatrix(int[] source1, int[] source2, int[] dest) {
        while (currCol < source2Cols) {
            while (currRow < source1Rows) {
                int destValue = 0;
                System.out.println("iterating with currRow = " + currRow + " and currCol = " + currCol);
                int counter = 0;
                while (counter < source1Rows) {
                    int rowValue = source1[currRow * source1Cols + counter];
                    int colValue = source2[currCol + (counter * source2Cols)];
                    destValue += rowValue * colValue;
                    counter++;
                }
                dest[(currRow * destCols) + currCol] = destValue;
                currRow++;
            }
            currCol++;
        }

    }

    private static void printMatrix(int[] matrix, int numCols) {
        int counter = 0;
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(matrix[i] + ",");
        }
        System.out.println();
    }
}
