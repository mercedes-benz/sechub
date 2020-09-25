// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.Color;

import javax.swing.JButton;

import com.daimler.sechub.commons.model.TrafficLight;

public class TrafficLightComponent extends JButton {
    
    private static final long serialVersionUID = 1L;
    private TrafficLight trafficLight;

    public TrafficLightComponent() {
        setEnabled(false);
        setContentAreaFilled(false);
        setOpaque(true);
        setText("  ");
    }
    
    public void setTrafficLight(TrafficLight tl) {
        this.trafficLight=tl;
    }

    protected void paintComponent(java.awt.Graphics g) {
        if (trafficLight==null) {
            return;
        }
        switch(trafficLight) {
        case RED:
            setBackground(Color.RED);
            break;
        case GREEN:
            setBackground(Color.GREEN);
            break;
        case YELLOW:
            setBackground(Color.YELLOW);
            break;
        
        }
        super.paintComponent(g);
    };
}
