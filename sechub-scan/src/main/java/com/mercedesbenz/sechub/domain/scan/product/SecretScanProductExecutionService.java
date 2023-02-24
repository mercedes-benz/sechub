// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

/**
 * Marker interface, necessary for avoiding mockito problem (mockito inherits
 * abstract parts when directly using classes)
 */
public interface SecretScanProductExecutionService extends ProductExecutionStoreService {

}
