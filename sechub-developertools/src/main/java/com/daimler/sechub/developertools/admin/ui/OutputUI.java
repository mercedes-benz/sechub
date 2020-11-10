// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;

public class OutputUI {
    
    private static final String OUTPUT_FONT_SETTINGS = ConfigurationSetup.getOutputFontSettings("courier 10");
    private JPanel panel;
    private JTextArea outputTextArea;
    private Font originFont;

    private static final Logger LOG = LoggerFactory.getLogger(OutputUI.class);

    public JPanel getPanel() {
        return panel;
    }

    public OutputUI() {
        panel = new JPanel(new BorderLayout());

        outputTextArea = new JTextArea();
        outputTextArea.setFont(Font.decode(OUTPUT_FONT_SETTINGS));
        JPopupMenu popup = new JPopupMenu();
        outputTextArea.setComponentPopupMenu(popup);
        originFont = outputTextArea.getFont();
        

        ActionSupport support = new ActionSupport();
        support.apply(popup, support.createDefaultCutCopyAndPastActions());
        popup.addSeparator();
        popup.add(new CleanOutputAreaAction());
        popup.addSeparator();
        popup.add(new IncreaseFontSizeAction());
        popup.add(new ResetFontSizeAction());
        popup.add(new DecreaseFontSizeAction());
        popup.add(new PresentationFontSizeAction());

        panel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

    }
    

    public void output(String text) {
        outputTextArea.append(text);
        outputTextArea.append("\n");
        outputTextArea.setCaretPosition(outputTextArea.getText().length());
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable t) {
        LOG.error(message, t);
        output("ERROR:");
        output(message);
        if (t != null) {
            output(t.toString());
            output(">> Look into your shell or IDE console output for details!");
        }
    }
    private class IncreaseFontSizeAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        
        private IncreaseFontSizeAction() {
            putValue(Action.NAME,"Font ++");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Font oldFont = outputTextArea.getFont();
            Font newFont = new Font(oldFont.getFontName(),Font.PLAIN,oldFont.getSize()+3);
            outputTextArea.setFont(newFont);
            
        }
        
    }
    
    private class DecreaseFontSizeAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        
        private DecreaseFontSizeAction() {
            putValue(Action.NAME,"Font --");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Font oldFont = outputTextArea.getFont();
            Font newFont = new Font(oldFont.getFontName(),Font.PLAIN,oldFont.getSize()-3);
            outputTextArea.setFont(newFont);
            
        }
        
    }
    
    private class ResetFontSizeAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        
        private ResetFontSizeAction() {
            putValue(Action.NAME,"Font (100%)");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            outputTextArea.setFont(originFont);
            
        }
        
    }
    
    private class PresentationFontSizeAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        
        private PresentationFontSizeAction() {
            putValue(Action.NAME,"Font (200%)");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Font newFont = new Font(originFont.getFontName(),Font.PLAIN,originFont.getSize()*2);
            outputTextArea.setFont(newFont);
            
        }
        
    }
    private class CleanOutputAreaAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        
        private CleanOutputAreaAction() {
            putValue(Action.NAME,"Clean");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            outputTextArea.setText("");
        }
        
    }

}
