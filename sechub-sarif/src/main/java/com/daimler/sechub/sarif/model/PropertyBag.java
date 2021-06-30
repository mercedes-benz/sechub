// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 
 * From <a href=
 * "https://github.com/microsoft/sarif-tutorials/blob/main/docs/2-Basics.md#property-bags">Sarif
 * documentation</a>: <quote> Before we go any further, let's address an issue
 * that almost every tool vendor cares about: What do I do if my tool produces
 * information that the SARIF specification doesn't mention?
 * 
 * The answer is that every object in the SARIF object model — from logs to runs
 * to results to locations to messages, without exception — defines a property
 * named properties. The spec calls a property named properties a property bag.
 * </quote> See also <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317448">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
public class PropertyBag extends HashMap<String, Object> {

    private static final String TAGS = "tags";
    private static final long serialVersionUID = 1L;

    /**
     * Adds given tag to tag set
     * 
     * @param tag
     * @return <code>true</code> if this set did not already contain the specified
     *         tag
     */
    public boolean addTag(String tag) {
        return ensureAvailableTagSet().add(tag);
    }

    /**
     * This adds a property. For key "tags" there is a special handling, this is
     * always handled as a collection and internally all current elements are added
     * into a set. So tags are never duplicated.
     * 
     * @param key   when <code>null</code> nothing happens, because property bags
     *              may not have key null
     * @param value when <code>null</code> than a former will just be removed
     * @return former value or <code>null</code>
     */
    @Override
    public Object put(String key, Object value) {
        if (key == null) {
            return null;
        }
        if (value == null) {
            return remove(key);
        }
        if (TAGS.equals(key)) {
            if (value instanceof Collection) {
                // depending on on content and deserialization behaviour this can differ- e.g.
                // an ArrayList
                @SuppressWarnings("unchecked")
                Collection<Object> list = (Collection<Object>) value;
                for (Object object : list) {
                    if (object != null) {
                        ensureAvailableTagSet().add(object.toString());
                    }
                }
                return null;

            }
            if (!(value instanceof Set)) {
                throw new IllegalArgumentException("key:" + key + " must contain an set/array, but was:" + (value == null ? null : value.getClass()));
            }
        }
        return super.put(key, value);
    }

    /**
     * Fetch tags
     * 
     * @return tags or empty set, when not defined
     */
    public Set<String> fetchTags() {
        return ensureAvailableTagSet();
    }

    private Set<String> ensureAvailableTagSet() {
        Object data = computeIfAbsent(TAGS, key -> new LinkedHashSet<>());
        if (!(data instanceof Set)) {
            throw new IllegalStateException("tags property does not contain a list but:" + data);
        }
        @SuppressWarnings("unchecked")
        Set<String> dataList = (Set<String>) data;
        return dataList;
    }
}
