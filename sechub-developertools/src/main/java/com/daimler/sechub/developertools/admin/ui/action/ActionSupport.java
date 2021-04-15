// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class ActionSupport {

    private static final ActionSupport INSTANCE = new ActionSupport();

    public static final ActionSupport getInstance() {
        return INSTANCE;
    }

    private ActionSupport() {
    }

    public void apply(JPopupMenu menu, List<JMenuItem> items) {
        for (JMenuItem item : items) {
            menu.add(item);
        }
    }

    public void apply(JMenu menu, List<JMenuItem> items) {
        for (JMenuItem item : items) {
            menu.add(item);
        }
    }

    public List<JMenuItem> createDefaultCutCopyAndPastActions() {
        List<JMenuItem> menuItems = new ArrayList<>();

        JMenuItem menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
        menuItem.setMnemonic(KeyEvent.VK_T);
        menuItems.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItems.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("Paste");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItems.add(menuItem);
        return menuItems;
    }

    public void provideUndoRedo(JTextComponent textcomp) {
        provideUndoRedo(textcomp, null);
    }

    public void provideUndoRedo(JTextComponent textcomp, List<JMenuItem> menuItems) {
        UndoManager undoManager = new UndoManager();

        Document doc = textcomp.getDocument();
        doc.addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        ActionMap actionMap = textcomp.getActionMap();
        InputMap inputMap = textcomp.getInputMap();

        UndoAction undoAction = new UndoAction("Undo", undoManager);
        actionMap.put("Undo", undoAction);
        inputMap.put(KeyStroke.getKeyStroke("control Z"), "Undo");

        RedoAction redoAction = new RedoAction("Redo", undoManager);
        actionMap.put("Redo", redoAction);
        inputMap.put(KeyStroke.getKeyStroke("control shift Z"), "Redo");

        if (menuItems == null) {
            return;
        }
        menuItems.add(new JMenuItem(undoAction));
        menuItems.add(new JMenuItem(redoAction));
    }

    public List<JMenuItem> createFontResizeActions(JTextComponent component) {
        List<JMenuItem> menuItems = new ArrayList<>();
    
        menuItems.add(new JMenuItem(new IncreaseFontSizeAction(component)));
        menuItems.add(new JMenuItem(new ResetFontSizeAction(component)));
        menuItems.add(new JMenuItem(new DecreaseFontSizeAction(component)));
        menuItems.add(new JMenuItem(new PresentationFontSizeAction(component)));
        return menuItems;
    }
    
    public List<JMenuItem> createUndoRedoActions(JTextComponent textComponent) {
        List<JMenuItem> menuItems = new ArrayList<>();
        provideUndoRedo(textComponent,menuItems);
        return menuItems;
    }
        
    
    public void installAllTextActionsAsPopupTo(JTextComponent textComponent) {
        JPopupMenu popup = new JPopupMenu();
        textComponent.setComponentPopupMenu(popup);

        apply(popup, createDefaultCutCopyAndPastActions());
        popup.addSeparator();
        apply(popup, createCleanOutputActions(textComponent));
        popup.addSeparator();
        apply(popup, createFontResizeActions(textComponent));
        popup.addSeparator();
        apply(popup, createUndoRedoActions(textComponent));
        
    }

    public List<JMenuItem> createCleanOutputActions(JTextComponent component) {
        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new JMenuItem(new CleanOutputAreaAction(component)));
        return menuItems;
    }

    private class UndoAction extends AbstractAction {
        private final UndoManager undo;
        private static final long serialVersionUID = 1L;

        private UndoAction(String name, UndoManager undo) {
            super(name);
            this.undo = undo;
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                if (undo.canUndo()) {
                    undo.undo();
                }
            } catch (CannotUndoException e) {
            }
        }
    }

    private class RedoAction extends AbstractAction {
        private final UndoManager undo;
        private static final long serialVersionUID = 1L;

        private RedoAction(String name, UndoManager undo) {
            super(name);
            this.undo = undo;
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                if (undo.canRedo()) {
                    undo.redo();
                }
            } catch (CannotRedoException e) {
            }
        }
    }

    private abstract class TextSupportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        protected JTextComponent textComponent;
        protected Font originFont;

        TextSupportAction(JTextComponent textComponent) {
            this.textComponent = textComponent;
            originFont = textComponent.getFont();
        }
    }

    private class IncreaseFontSizeAction extends TextSupportAction {

        private static final long serialVersionUID = 1L;

        private IncreaseFontSizeAction(JTextComponent textComponent) {
            super(textComponent);
            putValue(Action.NAME, "Font ++");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Font oldFont = textComponent.getFont();
            Font newFont = new Font(oldFont.getFontName(), Font.PLAIN, oldFont.getSize() + 3);
            textComponent.setFont(newFont);

        }

    }

    private class DecreaseFontSizeAction extends TextSupportAction {

        private static final long serialVersionUID = 1L;

        private DecreaseFontSizeAction(JTextComponent textComponent) {
            super(textComponent);
            putValue(Action.NAME, "Font --");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Font oldFont = textComponent.getFont();
            Font newFont = new Font(oldFont.getFontName(), Font.PLAIN, oldFont.getSize() - 3);
            textComponent.setFont(newFont);

        }

    }

    private class ResetFontSizeAction extends TextSupportAction {

        private static final long serialVersionUID = 1L;

        private ResetFontSizeAction(JTextComponent textComponent) {
            super(textComponent);
            putValue(Action.NAME, "Font (100%)");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textComponent.setFont(originFont);

        }

    }

    private class PresentationFontSizeAction extends TextSupportAction {

        private static final long serialVersionUID = 1L;

        private PresentationFontSizeAction(JTextComponent textComponent) {
            super(textComponent);
            putValue(Action.NAME, "Font (200%)");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Font newFont = new Font(originFont.getFontName(), Font.PLAIN, originFont.getSize() * 2);
            textComponent.setFont(newFont);

        }

    }

    private class CleanOutputAreaAction extends TextSupportAction {

        private static final long serialVersionUID = 1L;

        private CleanOutputAreaAction(JTextComponent textComponent) {
            super(textComponent);
            putValue(Action.NAME, "Clean");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textComponent.setText("");
        }

    }
}
