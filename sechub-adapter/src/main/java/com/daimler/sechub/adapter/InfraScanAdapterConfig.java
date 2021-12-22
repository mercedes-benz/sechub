package com.daimler.sechub.adapter;

import java.net.InetAddress;
import java.net.URI;
import java.util.Set;

public interface InfraScanAdapterConfig extends AdapterConfig {
    
    /**
     * Returns a string describing the target type (e.g. "INTRANET")
     * @return a string describing the target, never <code>null</code>. when type not set an empty string is returned
     */
    String getTargetType();

    /**
     * Return all target URIs
     * @return target URIs (equals in URLs is a little bit odd, can make Domain look ups so we use URI instead).<br><br>
     */
    Set<URI> getTargetURIs();

    // TODO still needed?
//    /**
//     * Returns only the first target URI
//     * <br><br>
//     * This is interesting when the product is only able to scan one URI at a time. So it would be a little bit cumbersome to use {@link #getTargetURIs()}
//     * @return target URI or <code>null</code> if none defined
//     */
//    URI getTargetURI();

    // TODO still needed?
//    /**
//     * Returns a target string. When configured target is a for URIs, only the first target URI is returned as a simple string. But Target can
//     * also be one ore more folder pathes - e.g. for source scanning
//     * <br><br>
//     * This is interesting when the product scans no URLs or is only able to scan one URI at a time. So it would be a little bit cumbersome to use {@link #getTargetURIs()}
//     * @return target URI string or <code>null</code> if none defined
//     */
//    String getTargetAsString();
    
    /**
     * Returns root target URIs (equals in URLs is a little bit odd, can make Domain look ups so we use URI instead).<br><br>
     * An example:
     * <br>
     * The config has got following target uris:
     * <pre>
     * https://www.mycoolstuff.com/app1/
     * https://www.mycoolstuff.com/app2/
     * https://www.mycoolstuff.com/app3/
     * https://www.othercoolstuff.com/app4/
     * </pre>
     * the call to this method will result in
     * <pre>
     * https://www.mycoolstuff.com
     * https://www.othercoolstuff.com
     * </pre>
     * @return a set, never <code>null</code>
     */
    Set<URI> getRootTargetURIs();

    //TODO still needed?
//    /**
//     * Returns root target URI (will return always the first one or <code>null</code>)
//     * @return root uri or <code>null</code>
//     */
//    URI getRootTargetURI();

    //TODO still neeed?
//    /**
//     * Returns root target URI as string (will return always the first one or <code>null</code>)
//     * @return root uri as string or <code>null</code>
//     */
//    String getRootTargetURIasString();

    /**
     * Returns IP adresses.
     * @return ip adresses or empty set, never <code>null</code>
     */
    Set<InetAddress> getTargetIPs();
}
