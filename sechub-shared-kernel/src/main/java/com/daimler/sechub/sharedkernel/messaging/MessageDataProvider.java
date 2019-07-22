// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

public interface MessageDataProvider<T> {

	/**
	 * @param data
	 * @return object for data or <code>null</code>
	 */
	public T get(String data);

	/**
	 * 
	 * @param object 
	 * @return string describing object 
	 */
	public String getString(T object);
	
}
