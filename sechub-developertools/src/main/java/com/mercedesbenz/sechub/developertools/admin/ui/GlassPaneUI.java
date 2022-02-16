// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GlassPaneUI {

    private MyGlassPane glassPane;

    public GlassPaneUI(UIContext context, JFrame frame) {
        this.glassPane = new MyGlassPane();
        frame.setGlassPane(glassPane);
    }

    public void block(boolean block) {
        SwingUtilities.invokeLater(() -> handleBlock(block));
    }

    private Object handleBlock(boolean block) {
        glassPane.setVisible(block);
        return null;
    }

    private class MyGlassPane extends JComponent implements KeyListener {

        private static final long serialVersionUID = 1L;

        public MyGlassPane() {
            Color color = new Color(0.0f, 0.0f, 0.0f, 0.5f);
            setBackground(color);
            setVisible(false);
            /* disable key and mouse event handling when visible */
            addKeyListener(this);
            addMouseListener(new MouseAdapter() {
            });
            addMouseMotionListener(new MouseMotionAdapter() {
            });

            /* set busy cursor */
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getSize().width, getSize().height);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            e.consume();
        }
    }
}
