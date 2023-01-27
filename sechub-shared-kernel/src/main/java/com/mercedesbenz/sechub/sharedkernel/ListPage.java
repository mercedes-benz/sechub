// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import java.util.List;

/**
 * A generic interface for page which contains a list with objects of given type
 * inside
 *
 * @param <T> type of elements
 */
public interface ListPage<T> {

    public int getPage();

    public int getTotalPages();

    public List<T> getContent();
}
