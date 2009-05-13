/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainSimulatorFrame.java
 *
 * Created on May 7, 2009, 12:09:34 AM
 */
package org.cs533.newprocessor.simulator.GUI;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.core.ProcessorCore;
import org.cs533.newprocessor.components.core.RegisterFile;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author Vivek
 */
public class MainSimulatorFrame extends javax.swing.JFrame {

    private ProcessorCore pCore = null;

    public class RegisterListModel extends AbstractListModel implements ListModel {

        public int getSize() {
            return RegisterFile.NUM_REGISTERS + 2;
        }

        public void fireContentsChanged(int index0, int index1) {
            super.fireContentsChanged(this, index0, index1);
        }

        public Object getElementAt(int index) {
            if (index == 0) {
                return "pc   " + pCore.rFile.getPC();
            } else if (index == 1) {
                return "returnReg   " + pCore.rFile.getRetReg();
            } else {
                return "r" + (index - 2) + "   " +
                        pCore.rFile.getValueForRegister(index - 2);
            }
        }
    };

    /** Creates new form MainSimulatorFrame */
    public MainSimulatorFrame() {
        initComponents();
    }

    public MainSimulatorFrame(ProcessorCore core) {
        initComponents();
        pCore = core;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        runTickButton = new javax.swing.JButton();
        toggleSimulation = new javax.swing.JButton();
        tickCount = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jLabel1.setText("Processore CORE # -1");

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Processor State");

        runTickButton.setLabel("runTick");
        runTickButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runTickButtonActionPerformed(evt);
            }
        });

        toggleSimulation.setText("start simulation");
        toggleSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleSimulationActionPerformed(evt);
            }
        });

        tickCount.setText("tick count");
        tickCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tickCountActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(tickCount, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(runTickButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(toggleSimulation))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(131, 131, 131))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tickCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(runTickButton)
                            .addComponent(toggleSimulation)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 643, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void runTickButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runTickButtonActionPerformed
        Simulator.runTicks(1);
}//GEN-LAST:event_runTickButtonActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void toggleSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleSimulationActionPerformed
      final String[] newText = new String[1];
        if (Simulator.isRunning) {
            Simulator.stopSimulation();
            newText[0] = "start simulation";
        }else {
          Simulator.runSimulation();
          newText[0] = "stop simulation";
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                toggleSimulation.setText(newText[0]);
            }
        });
}//GEN-LAST:event_toggleSimulationActionPerformed

    private void tickCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tickCountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tickCountActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainSimulatorFrame().setVisible(true);
                
            }
        });
    }

    public void spawnUpdatingThread() {
        final RegisterListModel rModel = new RegisterListModel();
        jList1.setModel(rModel);
        Thread t = new Thread(new Runnable() {

            public void run() {
                AbstractInstruction oldInstr = null;
                try {
                    while (true) {
                        Thread.sleep(100);
                        rModel.fireContentsChanged(0, rModel.getSize() - 1);
                        tickCount.setText("tick count: " + pCore.tickCount);
                        if (pCore.abstrInstr != oldInstr) {
                            jTextArea1.setText(pCore.abstrInstr.toString());
                            oldInstr = pCore.abstrInstr;
                        }
                        jTextField1.setText(pCore.currentState.toString());
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainSimulatorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        t.start();
    }

    public static MainSimulatorFrame getFrameAndShow(final ProcessorCore core) {
        final MainSimulatorFrame frame = new MainSimulatorFrame(core);
        frame.setTitle("J-Core Multi-Core Processor Simulator");
        frame.jLabel1.setText("processor core " + core.coreNumber);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                frame.setVisible(true);
                frame.toggleSimulation.setText(Simulator.isRunning?"stop simulation":"start simulation");
            }
        });
        return frame;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton runTickButton;
    private javax.swing.JTextField tickCount;
    private javax.swing.JButton toggleSimulation;
    // End of variables declaration//GEN-END:variables
}