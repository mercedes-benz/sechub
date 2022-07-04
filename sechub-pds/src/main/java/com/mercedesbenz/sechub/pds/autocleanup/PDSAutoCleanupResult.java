// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import java.time.LocalDateTime;
import java.util.Objects;

public class PDSAutoCleanupResult {

    private long cleanupTimeInDays;

    private LocalDateTime cleanupTimeStamp;

    private int deletedEntries;

    private AutoCleanupResultKey key;

    private PDSAutoCleanupResult(AutoCleanupResultKey key) {
        this.key = key;
    }

    public static final AutoCleanupResultBuilder builder() {
        return new AutoCleanupResultBuilder();
    }

    public long getCleanupTimeInDays() {
        return cleanupTimeInDays;
    }

    public LocalDateTime getUsedCleanupTimeStamp() {
        return cleanupTimeStamp;
    }

    public int getDeletedEntries() {
        return deletedEntries;
    }

    public AutoCleanupResultKey getKey() {
        return key;
    }

    public static class AutoCleanupResultKey {
        private Class<?> inspectedClass;
        private String variant;

        public AutoCleanupResultKey(String variant, Class<?> inspectedClass) {
            this.variant = variant;
            this.inspectedClass = inspectedClass;
        }

        public Class<?> getInspectedClass() {
            return inspectedClass;
        }

        /**
         * @return variant, or <code>null</code>
         */
        public String getVariant() {
            return variant;
        }

        @Override
        public int hashCode() {
            return Objects.hash(inspectedClass, variant);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AutoCleanupResultKey other = (AutoCleanupResultKey) obj;
            return inspectedClass == other.inspectedClass && Objects.equals(variant, other.variant);
        }

    }

    public static class AutoCleanupResultBuilder {

        private long cleanupTimeInDays;

        private LocalDateTime cleanupTimeStamp;
        private int deletedEntries;
        private Class<?> inspectedClass;
        private String variant;

        private AutoCleanupResultBuilder() {
        }

        public PDSAutoCleanupResult build() {
            /* fallback to defaults when not set, to avoid NPEs and crude log output */
            if (variant == null) {
                variant = "not-defined";
            }
            if (inspectedClass == null) {
                inspectedClass = Object.class;
            }
            AutoCleanupResultKey key = new AutoCleanupResultKey(variant, inspectedClass);
            PDSAutoCleanupResult data = new PDSAutoCleanupResult(key);
            data.deletedEntries = deletedEntries;
            data.cleanupTimeInDays = cleanupTimeInDays;
            data.cleanupTimeStamp = cleanupTimeStamp;
            return data;
        }

        public AutoCleanupResultBuilder forDays(long days) {
            this.cleanupTimeInDays = days;
            return this;
        }

        public AutoCleanupResultBuilder hasDeleted(int amount) {
            this.deletedEntries = amount;
            return this;
        }

        public AutoCleanupResultBuilder autoCleanup(String variant, Class<?> clazz) {
            this.inspectedClass = clazz;
            this.variant = variant;
            return this;
        }

        public AutoCleanupResultBuilder byTimeStamp(LocalDateTime timeStamp) {
            this.cleanupTimeStamp = timeStamp;
            return this;
        }
    }
}
