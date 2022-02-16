// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import java.io.File;

import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc.SpringRestDocOutput;

public class UseCaseRestDocEntry {

    public UseCaseEntry /* NOSONAR */ usecaseEntry;
    public String /* NOSONAR */ variantOriginValue;
    public String /* NOSONAR */ variantId;
    public String /* NOSONAR */ path;
    public String /* NOSONAR */ identifier;
    public File /* NOSONAR */ copiedRestDocFolder;
    public SpringRestDocOutput[] /* NOSONAR */ wanted;
}
