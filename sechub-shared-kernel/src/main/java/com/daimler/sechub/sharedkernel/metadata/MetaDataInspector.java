// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.metadata;

/**
 * Represents a mechanism to inspect some meta data.
 * @author Albert Tregnaghi
 *
 */
public interface MetaDataInspector {

	/**
	 * Starts a new inspection
	 * @param id
	 * @return inspection, never <code>null</code>
	 */
	MetaDataInspection inspect(String id) ;

}
