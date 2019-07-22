// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import static com.daimler.sechub.docgen.GeneratorConstants.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.docgen.util.DocReflectionUtil;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class UseCaseModel {

	private static final Logger LOG = LoggerFactory.getLogger(UseCaseModel.class);

	Map<String, UseCaseEntry> useCases = new TreeMap<>();
	private String pathToUseCaseBaseFolder;

	public UseCaseModel(String pathToUseCaseBaseFolder) {
		this.pathToUseCaseBaseFolder = pathToUseCaseBaseFolder;
	}

	public SortedSet<UseCaseEntry> getUseCases() {
		return new TreeSet<>(useCases.values());
	}

	public class UseCaseEntry implements Comparable<UseCaseEntry> {

		private String id;
		private String title;
		private String description;
		private List<UseCaseEntryStep> steps = new ArrayList<>();
		private String annotationName;
		private UseCaseGroup[] groups;
		private String idEnumName;
		private Set<UseCaseRestDocEntry> restDocEntries = new LinkedHashSet<>();


		public UseCaseEntry(UseCaseGroup[] groups) {
			this.groups=groups;
		}
		public Set<UseCaseRestDocEntry> getRestDocEntries() {
			return restDocEntries;
		}
		public UseCaseGroup[] getGroups() {
			return groups;
		}

		public String getAnnotationName() {
			return annotationName;
		}

		public List<UseCaseEntryStep> getSteps() {
			return steps;
		}

		@Override
		public int compareTo(UseCaseEntry o) {
			return id.compareTo(o.id);
		}

		public String getIdentifierEnumName() {
			return idEnumName;
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UseCaseEntry other = (UseCaseEntry) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}

		public class UseCaseEntryStep implements Comparable<UseCaseEntryStep> {
			private String title;

			private String description;

			private SortedSet<String> rolesAllowed = new TreeSet<>();

			private int number;

			private int[] next;

			private String location;

			public String getLocation() {
				return location;
			}

			public Set<String> getRolesAllowed() {
				return rolesAllowed;
			}

			public String getTitle() {
				return title;
			}

			public String getDescription() {
				return description;
			}

			@Override
			public int compareTo(UseCaseEntryStep o) {
				return number - o.number;
			}

			public int getNumber() {
				return number;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + number;
				result = prime * result + ((title == null) ? 0 : title.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				UseCaseEntryStep other = (UseCaseEntryStep) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (number != other.number)
					return false;
				if (title == null) {
					if (other.title != null)
						return false;
				} else if (!title.equals(other.title)) {
					return false;
				}
				return true;
			}

			private UseCaseEntry getOuterType() {
				return UseCaseEntry.this;
			}

			public int[] getNext() {
				return next;
			}

		}

		public void addStep(Step step, List<RolesAllowed> rolesAllowedList, String location) {
			UseCaseEntryStep entryStep = new UseCaseEntryStep();
			entryStep.title = step.name();
			entryStep.number = step.number();
			entryStep.next = step.next();
			String stepDescription = step.description();
			if (stepDescription.endsWith(".adoc")) {
				entryStep.description = createIncludeOfUseCaseAsciiDocFile(stepDescription);
			} else {
				entryStep.description = stepDescription;
			}
			if (rolesAllowedList != null) {
				List<String> roleStringList = new ArrayList<>();
				for (RolesAllowed ra : rolesAllowedList) {
					String[] allowedRolesStringArray = ra.value();
					for (String role: allowedRolesStringArray) {
						if (role!=null) {
							roleStringList.add(role);
						}
					}
				}
				entryStep.rolesAllowed.addAll(roleStringList);
			}
			entryStep.location = location;
			steps.add(entryStep);
		}
		public void addLinkToRestDoc(UseCaseRestDocEntry restDocEntry) {
			restDocEntries.add(restDocEntry);
		}

	}

	public UseCaseEntry ensureUseCase(final Class<? extends Annotation> clazz) {
		Class<? extends Annotation> clazzToFetch = DocReflectionUtil.resolveUnproxiedClass(clazz);
		/* NOSONAR */return useCases.computeIfAbsent(fetchId(clazz).uniqueId(), name -> createEntry(clazzToFetch));
	}

	private UseCaseEntry createEntry(Class<? extends Annotation> clazz) {
		if (DEBUG) {
			LOG.info("create entry:{}", clazz);
		}
		UseCaseIdentifier id = fetchId(clazz);
		UseCaseGroup[] groups = fetchGroups(clazz);

		UseCaseEntry entry = new UseCaseEntry(groups);
		Class<? extends Annotation> clazzToFetch = DocReflectionUtil.resolveUnproxiedClass(clazz);
		UseCaseDefinition definition = clazzToFetch.getAnnotation(UseCaseDefinition.class);
		entry.id = id.uniqueId();
		entry.idEnumName=id.name();
		entry.title = definition.title();
		entry.annotationName = clazzToFetch.getName();
		String description = definition.description();
		if (description.endsWith(".adoc")) {
			entry.description = createIncludeOfUseCaseAsciiDocFile(description);
		} else {
			entry.description = description;
		}
		return entry;

	}

	private UseCaseGroup[] fetchGroups(Class<? extends Annotation> clazz) {
		Class<? extends Annotation> clazzToFetch = DocReflectionUtil.resolveUnproxiedClass(clazz);
		UseCaseDefinition definition = clazzToFetch.getAnnotation(UseCaseDefinition.class);
		if (definition == null) {
			return new UseCaseGroup[] {};
		}
		return definition.group();
	}

	private String createIncludeOfUseCaseAsciiDocFile(String name) {
		return "include::" + pathToUseCaseBaseFolder + "/" + name + "[]";
	}

	private UseCaseIdentifier fetchId(Class<? extends Annotation> clazz) {
		Class<? extends Annotation> clazzToFetch = DocReflectionUtil.resolveUnproxiedClass(clazz);
		UseCaseDefinition definition = clazzToFetch.getAnnotation(UseCaseDefinition.class);
		if (definition == null) {
			throw new IllegalStateException("cannot fetch id from "+clazz);
		}
		return definition.id();
	}

	public SortedSet<UseCaseEntry> getUseCasesInsideGroup(UseCaseGroup wantedGroup) {
		SortedSet<UseCaseEntry> entries = new TreeSet<>();
		SortedSet<UseCaseEntry> all = getUseCases();
		for (UseCaseEntry entry:all) {
			for (UseCaseGroup group: entry.getGroups()) {
				if (group==wantedGroup) {
					entries.add(entry);
					break;
				}
			}
		}
		return entries;
	}
}
