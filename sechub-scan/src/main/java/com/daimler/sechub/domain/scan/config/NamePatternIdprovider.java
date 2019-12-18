package com.daimler.sechub.domain.scan.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamePatternIdprovider {


private static final Logger LOG = LoggerFactory.getLogger(NamePatternIdprovider.class);

	private List<NamePatternToIdEntry> entries = new ArrayList<>();

	public void add(NamePatternToIdEntry entry) {
		if (entry==null) {
			LOG.warn("Ignoring null entry");
			return;
		}
		entries.add(entry);
	}

	/**
	 * Resolves id for given name or <code>null</code> when no matchers available
	 * @param name
	 * @return id or <code>null</code>
	 */
	public String getIdForName(String name) {
		for (NamePatternToIdEntry entry: entries) {
			if (entry.isMatching(name)) {
				return entry.getId();
			}
		}
		return null;
	}

}
