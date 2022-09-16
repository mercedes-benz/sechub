// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

/**
 * Marker interface, necessary for avoiding mockito problem (mockito does also
 * inherit abstract parts when directly using classes)
 *
 * @author Albert Tregnaghi
 *
 */
public interface InfrastructureScanProductExecutionService extends ProductExecutionStoreService {

}