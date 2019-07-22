// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public class AbstractCodeScanAdapterConfig extends AbstractAdapterConfig {

	String sourceScanTargetString;
	
	@Override
	public String getTargetAsString() {
		if (sourceScanTargetString!=null) {
			return sourceScanTargetString;
		}
		return super.getTargetAsString();
	}
}
