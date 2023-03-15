// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.adapter;

import com.mercedesbenz.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.TemplateData;

public interface TemplateDataUIPart {

    public void setText(String text);

    public String getText();

    public TemplateData getData();

}
