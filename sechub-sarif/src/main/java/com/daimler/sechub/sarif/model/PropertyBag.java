// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public void addTag(String tag) {
        Object data = computeIfAbsent(TAGS, key -> new ArrayList<>());
        if (!(data instanceof List)) {
            throw new IllegalStateException("tags property does not contain a list but:" + data);
        }
        @SuppressWarnings("unchecked")
        List<String> dataList = (List<String>) data;
        dataList.add(tag);
    }

    /**
     * Fetch tags
     * 
     * @return tags or empty list, when not defined
     */
    public List<String> fetchTags() {
        String key = TAGS;
        return fetchStringListByKey(key);
    }

    /**
     * Fetches string list by given key
     * 
     * @param key
     * @return a list containing list entries or being empty when value for key is
     *         not a list
     */
    public List<String> fetchStringListByKey(String key) {
        Object tags = get(key);
        List<String> result = new ArrayList<>();
        if (tags instanceof List) {
            List<?> dataList = (List<?>) tags;
            for (Object data : dataList) {
                if (data != null) {
                    result.add(data.toString());
                }
            }
        }
        return result;
    }

}
