// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.GridBagConstraints;

/**
 * A simple grid bag constraint factory having two columns: a) label b) component
 * <br><br>
 * <u>
 * <li>label has x size 1 in grid, is on location x:0</li>
 * <li>component  has x size1 3 in grid, is on location x:1</li>
 * </ul>
 * So grid it self has a size of 0-3 horizontal and vertical growing
 * 
 * @author Albert Tregnaghi
 *
 */
public class DialogGridBagConstraintsFactory {

    public static GridBagConstraints createLabelConstraint(int row) {
        GridBagConstraints gc = createConstraint(row, 0);
        gc.ipady = 15;
        gc.weightx = 0.0;
        return gc;
        
    }
    public static GridBagConstraints createComponentConstraint(int row) {
        GridBagConstraints gc = createConstraint(row, 1);
        gc.ipady = 5;
        gc.gridwidth = 3;
        gc.weightx = 0.5;
        return gc;
    }


    public static GridBagConstraints createConstraint(int row, int column) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = column;
        c.gridy = row;
        c.ipadx = 15;
        return c;
    }
}
