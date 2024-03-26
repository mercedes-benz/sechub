package com.mercedesbenz.sechub.commons.model;

public interface TypeFilter {
    public static class AcceptAllTypeFilter implements TypeFilter {

        @Override
        public boolean isTypeAccepted(String type) {
            return true;
        }
    }

    public static final AcceptAllTypeFilter ACCEPT_ALL = new AcceptAllTypeFilter();

    public boolean isTypeAccepted(String type);
}
