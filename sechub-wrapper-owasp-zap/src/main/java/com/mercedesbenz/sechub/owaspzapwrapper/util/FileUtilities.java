// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

public class FileUtilities {

    public static File stringToFile(String file) {
        if (file == null) {
            return null;
        }
        try {
            return new File(file).toPath().toRealPath().toAbsolutePath().toFile();
        } catch (IOException e) {
            throw new ZapWrapperRuntimeException("Not able to resolve absolute path of: " + file, e, ZapWrapperExitCode.IO_ERROR);
        }
    }

}
