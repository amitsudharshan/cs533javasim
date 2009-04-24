/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import java.util.Hashtable;
import java.lang.Integer;
import java.util.HashSet;
/**
 *
 * @author Vivek
 */

/**
   *
   * P1 read  0x100
   * P2 read 0x100   (should become shared in both)
   * P1 write 0x100 (invalidate P2's copy)
   * P2 reads 0x100  (back to shared in both)
   * P2 write 0x100  (invalidate P1's copy ,  share P2)
   */
public class MESI_Test
{
 //  HashSet<L1MESICacheLine> Dcache1
 //          = new HashSet();
  Hashtable<Integer, L1MESICacheLine> cache1 = new Hashtable<Integer, L1MESICacheLine>();
  Hashtable<Integer, L1MESICacheLine> cache2 = new Hashtable<Integer, L1MESICacheLine>();
  L1MESICacheLine x = new L1MESICacheLine();
  // cache1.put(4, 2);
   //cache1.put(, value);
   //new L1MESICacheLine()
   // Dcache1.add(new CacheLine());



  // cache1.put(Integer(4), new L1MESICacheLine() );
}
