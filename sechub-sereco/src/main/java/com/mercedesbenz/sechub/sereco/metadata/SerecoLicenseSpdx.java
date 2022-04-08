package com.mercedesbenz.sechub.sereco.metadata;

public class SerecoLicenseSpdx {
	private String spdxJson;

	/**
	 * The SPDX standard defines several document types.
	 * 
	 * This method accepts any document type and sets the
	 * correct type.
	 *  
	 * @param spdx
	 * @return
	 */
	public static SerecoLicenseSpdx of(String spdx) {
		SerecoLicenseSpdx spdxLicense = new SerecoLicenseSpdx();
		spdxLicense.setSpdxJson(spdx);
		
		return spdxLicense;
	}
	
	private void setSpdxJson(String spdx) {
		this.spdxJson = spdx;
	}
	
	public String getSpdxJson() {
		return spdxJson;
	}
	
	public boolean hasSpdxJson() {
		boolean hasSpdxJson = false;
		
		if (spdxJson != null) {
			hasSpdxJson = true;
		}
		
		return hasSpdxJson;
	}
}
