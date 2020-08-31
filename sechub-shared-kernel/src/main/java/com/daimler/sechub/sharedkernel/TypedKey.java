// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

/**
 * Represents a key having type information for its value pendant 
 * @author Albert Tregnaghi
 *
 * @param <V>
 */
public interface TypedKey<V> {

	public String getId();
	public Class<V> getValueClass();
}
