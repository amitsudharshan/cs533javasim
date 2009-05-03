/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor;

import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author amit
 */
public interface AsyncComponentInterface extends Runnable {
        public void configure(CyclicBarrier prepBarrier, CyclicBarrier clockBarrier);

}
