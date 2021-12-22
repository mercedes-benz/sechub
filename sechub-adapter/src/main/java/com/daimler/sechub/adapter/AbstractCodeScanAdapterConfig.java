// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public abstract class AbstractCodeScanAdapterConfig extends AbstractAdapterConfig implements CodeScanAdapterConfig {

	String sourceScanTargetString;
	
	@Override
	public String getTargetAsString() {
		if (sourceScanTargetString == null) {
			return null;
		}
		return sourceScanTargetString;
	}
}
