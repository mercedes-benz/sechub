// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListedProfile {
	String name;

	List<ListedProfile> includedProfiles = new ArrayList<>();
	List<File> configFiles = new ArrayList<>();

	public ListedProfile(String profileName) {
		this.name=profileName;
	}

	public String getLabel() {
		String label = name;
		if (isBaseProfile()) {
			label= "default";
		}
		return label.replaceAll("-", "_");
	}

	public String getName() {
		return name;
	}

	public static String calculateProfileName(File file) {
		return calculateProfileName(file.getName());
	}


	public static String calculateProfileName(String name) {
		int pointIndex = name.indexOf('.');
		if (pointIndex==-1) {
			return "unknown";
		}
		String sub = name.substring(0,pointIndex);
		if (!sub.startsWith("application")) {
			return "unknown-"+sub;
		}
		int length = "application".length();
		if (sub.length()==length) {
			return "";
		}
		String withoutApplication = sub.substring(length);
		if (withoutApplication.length()==0) {
			return "";
		}
		return withoutApplication.substring(1);
	}

	public static boolean isPropertyFile(File file) {
		return isPropertyFile(file.getName());
	}


	public static boolean isPropertyFile(String name) {
		if (name==null) {
			return false;
		}
		return name.endsWith(".properties");
	}

	public static boolean isYaml(File file) {
		return isYaml(file.getName());
	}


	public static boolean isYaml(String name) {
		if (name==null) {
			return false;
		}
		return name.endsWith(".yml")|| name.endsWith(".yaml");
	}

	public boolean isBaseProfile() {
		return "".contentEquals(name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListedProfile other = (ListedProfile) obj;
		return Objects.equals(name, other.name);
	}

}