// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.UUID;

public interface ScanJobListener {

    void started(UUID jobUUID, CanceableScanJob scan);

    void ended(UUID jobUUID);

    void suspended(UUID jobUUID);

}