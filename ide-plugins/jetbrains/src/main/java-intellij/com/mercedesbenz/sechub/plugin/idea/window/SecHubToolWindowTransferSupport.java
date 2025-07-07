// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.*;

import com.mercedesbenz.sechub.plugin.idea.SecHubReportImporter;
import com.mercedesbenz.sechub.plugin.util.ErrorLog;

public class SecHubToolWindowTransferSupport extends TransferHandler {
    private final ErrorLog errorLog;

    public SecHubToolWindowTransferSupport(ErrorLog errorLog) {
        this.errorLog = errorLog;
    }

    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) {
            // only supporting drop here
            return false;
        }

        /* only support file(s) */
        if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }

        return true;
    }

    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        // Handle transfer
        Transferable transferable = support.getTransferable();
        DataFlavor flavor = DataFlavor.javaFileListFlavor;
        List<File> files;
        try {
            files = (List<File>) transferable.getTransferData(flavor);
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        if (files.size() == 0) {
            errorLog.warn("File array is 0 - so ignored");
            return false;
        }
        if (files.size() > 1) {
            errorLog.warn("File array is greater than 1 - so import only first file. Others are ignored!");
        }
        File file = files.get(0);

        try {
            SecHubReportImporter.getInstance().importAndDisplayReport(file);
            return true;
        } catch (IOException e) {
            errorLog.error("Was not able to import", e);
            return false;
        }
    }
}
