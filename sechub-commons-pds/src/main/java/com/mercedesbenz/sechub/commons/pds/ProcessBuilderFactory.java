// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import java.util.List;

/**
 * An abstraction for creating process builders. Via this class we can mock
 * process building in tests very easy with Mockito.
 *
 * @author Albert Tregnaghi
 *
 */
public interface ProcessBuilderFactory {

    public ProcessBuilder createForCommands(String... commands);

    public ProcessBuilder createForCommandList(List<String> commands);
}
