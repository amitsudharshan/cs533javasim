/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.assembler.abstractandinterface;

import org.cs533.newprocessor.components.core.RegisterFile;

/**
 *
 * @author amit
 */
public interface ALUInstructionInterface {

        public void executeOperation(RegisterFile reg);
}
