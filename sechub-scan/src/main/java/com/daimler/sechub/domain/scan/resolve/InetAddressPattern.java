// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import java.net.InetAddress;

public interface InetAddressPattern {

	public boolean isMatching(InetAddress address);
}
