// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.util.Map;

import com.daimler.sechub.adapter.AdapterConfig;

public interface PDSAdapterConfig extends AdapterConfig{

	Map<String,String> getJobParameters();


}