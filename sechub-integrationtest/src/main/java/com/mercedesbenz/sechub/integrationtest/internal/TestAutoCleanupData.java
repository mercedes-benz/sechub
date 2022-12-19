// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.Objects;

public class TestAutoCleanupData {

    private TestCleanupTime cleanupTime = new TestCleanupTime();
    private String version = "1.0";

    public TestAutoCleanupData() {
        /* for json serialization */
    }

    public TestAutoCleanupData(int amount, TestCleanupTimeUnit unit) {
        TestCleanupTime cleanupTime = getCleanupTime();
        cleanupTime.setAmount(amount);
        cleanupTime.setUnit(unit);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public TestCleanupTime getCleanupTime() {
        return cleanupTime;
    }

    public enum TestCleanupTimeUnit {
        DAY,

        WEEK,

        MONTH,

        YEAR
    }

    public class TestCleanupTime {
        private TestCleanupTimeUnit unit;
        private int amount;

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public void setUnit(TestCleanupTimeUnit unit) {
            this.unit = unit;
        }

        public int getAmount() {
            return amount;
        }

        public TestCleanupTimeUnit getUnit() {
            return unit;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hash(amount, unit);
            return result;
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
            TestCleanupTime other = (TestCleanupTime) obj;
            return amount == other.amount && Objects.equals(unit, other.unit);
        }

    }

    @Override
    public int hashCode() {
        return Objects.hash(cleanupTime, version);
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
        TestAutoCleanupData other = (TestAutoCleanupData) obj;
        return Objects.equals(cleanupTime, other.cleanupTime) && Objects.equals(version, other.version);
    }
}
