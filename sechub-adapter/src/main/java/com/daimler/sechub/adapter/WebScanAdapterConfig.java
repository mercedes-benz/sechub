// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.URI;

public interface WebScanAdapterConfig extends AdapterConfig {

	public LoginConfig getLoginConfig();
	
	public SecHubTimeUnitData getMaxScanDuration();
	
	public boolean hasMaxScanDuration();
	
    /**
     * Returns the target URI
     *
     * @return target URI or <code>null</code> if none defined
     */
    public URI getTargetURI();
    
    /**
     * Returns a string describing the target type (e.g. "INTRANET")
     * @return a string describing the target, never <code>null</code>. when type not set an empty string is returned
     */
    String getTargetType();
    
    /**
     * Returns root target URI as string.
     * 
     * See {@link #getRootTargetURI()} for more details about the root target URI.
     * 
     * @return root uri as string or <code>null</code>
     */
    String getRootTargetURIasString();
    
    /**
     * Returns root target URI
     * 
     * The root target URI is the protocol scheme, hostname and port of an URI.
     * 
     * For example, the target URI is: https://example.org/test?test=1.
     * In this case the root target URI is: https://example.org
     *  
     * @return root uri or <code>null</code>
     */
    URI getRootTargetURI();
    
  /**
   * Returns a target string. When configured target is a for URIs, only the first target URI is returned as a simple string. But Target can
   * also be one ore more folder paths - e.g. for source scanning
   * <br><br>
   * This is interesting when the product scans no URLs or is only able to scan one URI at a time. So it would be a little bit cumbersome to use {@link #getTargetURIs()}
   * @return target URI string or <code>null</code> if none defined
   */
  String getTargetAsString();
}