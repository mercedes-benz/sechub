// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import java.io.File;
import java.lang.reflect.Method;

import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc.SpringRestDocOutput;

public class UseCaseRestDocEntry {

    public UseCaseEntry /* NOSONAR */ usecaseEntry;
    public String /* NOSONAR */ variantOriginValue;
    public String /* NOSONAR */ variantId;
    public String /* NOSONAR */ path;
    public String /* NOSONAR */ identifier;
    public File /* NOSONAR */ copiedRestDocFolder;
    public SpringRestDocOutput[] /* NOSONAR */ wanted;
    public Method restDocTestMethod;
}
