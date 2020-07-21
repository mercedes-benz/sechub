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

}
