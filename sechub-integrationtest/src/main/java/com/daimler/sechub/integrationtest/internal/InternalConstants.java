// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import com.daimler.sechub.test.ExampleConstants;

public class InternalConstants {

	private InternalConstants() {

	}
	public static final String URL_FOR_NETSPARKER_GREEEN_LONG_RUNNING= "https://longrunning.but.green.demo."+ExampleConstants.URI_TARGET_SERVER;
	public static final String URL_FOR_NETSPARKER_ONE_FINDING= "https://vulnerable.demo."+ExampleConstants.URI_TARGET_SERVER;
	public static final String URL_FOR_NETSPARKER_MANY_FINDINGS = "https://netsparker.manyfindings.demo."+ExampleConstants.URI_TARGET_SERVER;

}
