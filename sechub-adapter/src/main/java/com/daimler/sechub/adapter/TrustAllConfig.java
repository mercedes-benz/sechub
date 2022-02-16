// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public interface TrustAllConfig extends ProxyConfig, TraceIdProvider {

    boolean isTrustAllCertificatesEnabled();
}
