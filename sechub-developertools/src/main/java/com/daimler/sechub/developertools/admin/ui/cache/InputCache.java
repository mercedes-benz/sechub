// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.cache;

import java.util.EnumMap;
import java.util.Map;

public class InputCache {
	
	private Map<InputCacheIdentifier,String> cache = new EnumMap<>(InputCacheIdentifier.class);

	public String get(InputCacheIdentifier identifier) {
		if (identifier==null) {
			return "";
		}
		return cache.get(identifier);
	}

	public void set(InputCacheIdentifier identifier, String value) {
		cache.put(identifier,value);
	}
}
