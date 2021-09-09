// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import com.daimler.sechub.sharedkernel.MustBeKeptStable;

@MustBeKeptStable("You can rename enums, but do not change id parts, because used inside DB!")
public enum ScanProjectConfigID {

    /**
     * Id to fetch  mock configuration data, which will contain JSON representing a ScanProjectMockDataConfiguration
     * object
     */
	MOCK_CONFIGURATION("mock_config"), 
	
	/**
	 * Id to fetch false positive configuration data, which will contain JSON representing a FalsePositiveProjectConfiguration
	 * object
	 */
	FALSE_POSITIVE_CONFIGURATION("false_positives"), 
	
	/**
	 * Id to fetch project access level data, which will contain just the id of the access level.
	 */
	PROJECT_ACCESS_LEVEL("project_access_level"),
	
	;
	
	private String id;

	ScanProjectConfigID(String id){
		notNull(id, "config id may not be null!");
		maxLength(id,20); // because in DB we got only 3x20 defined, so max is 20
		this.id=id;
	}
	
	public String getId() {
		return id;
	}
}
