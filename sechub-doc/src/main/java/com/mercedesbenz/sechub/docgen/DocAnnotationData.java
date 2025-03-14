// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.scheduling.annotation.Scheduled;

/* inspired by: https://raw.githubusercontent.com/de-jcup/code2doc/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/TechInfoLinkAnnotationData.java */
/**
 * Annotation data for documentation
 *
 * @author Albert Tregnaghi
 *
 */
public class DocAnnotationData implements Comparable<DocAnnotationData> {
    /**
     * Reflection link to class where annotation @MustBeDocumented is appended
     */
    public Class<?> /* NOSONAR */ linkedClass;
    /**
     * Scope for this annotation data - is scope from @MustBeDocumented
     */
    public String /* NOSONAR */ scope = "none";

    /**
     * Reflection link to method where annotation @MustBeDocumented is appended
     */
    public Method /* NOSONAR */ linkedMethod;

    /**
     * Meta data from spring @Value annotation - it just contains the value()
     * content
     */
    public String /* NOSONAR */ springValue;

    /**
     * Meta data from spring @Scheduled annotation - contains full annotation or
     * <code>null</code>
     */
    public Scheduled /* NOSONAR */ springScheduled;

    /**
     * Reflection link to field where annotation @MustBeDocumented is appended
     */
    public Field /* NOSONAR */ linkedField;
    public String /* NOSONAR */ description;
    public boolean /* NOSONAR */ isSecret;

    /**
     * Here we can add some additional options - if necessary
     */
    public Map<String, String> options = new TreeMap<>();
    public ConfigurationPropertiesData propertiesData;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((linkedClass == null) ? 0 : linkedClass.hashCode());
        result = prime * result + ((linkedField == null) ? 0 : linkedField.hashCode());
        result = prime * result + ((linkedMethod == null) ? 0 : linkedMethod.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
        DocAnnotationData other = (DocAnnotationData) obj;
        if (linkedClass == null) {
            if (other.linkedClass != null)
                return false;
        } else if (!linkedClass.equals(other.linkedClass)) {
            return false;
        }
        if (linkedField == null) {
            if (other.linkedField != null)
                return false;
        } else if (!linkedField.equals(other.linkedField)) {
            return false;
        }
        if (linkedMethod == null) {
            if (other.linkedMethod != null)
                return false;
        } else if (!linkedMethod.equals(other.linkedMethod)) {
            return false;
        }
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TechInfoLinkAnnotationData [linkedClass=" + linkedClass + ", scope=" + scope + ", linkedMethod=" + linkedMethod + ", linkedField=" + linkedField
                + ", options = " + options + " ]";
    }

    @Override
    public int compareTo(DocAnnotationData o) {
        /* dumb but working */
        return hashCode() - o.hashCode();
    }

}