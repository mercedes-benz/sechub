// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

/**
 * An installation setup defines the necessary definitions when using different target types (INTERNET, INTRANET).
 * The setup is the wrapper part for those product executions which have to handle execution different. Mostly
 * this is an issue with internal network behaviour, firewall configurations etc.<br><br>
 * Implementations for this interface shall also provide more information about the target specific situation/installation. See
 * existing implementations for examples or as a short description next "ascii art picture":<br><br>
 * 
 * <pre>
 *  TargetType      Intranet               Internet                CodeUpload
 *                  -----------------      -------------------     ------------
 *  Product         scanner1.intra.net     scanner2.intra.net      codescanner.intra.net
 *  Proxy           scannerproxy.intra.net         
 *  Proxy-Port      8888     
 *  CodeIdentifier                                                 code-upload://c:/dev/a;c:/dev/b   
 * </pre>
 * 
 * @author Albert Tregnaghi
 *
 */
public interface InstallSetup {

	/**
	 * Returns <code>true</code> when the setup is able to handle the wanted target type. Normally a <code>false</code> is 
	 * returned when no credentials are available or no configuration for the target type exists.
	 * @param target
	 * @return <code>true</code> when able to scan target type otherwise <code>false</code>
	 * 
	 */
	public boolean isAbleToScan(TargetType targetType);
}
