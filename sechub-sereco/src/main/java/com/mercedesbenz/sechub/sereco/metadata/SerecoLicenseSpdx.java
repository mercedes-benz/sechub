package com.mercedesbenz.sechub.sereco.metadata;

public class SerecoLicenseSpdx {
	private String json;

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
		spdxLicense.setJson(spdx);
		
		return spdxLicense;
	}
	
	private void setJson(String spdx) {
		this.json = spdx;
	}
	
	public String getJson() {
		return json;
	}
	
	public boolean hasSpdxJson() {
		boolean hasSpdxJson = false;
		
		if (json != null) {
			hasSpdxJson = true;
		}
		
		return hasSpdxJson;
	}
}
