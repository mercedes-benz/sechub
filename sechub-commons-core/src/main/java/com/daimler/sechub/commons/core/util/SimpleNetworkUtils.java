package com.daimler.sechub.commons.core.util;

import java.net.URI;

public class SimpleNetworkUtils {
	/**
	 * Checks if a given URL is either null or empty
	 * 
	 * @param uri
	 * @return
	 */
    public static boolean isURINullOrEmpty(URI uri) {
    	return (uri == null || uri.toString().isEmpty());
    }
    
    /**
     * Checks whether the given URI has HTTP protocol information or not
     * 
     * @param uri
     * @return 
     */
	public static boolean isHttpProtocol(URI uri) {
	    boolean isHttpProtocol = false;
	    
	    if (!isURINullOrEmpty(uri)) {
		    String scheme = uri.getScheme();
		    
	        if ("http".equals(scheme) || "https".equals(scheme)) {
	            isHttpProtocol = true;
	        }
	    }
        
        return isHttpProtocol;
	}
}
