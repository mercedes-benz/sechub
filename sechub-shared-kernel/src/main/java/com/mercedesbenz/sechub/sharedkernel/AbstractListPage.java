// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public abstract class AbstractListPage<T> implements ListPage<T> {

    public static final String PROPERTY_PAGE = "page";
    public static final String PROPERTY_TOTAL_PAGES = "totalPages";

    private int page;

    private int totalPages;

    @Override
    public final int getPage() {
        return page;
    }

    @Override
    public final int getTotalPages() {
        return totalPages;
    }

    public final void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public final void setPage(int page) {
        this.page = page;
    }
}
