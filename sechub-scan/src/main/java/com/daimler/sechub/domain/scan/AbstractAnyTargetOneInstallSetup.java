// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

/**
 * Abstract base class for scans where INTERNET and INTRANET scans will be done by ONE installation and we do NOT
 * CARE about in which environment we are. The product is able to handle all
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractAnyTargetOneInstallSetup extends AbstractInstallSetup implements AnyTargetOneInstallSetup{
	

	@Override
	protected void init(ScanInfo info) {
	}
	
	@Override
	public final boolean isAbleToScan(TargetType type) {
		/* can scan all...*/
		return true;
	}

}
