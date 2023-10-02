// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import static com.mercedesbenz.sechub.docgen.GeneratorConstants.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.docgen.util.DocReflectionUtil;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseDefinition;
import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseGroup;
import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseIdentifier;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

import jakarta.annotation.security.RolesAllowed;

public class UseCaseModel {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseModel.class);

    private String pathToUseCaseBaseFolder;
    private Map<UseCaseGroup, UseCaseDefGroup> groupMap = new LinkedHashMap<>();
    private Map<PDSUseCaseGroup, UseCaseDefGroup> pdsGroupMap = new LinkedHashMap<>();
    Map<String, UseCaseEntry> useCases = new TreeMap<>();

    private UseCaseModelType type;

    public UseCaseModel(String pathToUseCaseBaseFolder, UseCaseModelType type) {
        this.pathToUseCaseBaseFolder = pathToUseCaseBaseFolder;
        this.type = type;
    }

    public SortedSet<UseCaseEntry> getUseCases() {
        return new TreeSet<>(useCases.values());
    }

    public static enum UseCaseModelType {
        SECHUB,

        PDS,
    }

    public UseCaseModelType getType() {
        return type;
    }

    public class UseCaseDefGroup {

        public String title;
        public String description;
        public String name;
        private UseCaseModelType modelType;

        public UseCaseDefGroup(UseCaseModelType modelType) {
            this.modelType = modelType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hash(name, modelType);
            return result;
        }

        public UseCaseModel getModel() {
            return UseCaseModel.this;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            UseCaseDefGroup other = (UseCaseDefGroup) obj;
            return Objects.equals(name, other.name) && modelType == other.modelType;
        }

        public SortedSet<UseCaseEntry> getUseCases() {
            return getModel().getUseCasesInsideGroup(this);
        }

    }

    public class UseCaseEntry implements Comparable<UseCaseEntry> {

        private String id;
        private String title;
        private String description;
        private List<UseCaseEntryStep> steps = new ArrayList<>();
        private String annotationName;
        private UseCaseDefGroup[] groups;
        private String idEnumName;
        private Set<UseCaseRestDocEntry> restDocEntries = new LinkedHashSet<>();

        public UseCaseEntry(UseCaseDefGroup[] groups) {
            this.groups = groups;
        }

        public Set<UseCaseRestDocEntry> getRestDocEntries() {
            return restDocEntries;
        }

        public UseCaseDefGroup[] getGroups() {
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

        @Override
        public String toString() {
            return "UCEntry:" + id;
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
            addStep(new SecHubStepDataProvider(step), rolesAllowedList, location);
        }

        public void addStep(PDSStep step, List<RolesAllowed> rolesAllowedList, String location) {
            addStep(new PDSStepDataProvider(step), rolesAllowedList, location);
        }

        public void addStep(StepDataProvider step, List<RolesAllowed> rolesAllowedList, String location) {
            UseCaseEntryStep entryStep = new UseCaseEntryStep();
            entryStep.title = step.getTitle();
            entryStep.number = step.getNumber();
            entryStep.next = step.getNext();
            String stepDescription = step.getDescription();
            if (stepDescription.endsWith(".adoc")) {
                entryStep.description = createIncludeOfUseCaseAsciiDocFile(stepDescription);
            } else {
                entryStep.description = stepDescription;
            }
            if (rolesAllowedList != null) {
                List<String> roleStringList = new ArrayList<>();
                for (RolesAllowed ra : rolesAllowedList) {
                    String[] allowedRolesStringArray = ra.value();
                    for (String role : allowedRolesStringArray) {
                        if (role != null) {
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
        /* NOSONAR */return useCases.computeIfAbsent(fetchId(clazz).uniqueId, name -> createEntry(clazzToFetch));
    }

    private UseCaseEntry createEntry(Class<? extends Annotation> clazz) {
        if (DEBUG) {
            LOG.info("create entry:{}", clazz);
        }
        UseCaseId id = fetchId(clazz);
        UseCaseDefGroup[] groups = fetchGroups(clazz);

        UseCaseEntry entry = new UseCaseEntry(groups);
        Class<? extends Annotation> clazzToFetch = DocReflectionUtil.resolveUnproxiedClass(clazz);
        entry.id = id.uniqueId;
        entry.idEnumName = id.name;
        UseCaseDef definition = getAnnotatedDef(clazzToFetch);
        entry.title = definition.title;
        entry.annotationName = clazzToFetch.getName();
        String description = definition.description;
        if (description.endsWith(".adoc")) {
            entry.description = createIncludeOfUseCaseAsciiDocFile(description);
        } else {
            entry.description = description;
        }
        return entry;

    }

    private UseCaseDef getAnnotatedDef(Class<?> clazzToFetch) {
        UseCaseDef d = new UseCaseDef();
        UseCaseDefinition def = clazzToFetch.getAnnotation(UseCaseDefinition.class);
        if (def != null) {
            d.title = def.title();
            d.description = def.description();
            d.group = convert(def.group());
        } else {
            PDSUseCaseDefinition pdsDef = clazzToFetch.getAnnotation(PDSUseCaseDefinition.class);
            d.title = pdsDef.title();
            d.description = pdsDef.description();
            d.group = convert(pdsDef.group());
        }
        return d;
    }

    private UseCaseDefGroup[] convert(UseCaseGroup[] g) {
        UseCaseDefGroup[] g2 = new UseCaseDefGroup[g.length];
        for (int i = 0; i < g.length; i++) {
            g2[i] = getGroup(g[i]);
        }
        return g2;
    }

    private UseCaseDefGroup[] convert(PDSUseCaseGroup[] g) {
        UseCaseDefGroup[] g2 = new UseCaseDefGroup[g.length];
        for (int i = 0; i < g.length; i++) {
            g2[i] = getGroup(g[i]);
        }
        return g2;
    }

    public UseCaseDefGroup getGroup(UseCaseGroup g) {
        UseCaseDefGroup data = groupMap.get(g);
        if (data == null) {
            data = new UseCaseDefGroup(UseCaseModelType.SECHUB);
            data.title = g.getTitle();
            data.description = g.getDescription();
            data.name = g.name();
            groupMap.put(g, data);
        }
        return data;
    }

    public UseCaseDefGroup getGroup(PDSUseCaseGroup g) {
        UseCaseDefGroup data = pdsGroupMap.get(g);
        if (data == null) {
            data = new UseCaseDefGroup(UseCaseModelType.PDS);
            data.title = g.getTitle();
            data.description = g.getDescription();
            data.name = g.name();

            pdsGroupMap.put(g, data);
        }
        return data;
    }

    private class UseCaseDef {
        String title;
        String description;
        UseCaseDefGroup[] group = new UseCaseDefGroup[] {};
    }

    private UseCaseDefGroup[] fetchGroups(Class<? extends Annotation> clazz) {
        Class<? extends Annotation> clazzToFetch = DocReflectionUtil.resolveUnproxiedClass(clazz);
        UseCaseDef definition = getAnnotatedDef(clazzToFetch);
        ;
        if (definition == null) {
            return new UseCaseDefGroup[] {};
        }
        return definition.group;
    }

    private String createIncludeOfUseCaseAsciiDocFile(String name) {
        return "include::" + pathToUseCaseBaseFolder + "/" + name + "[]";
    }

    private class UseCaseId {
        public String name;
        public String uniqueId;
    }

    private UseCaseId fetchId(Class<? extends Annotation> clazz) {
        UseCaseId r = new UseCaseId();
        Class<? extends Annotation> clazzToFetch = DocReflectionUtil.resolveUnproxiedClass(clazz);
        UseCaseDefinition definition = clazzToFetch.getAnnotation(UseCaseDefinition.class);
        if (definition != null) {

            UseCaseIdentifier ide = definition.id();
            r.name = ide.name();
            r.uniqueId = ide.uniqueId();
            return r;
        }

        PDSUseCaseDefinition definition2 = clazzToFetch.getAnnotation(PDSUseCaseDefinition.class);
        if (definition2 == null) {
            throw new IllegalStateException("cannot fetch id from " + clazz);
        }
        PDSUseCaseIdentifier ide = definition2.id();
        r.name = ide.name();
        r.uniqueId = ide.uniqueId();
        return r;
    }

    public SortedSet<UseCaseEntry> getUseCasesInsideGroup(UseCaseDefGroup wantedGroup) {
        SortedSet<UseCaseEntry> entries = new TreeSet<>();
        SortedSet<UseCaseEntry> all = getUseCases();
        for (UseCaseEntry entry : all) {
            for (UseCaseDefGroup group : entry.getGroups()) {
                if (group.name == wantedGroup.name) {
                    entries.add(entry);
                    break;
                }
            }
        }
        return entries;
    }
}
