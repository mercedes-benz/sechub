// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

/**
 * Marker interface, necessary for avoiding mockito problem (mockito does also
 * inherit abstract parts when directly using classes)
 * 
 * @author Albert Tregnaghi
 *
 */
public interface ReportProductExecutionService extends ProductExectionStoreService {

}