package com.mercedesbenz.sechub.commons.model;

public interface TypeFilter {
    public static class AcceptAllTypeFilter implements TypeFilter {

        @Override
        public boolean isTypeAccepted(String type) {
            return true;
        }
    }

    public static class AcceptTypeFilter implements TypeFilter {

        private String type;

        public AcceptTypeFilter(String type) {
            this.type = type;
        }

        @Override
        public boolean isTypeAccepted(String type) {
            if (type == null) {
                return false;
            }
            // more than one type can be defined eg. docker,git,binary
            final String[] splitTypes = type.split(",");
            for (String splitType : splitTypes) {
                if (this.type.equals(splitType)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final AcceptAllTypeFilter ACCEPT_ALL = new AcceptAllTypeFilter();

    public boolean isTypeAccepted(String type);
}
