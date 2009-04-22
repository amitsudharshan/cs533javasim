/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amit
 */
public class ExecutableImage implements Serializable {

    public byte[] memoryImage;
    public int[] initialPC;

    public ExecutableImage(byte[] image, int[] pcInit) {
        memoryImage = image;
        initialPC = pcInit;
    }

    public int[] getInitialPC() {
        return initialPC;
    }

    public byte[] getMemoryImage() {
        return memoryImage;
    }

    public static ExecutableImage loadImageFromFile(String file) {
        ObjectInputStream objIn = null;
        ExecutableImage toReturn = null;
        try {
            objIn = new ObjectInputStream(new FileInputStream(file));
            toReturn = (ExecutableImage)objIn.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExecutableImage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExecutableImage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objIn.close();
            } catch (IOException ex) {
                Logger.getLogger(ExecutableImage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return toReturn;
    }
}
