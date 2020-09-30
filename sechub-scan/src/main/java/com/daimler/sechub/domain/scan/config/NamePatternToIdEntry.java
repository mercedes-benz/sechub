// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.JSONable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
public class NamePatternToIdEntry implements JSONable<NamePatternToIdEntry>{

	private static final Logger LOG = LoggerFactory.getLogger(NamePatternToIdEntry.class);

	private String namePattern;
	private String id;

	private Pattern regexp;

	private static final NamePatternToIdEntry JSON_INITIALIZER = new NamePatternToIdEntry();

	public static NamePatternToIdEntry createFromJSON(String json) {
		return JSON_INITIALIZER.fromJSON(json);
	}

	private NamePatternToIdEntry() {
		/* only internal used */
	}

	@JsonCreator
	public NamePatternToIdEntry(@JsonProperty("namePattern")String namePattern,@JsonProperty("id") String id) {
		this.namePattern = namePattern;
		this.id = id;

		try {
			regexp=Pattern.compile(namePattern);
		}catch(RuntimeException e) {
			LOG.error("Was not able to compile regexp: {}",namePattern, e);
		}
	}

	Pattern getRegexp() {
		return regexp;
	}

	public String getNamePattern() {
		return namePattern;
	}

	public String getId() {
		return id;
	}

	public boolean isMatching(String string) {
		if (string == null || regexp==null) {
			return false;
		}
		return regexp.matcher(string).matches();
	}

	@Override
	public Class<NamePatternToIdEntry> getJSONTargetClass() {
		return NamePatternToIdEntry.class;
	}

	@Override
	public String toString() {
		return "NamePatternToIdEntry [namePattern=" + namePattern + ", id=" + id + ", regexp=" + regexp + "]";
	}


}
