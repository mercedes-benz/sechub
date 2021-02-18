// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import com.daimler.sechub.developertools.JSONDeveloperHelper;

public class CopyToClipboardAsPropertyEntryAction extends AbstractAdapterDialogMappingAction {

    private static final long serialVersionUID = 1L;

    public CopyToClipboardAsPropertyEntryAction(MappingUI ui) {
        super("To clipboard", ui);
        putValue(Action.SHORT_DESCRIPTION,
                "<html>This will shrink the current JSON to unprettyfied format and<br>create a simple key=value text in clipboard <b>ready to paste as product executor parameter</b></html>");
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        String json = getMappingUI().getJSON();
        String compressedJson = JSONDeveloperHelper.INSTANCE.compress(json);
        String key = getMappingUI().getData().key;

        String content = key + "=" + compressedJson;

        StringSelection selection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

}
