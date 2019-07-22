// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public interface ProxyConfig {

	String getProxyHostname();

	int getProxyPort();

	boolean isProxyDefined();
}
