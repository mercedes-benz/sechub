// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;

public class ActionSupport {

	public void apply(JPopupMenu menu, List<JMenuItem> items) {
		for (JMenuItem item: items) {
			menu.add(item);
		}
	}
	
	public void apply(JMenu menu, List<JMenuItem> items) {
		for (JMenuItem item: items) {
			menu.add(item);
		}
	}
	
	public List<JMenuItem> createDefaultCutCopyAndPastActions(){
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
}
