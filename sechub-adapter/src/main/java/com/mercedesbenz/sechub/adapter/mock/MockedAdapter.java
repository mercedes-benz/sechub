// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.mock;

import com.mercedesbenz.sechub.adapter.Adapter;
import com.mercedesbenz.sechub.adapter.AdapterConfig;

/**
 * Just a marker interface for all mocks created for adapters - easier to find
 * in IDE...
 *
 * @author Albert Tregnaghi
 *
 */
public interface MockedAdapter<C extends AdapterConfig> extends Adapter<C> {

}
