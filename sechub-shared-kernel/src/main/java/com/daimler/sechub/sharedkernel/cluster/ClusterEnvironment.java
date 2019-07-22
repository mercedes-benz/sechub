// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.cluster;

/**
 * Describes the environment where the spring boot application is running
 * @author Albert Tregnaghi
 *
 */
public class ClusterEnvironment {

	private POD pod;

	public ClusterEnvironment() {
		this.pod=new POD();
	}
	
	/**
	 * When we are in kubernetes cluster this will contain POD information
	 * @return
	 */
	public POD getPod() {
		return pod;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Environment:");
		sb.append(pod.toString());
		return sb.toString();
	}
	
}


