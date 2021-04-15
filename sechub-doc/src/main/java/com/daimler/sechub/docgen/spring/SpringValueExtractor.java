// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import com.daimler.sechub.commons.core.util.SimpleStringUtils;

public class SpringValueExtractor {

	public class SpringValue{
		private String key="";
		private String defaultValue;

		public String getKey() {
			return key;
		}
		public boolean hasDefaultValue() {
			return defaultValue!=null;
		}
		/**
		 * Returns default value
		 * @return default value or empty string if not defined
		 */
		public String getDefaultValue() {
			if (defaultValue==null) {
				return "";
			}
			return defaultValue;
		}

		public String toDescription() {
			StringBuilder sb = new StringBuilder();
			sb.append("Key:").append(key);
			sb.append(", ");
			if (defaultValue!=null) {
				sb.append("per default:").append(defaultValue);
			}else {
				sb.append("no default set so must be defined");
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return "SpringValue [key=" + key + ", defaultValue=" + defaultValue + "]";
		}
	}

	public SpringValue extract(String string) {
		SpringValue value = new SpringValue();
		if (string==null) {
			return value;
		}
		String[] splitted = string.split("\\$\\{");
		String firstNotEmpty=null;
		for (String split: splitted) {
			if (SimpleStringUtils.isNotEmpty(split)) {
				firstNotEmpty=split.trim();
				if (firstNotEmpty.length()>1 && firstNotEmpty.endsWith("}")) {
					firstNotEmpty=firstNotEmpty.substring(0,firstNotEmpty.length()-1);
				}
				break;
			}
		}
		if (firstNotEmpty==null) {
			value.key=string;
			return value;
		}

		int leftIndex = firstNotEmpty.indexOf(":");
		if (leftIndex==-1) {
			value.key=firstNotEmpty;
			value.defaultValue=null;
			return value;
		}
		value.key= firstNotEmpty.substring(0,leftIndex);

		int startRight = leftIndex+1;
		if (firstNotEmpty.length()>startRight) {
			value.defaultValue=firstNotEmpty.substring(startRight);
		}else {
			/* we got an empty default value */
			value.defaultValue="";
		}
		return value;
	}

	public boolean isSpringValue(String string) {
		if (SimpleStringUtils.isEmpty(string)) {
			return false;
		}
		return (string.startsWith("${") && string.endsWith("}"));
	}
}
