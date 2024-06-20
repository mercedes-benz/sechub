// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;

public interface LogSanitizerProvider {

    public PDSLogSanitizer getLogSanitizer();
}
