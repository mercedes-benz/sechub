// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

/**
 * Represents a key having type information for its value pendant
 *
 * @author Albert Tregnaghi
 *
 * @param <V>
 */
public interface TypedKey<V> {

    public String getId();

    public Class<V> getValueClass();
}
