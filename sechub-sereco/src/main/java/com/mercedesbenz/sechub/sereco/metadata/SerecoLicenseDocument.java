package com.mercedesbenz.sechub.sereco.metadata;

/**
 * A container to hold license documents
 * 
 * @author Jeremias Eppler
 *
 */
public class SerecoLicenseDocument {
	private SerecoLicenseSpdx spdx;

	public void setSpdx(SerecoLicenseSpdx spdx) {
		this.spdx = spdx;
	}

	public SerecoLicenseSpdx getSpdx() {
		return spdx;
	}
	
	public boolean hasSpdx() {
		boolean hasSpdx = false;
		
		if (spdx == null) {
			hasSpdx = true;
		}
		
		return hasSpdx;
	}
}
