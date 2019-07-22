// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import com.daimler.sechub.adapter.Adapter;
import com.daimler.sechub.adapter.AdapterConfig;

/**
 * Just a marker interface for all mocks created for adapters - easier to find in IDE...
 * @author Albert Tregnaghi
 *
 */
public interface MockedAdapter<C extends AdapterConfig> extends Adapter<C>{

	/**
	 * For some stages (e.g. test) a sanity check can be enabled to check parameters
	 * are as expected. So config builder etc. can be tested.
	 * But for environments where those variable are set on deployment (e.g. kubernetes
	 * this is normally a problem and should not be turned on)
	 * @return
	 */
	public default boolean isMockSanityCheckEnabled() {
		return Boolean.getBoolean("sechub.adapter.mock.sanitycheck.enabled");
	}
}
