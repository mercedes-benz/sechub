// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

public interface ZapScan {

    void scan() throws ZapWrapperRuntimeException;

}