// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.TemplateData;

public interface TemplateDataUIPart {

    public void setText(String text);
    
    public String getText();
    
    public TemplateData getData();
    
}
