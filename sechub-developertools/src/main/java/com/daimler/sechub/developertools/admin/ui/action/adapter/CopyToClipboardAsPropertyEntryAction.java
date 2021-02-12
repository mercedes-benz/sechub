package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.JSONDeveloperHelper;

public class CopyToClipboardAsPropertyEntryAction extends AbstractAdapterDialogMappingAction {

    private static final long serialVersionUID = 1L;

    public CopyToClipboardAsPropertyEntryAction(MappingUI ui) {
        super("To clipboard", ui);
    }

    @Override
    protected void execute(ActionEvent e) throws Exception {
        String json = getMappingUI().getJSON();
        String compressedJson = JSONDeveloperHelper.INSTANCE.compress(json);
        String key = getMappingUI().getMappingId();
        
        String content = key + "="+compressedJson;
     
        StringSelection selection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

}
