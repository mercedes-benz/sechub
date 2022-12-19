// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public interface TrustAllConfig extends ProxyConfig, TraceIdProvider {

    boolean isTrustAllCertificatesEnabled();
}
