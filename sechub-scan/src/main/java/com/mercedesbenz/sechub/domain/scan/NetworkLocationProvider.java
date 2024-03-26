// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

public interface NetworkLocationProvider {

    List<URI> getURIs();

    List<InetAddress> getInetAddresses();

}
